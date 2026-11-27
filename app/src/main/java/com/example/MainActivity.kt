package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ActiveScreen
import com.example.data.TargetFps
import com.example.ui.DetailedSettingsSheet
import com.example.ui.HomeScreen
import com.example.ui.IsoGuideSheet
import com.example.ui.LauncherViewModel
import com.example.ui.PlayScreen
import com.example.ui.theme.BloodlinesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BloodlinesTheme {
                val viewModel: LauncherViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.refreshExternalCore()
                }

                // Zero-permission SAF Document Picker for .iso / .cso files
                val isoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri ->
                    if (uri != null) {
                        viewModel.onIsoPicked(uri)
                    }
                }

                val launchIsoPicker = {
                    isoPickerLauncher.launch(arrayOf("*/*"))
                }

                when (uiState.activeScreen) {
                    ActiveScreen.HOME -> {
                        HomeScreen(
                            uiState = uiState,
                            onPickIsoClick = launchIsoPicker,
                            onOpenGuideClick = { viewModel.setIsoGuideVisible(true) },
                            onDeleteIsoRequest = { viewModel.requestDeleteIso(true) },
                            onConfirmDeleteIso = { viewModel.confirmDeleteIso() },
                            onDismissDeleteIso = { viewModel.requestDeleteIso(false) },
                            onDismissImportError = { viewModel.clearImportError() },
                            onOpenSettingsClick = { viewModel.setAdvancedSettingsVisible(true) },
                            onSelectPreset = viewModel::applyPreset,
                            onToggleDisableHeavyEffects = viewModel::updateDisableHeavyEffects,
                            onToggleLazyTexture = viewModel::updateLazyTextureCaching,
                            onToggleTarget60Fps = { enabled ->
                                viewModel.updateTargetFps(
                                    if (enabled) TargetFps.FPS_60 else TargetFps.FPS_30
                                )
                            },
                            onPlayClick = { viewModel.enterPlayMode() }
                        )
                    }

                    ActiveScreen.PLAY -> {
                        PlayScreen(
                            isoInfo = uiState.isoInfo,
                            settings = uiState.settings,
                            hasExternalCore = uiState.installedExternalCorePackage != null,
                            onSelectPreset = viewModel::applyPreset,
                            onOpenSettings = { viewModel.setAdvancedSettingsVisible(true) },
                            onLaunchExternalCore = {
                                viewModel.buildExternalCoreIntent()?.let { intent ->
                                    runCatching { startActivity(intent) }
                                }
                            },
                            onExitPlay = { viewModel.exitPlayMode() }
                        )
                    }
                }

                if (uiState.showIsoGuideSheet) {
                    IsoGuideSheet(
                        onDismiss = { viewModel.setIsoGuideVisible(false) },
                        onPickIsoClick = launchIsoPicker
                    )
                }

                if (uiState.showAdvancedSettingsSheet) {
                    DetailedSettingsSheet(
                        settings = uiState.settings,
                        onDismiss = { viewModel.setAdvancedSettingsVisible(false) },
                        onSelectPreset = viewModel::applyPreset,
                        onSelectBackend = viewModel::updateRenderBackend,
                        onSelectResolution = viewModel::updateResolutionScale,
                        onSelectTargetFps = viewModel::updateTargetFps,
                        onSelectFrameskip = viewModel::updateFrameskip,
                        onSelectCpuClock = viewModel::updateCpuClock,
                        onToggleLazyTexture = viewModel::updateLazyTextureCaching,
                        onToggleHardwareTransform = viewModel::updateHardwareTransform,
                        onToggleDisableHeavyEffects = viewModel::updateDisableHeavyEffects,
                        onToggleLowLatencyAudio = viewModel::updateLowLatencyAudio,
                        onToggleRetainTextures = viewModel::updateRetainChangedTextures,
                        onChangeHudScale = viewModel::updateHudScale,
                        onChangeHudOpacity = viewModel::updateHudOpacity,
                        onToggleHaptic = viewModel::updateHapticFeedback,
                        onToggleShowLabels = viewModel::updateShowActionLabels
                    )
                }
            }
        }
    }
}
