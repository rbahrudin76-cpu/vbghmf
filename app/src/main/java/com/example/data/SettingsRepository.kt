package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

private val Context.launcherDataStore: DataStore<Preferences> by preferencesDataStore(name = "bloodlines_launcher_prefs")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val PRESET = stringPreferencesKey("perf_preset")
        val BACKEND = stringPreferencesKey("perf_backend")
        val RESOLUTION = intPreferencesKey("perf_resolution")
        val TARGET_FPS = intPreferencesKey("perf_target_fps")
        val FRAMESKIP = intPreferencesKey("perf_frameskip")
        val CPU_CLOCK = intPreferencesKey("perf_cpu_clock")
        val LAZY_TEXTURE = booleanPreferencesKey("perf_lazy_texture")
        val HARDWARE_TRANSFORM = booleanPreferencesKey("perf_hw_transform")
        val DISABLE_HEAVY_EFFECTS = booleanPreferencesKey("perf_disable_heavy_effects")
        val LOW_LATENCY_AUDIO = booleanPreferencesKey("perf_low_latency_audio")
        val RETAIN_TEXTURES = booleanPreferencesKey("perf_retain_textures")
        val HUD_SCALE = floatPreferencesKey("hud_scale")
        val HUD_OPACITY = floatPreferencesKey("hud_opacity")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("hud_haptic")
        val SHOW_ACTION_LABELS = booleanPreferencesKey("hud_show_labels")

        val ISO_INSTALLED = booleanPreferencesKey("iso_installed")
        val ISO_FILE_NAME = stringPreferencesKey("iso_file_name")
        val ISO_INTERNAL_PATH = stringPreferencesKey("iso_internal_path")
        val ISO_SIZE_BYTES = longPreferencesKey("iso_size_bytes")
        val ISO_FORMAT = stringPreferencesKey("iso_format")
        val ISO_DISC_ID = stringPreferencesKey("iso_disc_id")
        val ISO_DISC_TITLE = stringPreferencesKey("iso_disc_title")
        val ISO_VOLUME_LABEL = stringPreferencesKey("iso_volume_label")
        val ISO_VERIFIED_PSP = booleanPreferencesKey("iso_verified_psp")
        val ISO_BLOODLINES_MATCH = booleanPreferencesKey("iso_bloodlines_match")
        val ISO_ICON_PATH = stringPreferencesKey("iso_icon_path")
        val ISO_PIC1_PATH = stringPreferencesKey("iso_pic1_path")
        val ISO_IMPORTED_AT = longPreferencesKey("iso_imported_at")
    }

    val performanceSettingsFlow: Flow<PerformanceSettings> = context.launcherDataStore.data.map { prefs ->
        val presetName = prefs[Keys.PRESET] ?: PerformancePreset.ULTRA_ANTI_LAG.name
        val preset = PerformancePreset.entries.firstOrNull { it.name == presetName }
            ?: PerformancePreset.ULTRA_ANTI_LAG

        val backendName = prefs[Keys.BACKEND] ?: RenderBackend.VULKAN.name
        val backend = RenderBackend.entries.firstOrNull { it.name == backendName }
            ?: RenderBackend.VULKAN

        val resFactor = prefs[Keys.RESOLUTION] ?: ResolutionScale.X1_NATIVE.factor
        val resolution = ResolutionScale.entries.firstOrNull { it.factor == resFactor }
            ?: ResolutionScale.X1_NATIVE

        val fpsValue = prefs[Keys.TARGET_FPS] ?: TargetFps.FPS_60.fps
        val targetFps = TargetFps.entries.firstOrNull { it.fps == fpsValue } ?: TargetFps.FPS_60

        val frameskipVal = prefs[Keys.FRAMESKIP] ?: FrameskipLevel.ONE.level
        val frameskip = FrameskipLevel.entries.firstOrNull { it.level == frameskipVal }
            ?: FrameskipLevel.ONE

        val cpuVal = prefs[Keys.CPU_CLOCK] ?: CpuClockMode.MHZ_333.mhz
        val cpuClock = CpuClockMode.entries.firstOrNull { it.mhz == cpuVal } ?: CpuClockMode.MHZ_333

        PerformanceSettings(
            preset = preset,
            renderBackend = backend,
            resolutionScale = resolution,
            targetFps = targetFps,
            frameskip = frameskip,
            cpuClock = cpuClock,
            lazyTextureCaching = prefs[Keys.LAZY_TEXTURE] ?: true,
            hardwareTransform = prefs[Keys.HARDWARE_TRANSFORM] ?: true,
            disableHeavyEffects = prefs[Keys.DISABLE_HEAVY_EFFECTS] ?: true,
            lowLatencyAudio = prefs[Keys.LOW_LATENCY_AUDIO] ?: true,
            retainChangedTextures = prefs[Keys.RETAIN_TEXTURES] ?: true,
            hudButtonScale = prefs[Keys.HUD_SCALE] ?: 1.0f,
            hudOpacity = prefs[Keys.HUD_OPACITY] ?: 0.85f,
            hapticFeedback = prefs[Keys.HAPTIC_FEEDBACK] ?: true,
            showActionLabels = prefs[Keys.SHOW_ACTION_LABELS] ?: true
        )
    }

    val importedIsoFlow: Flow<ImportedIsoInfo> = context.launcherDataStore.data.map { prefs ->
        val path = prefs[Keys.ISO_INTERNAL_PATH].orEmpty()
        val fileExists = path.isNotEmpty() && File(path).exists()
        val installed = (prefs[Keys.ISO_INSTALLED] ?: false) && fileExists
        if (!installed) {
            ImportedIsoInfo()
        } else {
            val iconPath = prefs[Keys.ISO_ICON_PATH]?.takeIf { it.isNotEmpty() && File(it).exists() }
            val pic1Path = prefs[Keys.ISO_PIC1_PATH]?.takeIf { it.isNotEmpty() && File(it).exists() }
            ImportedIsoInfo(
                isInstalled = true,
                fileName = prefs[Keys.ISO_FILE_NAME].orEmpty(),
                internalFilePath = path,
                sizeBytes = File(path).length(),
                format = prefs[Keys.ISO_FORMAT].orEmpty(),
                discId = prefs[Keys.ISO_DISC_ID].orEmpty(),
                discTitle = prefs[Keys.ISO_DISC_TITLE].orEmpty(),
                volumeLabel = prefs[Keys.ISO_VOLUME_LABEL].orEmpty(),
                isVerifiedPspDisc = prefs[Keys.ISO_VERIFIED_PSP] ?: false,
                isBloodlinesMatch = prefs[Keys.ISO_BLOODLINES_MATCH] ?: false,
                iconPath = iconPath,
                pic1Path = pic1Path,
                importedAtMillis = prefs[Keys.ISO_IMPORTED_AT] ?: 0L
            )
        }
    }

    suspend fun savePerformanceSettings(settings: PerformanceSettings) {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.PRESET] = settings.preset.name
            prefs[Keys.BACKEND] = settings.renderBackend.name
            prefs[Keys.RESOLUTION] = settings.resolutionScale.factor
            prefs[Keys.TARGET_FPS] = settings.targetFps.fps
            prefs[Keys.FRAMESKIP] = settings.frameskip.level
            prefs[Keys.CPU_CLOCK] = settings.cpuClock.mhz
            prefs[Keys.LAZY_TEXTURE] = settings.lazyTextureCaching
            prefs[Keys.HARDWARE_TRANSFORM] = settings.hardwareTransform
            prefs[Keys.DISABLE_HEAVY_EFFECTS] = settings.disableHeavyEffects
            prefs[Keys.LOW_LATENCY_AUDIO] = settings.lowLatencyAudio
            prefs[Keys.RETAIN_TEXTURES] = settings.retainChangedTextures
            prefs[Keys.HUD_SCALE] = settings.hudButtonScale
            prefs[Keys.HUD_OPACITY] = settings.hudOpacity
            prefs[Keys.HAPTIC_FEEDBACK] = settings.hapticFeedback
            prefs[Keys.SHOW_ACTION_LABELS] = settings.showActionLabels
        }
    }

    suspend fun saveImportedIso(info: ImportedIsoInfo) {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.ISO_INSTALLED] = info.isInstalled
            prefs[Keys.ISO_FILE_NAME] = info.fileName
            prefs[Keys.ISO_INTERNAL_PATH] = info.internalFilePath
            prefs[Keys.ISO_SIZE_BYTES] = info.sizeBytes
            prefs[Keys.ISO_FORMAT] = info.format
            prefs[Keys.ISO_DISC_ID] = info.discId
            prefs[Keys.ISO_DISC_TITLE] = info.discTitle
            prefs[Keys.ISO_VOLUME_LABEL] = info.volumeLabel
            prefs[Keys.ISO_VERIFIED_PSP] = info.isVerifiedPspDisc
            prefs[Keys.ISO_BLOODLINES_MATCH] = info.isBloodlinesMatch
            prefs[Keys.ISO_ICON_PATH] = info.iconPath.orEmpty()
            prefs[Keys.ISO_PIC1_PATH] = info.pic1Path.orEmpty()
            prefs[Keys.ISO_IMPORTED_AT] = info.importedAtMillis
        }
    }

    suspend fun clearImportedIso() {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.ISO_INSTALLED] = false
            prefs.remove(Keys.ISO_FILE_NAME)
            prefs.remove(Keys.ISO_INTERNAL_PATH)
            prefs.remove(Keys.ISO_SIZE_BYTES)
            prefs.remove(Keys.ISO_FORMAT)
            prefs.remove(Keys.ISO_DISC_ID)
            prefs.remove(Keys.ISO_DISC_TITLE)
            prefs.remove(Keys.ISO_VOLUME_LABEL)
            prefs.remove(Keys.ISO_VERIFIED_PSP)
            prefs.remove(Keys.ISO_BLOODLINES_MATCH)
            prefs.remove(Keys.ISO_ICON_PATH)
            prefs.remove(Keys.ISO_PIC1_PATH)
            prefs.remove(Keys.ISO_IMPORTED_AT)
        }
    }
}
