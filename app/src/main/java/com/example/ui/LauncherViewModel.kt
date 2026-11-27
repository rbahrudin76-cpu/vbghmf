package com.example.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActiveScreen
import com.example.data.CpuClockMode
import com.example.data.FrameskipLevel
import com.example.data.ImportProgressState
import com.example.data.ImportedIsoInfo
import com.example.data.IsoVaultRepository
import com.example.data.PerformancePreset
import com.example.data.PerformanceSettings
import com.example.data.RenderBackend
import com.example.data.ResolutionScale
import com.example.data.SettingsRepository
import com.example.data.TargetFps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LauncherUiState(
    val activeScreen: ActiveScreen = ActiveScreen.HOME,
    val settings: PerformanceSettings = PerformanceSettings(),
    val isoInfo: ImportedIsoInfo = ImportedIsoInfo(),
    val importState: ImportProgressState = ImportProgressState.Idle,
    val showIsoGuideSheet: Boolean = false,
    val showAdvancedSettingsSheet: Boolean = false,
    val showDeleteIsoConfirm: Boolean = false,
    val installedExternalCorePackage: String? = null
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application.applicationContext)
    private val isoVaultRepository = IsoVaultRepository(
        context = application.applicationContext,
        settingsRepository = settingsRepository
    )

    private val activeScreen = MutableStateFlow(ActiveScreen.HOME)
    private val importState = MutableStateFlow<ImportProgressState>(ImportProgressState.Idle)
    private val showIsoGuideSheet = MutableStateFlow(false)
    private val showAdvancedSettingsSheet = MutableStateFlow(false)
    private val showDeleteIsoConfirm = MutableStateFlow(false)
    private val externalCorePackage = MutableStateFlow(isoVaultRepository.findInstalledExternalPspCore())

    private val modalFlags = combine(
        showIsoGuideSheet,
        showAdvancedSettingsSheet,
        showDeleteIsoConfirm,
        externalCorePackage
    ) { guide, settingsSheet, deleteConfirm, extPkg ->
        ModalState(guide, settingsSheet, deleteConfirm, extPkg)
    }

    val uiState: StateFlow<LauncherUiState> = combine(
        activeScreen,
        settingsRepository.performanceSettingsFlow,
        settingsRepository.importedIsoFlow,
        importState,
        modalFlags
    ) { screen, settings, iso, importProg, modals ->
        LauncherUiState(
            activeScreen = screen,
            settings = settings,
            isoInfo = iso,
            importState = importProg,
            showIsoGuideSheet = modals.guide,
            showAdvancedSettingsSheet = modals.settingsSheet,
            showDeleteIsoConfirm = modals.deleteConfirm,
            installedExternalCorePackage = modals.externalCorePkg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LauncherUiState()
    )

    private data class ModalState(
        val guide: Boolean,
        val settingsSheet: Boolean,
        val deleteConfirm: Boolean,
        val externalCorePkg: String?
    )

    fun refreshExternalCore() {
        externalCorePackage.value = isoVaultRepository.findInstalledExternalPspCore()
    }

    fun onIsoPicked(uri: Uri) {
        if (importState.value is ImportProgressState.Copying || importState.value is ImportProgressState.Verifying) {
            return
        }
        viewModelScope.launch {
            isoVaultRepository.importIsoFromUri(uri).collect { progress ->
                importState.value = progress
            }
        }
    }

    fun clearImportError() {
        if (importState.value is ImportProgressState.Error) {
            importState.value = ImportProgressState.Idle
        }
    }

    fun requestDeleteIso(show: Boolean) {
        showDeleteIsoConfirm.value = show
    }

    fun confirmDeleteIso() {
        showDeleteIsoConfirm.value = false
        viewModelScope.launch {
            isoVaultRepository.deleteVaultIso()
        }
    }

    fun setIsoGuideVisible(visible: Boolean) {
        showIsoGuideSheet.value = visible
    }

    fun setAdvancedSettingsVisible(visible: Boolean) {
        showAdvancedSettingsSheet.value = visible
    }

    fun enterPlayMode() {
        if (uiState.value.isoInfo.isInstalled) {
            activeScreen.value = ActiveScreen.PLAY
        } else {
            showIsoGuideSheet.value = true
        }
    }

    fun exitPlayMode() {
        activeScreen.value = ActiveScreen.HOME
    }

    fun applyPreset(preset: PerformancePreset) {
        val current = uiState.value.settings
        val updated = PerformanceSettings.forPreset(preset, current)
        saveSettings(updated)
    }

    fun updateRenderBackend(backend: RenderBackend) {
        saveCustomSettings { it.copy(renderBackend = backend) }
    }

    fun updateResolutionScale(scale: ResolutionScale) {
        saveCustomSettings { it.copy(resolutionScale = scale) }
    }

    fun updateTargetFps(fps: TargetFps) {
        saveCustomSettings { it.copy(targetFps = fps) }
    }

    fun updateFrameskip(level: FrameskipLevel) {
        saveCustomSettings { it.copy(frameskip = level) }
    }

    fun updateCpuClock(clock: CpuClockMode) {
        saveCustomSettings { it.copy(cpuClock = clock) }
    }

    fun updateLazyTextureCaching(enabled: Boolean) {
        saveCustomSettings { it.copy(lazyTextureCaching = enabled) }
    }

    fun updateHardwareTransform(enabled: Boolean) {
        saveCustomSettings { it.copy(hardwareTransform = enabled) }
    }

    fun updateDisableHeavyEffects(enabled: Boolean) {
        saveCustomSettings { it.copy(disableHeavyEffects = enabled) }
    }

    fun updateLowLatencyAudio(enabled: Boolean) {
        saveCustomSettings { it.copy(lowLatencyAudio = enabled) }
    }

    fun updateRetainChangedTextures(enabled: Boolean) {
        saveCustomSettings { it.copy(retainChangedTextures = enabled) }
    }

    fun updateHudScale(scale: Float) {
        saveSettings(uiState.value.settings.copy(hudButtonScale = scale.coerceIn(0.75f, 1.35f)))
    }

    fun updateHudOpacity(opacity: Float) {
        saveSettings(uiState.value.settings.copy(hudOpacity = opacity.coerceIn(0.35f, 1.0f)))
    }

    fun updateHapticFeedback(enabled: Boolean) {
        saveSettings(uiState.value.settings.copy(hapticFeedback = enabled))
    }

    fun updateShowActionLabels(enabled: Boolean) {
        saveSettings(uiState.value.settings.copy(showActionLabels = enabled))
    }

    fun buildExternalCoreIntent(): Intent? {
        val pkg = uiState.value.installedExternalCorePackage ?: return null
        val path = uiState.value.isoInfo.internalFilePath
        if (path.isEmpty()) return null
        return isoVaultRepository.buildExternalCoreLaunchIntent(pkg, path)
    }

    private fun saveCustomSettings(transform: (PerformanceSettings) -> PerformanceSettings) {
        val current = uiState.value.settings
        val updated = transform(current).copy(preset = PerformancePreset.CUSTOM)
        saveSettings(updated)
    }

    private fun saveSettings(updated: PerformanceSettings) {
        viewModelScope.launch {
            settingsRepository.savePerformanceSettings(updated)
        }
    }
}
