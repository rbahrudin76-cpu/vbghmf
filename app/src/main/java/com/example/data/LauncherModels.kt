package com.example.data

enum class RenderBackend(val label: String, val subtitle: String) {
    VULKAN("Vulkan", "Tercepat & minim stutter di HP modern"),
    OPENGL_ES("OpenGL ES", "Kompatibilitas luas untuk semua GPU")
}

enum class ResolutionScale(val factor: Int, val label: String, val dimensions: String) {
    X1_NATIVE(1, "1x PSP", "480×272 • Paling Ringan"),
    X2_HD(2, "2x HD", "960×544 • Tajam & Lancar"),
    X3_FHD(3, "3x FHD", "1440×816 • Ultra Tajam")
}

enum class TargetFps(val fps: Int, val label: String) {
    FPS_30(30, "30 FPS (Stabil)"),
    FPS_60(60, "60 FPS (Smooth)")
}

enum class FrameskipLevel(val level: Int, val label: String) {
    OFF(0, "Mati"),
    ONE(1, "Auto 1 Frame"),
    TWO(2, "Auto 2 Frame")
}

enum class CpuClockMode(val mhz: Int, val label: String) {
    AUTO(0, "Otomatis"),
    MHZ_222(222, "222 MHz (Hemat)"),
    MHZ_333(333, "333 MHz (Turbo Anti-Lag)")
}

enum class PerformancePreset(val label: String, val description: String) {
    ULTRA_ANTI_LAG("Anti-Lag Ultra", "Fokus FPS stabil tanpa patah-patah"),
    BALANCED("Seimbang", "Tajam 2x HD dengan performa halus"),
    HIGH_FIDELITY("Grafis Maksimal", "Resolusi 3x & efek visual penuh"),
    CUSTOM("Kustom", "Pengaturan manual sesuai perangkat")
}

data class PerformanceSettings(
    val preset: PerformancePreset = PerformancePreset.ULTRA_ANTI_LAG,
    val renderBackend: RenderBackend = RenderBackend.VULKAN,
    val resolutionScale: ResolutionScale = ResolutionScale.X2_HD,
    val targetFps: TargetFps = TargetFps.FPS_60,
    val frameskip: FrameskipLevel = FrameskipLevel.ONE,
    val cpuClock: CpuClockMode = CpuClockMode.MHZ_333,
    val lazyTextureCaching: Boolean = true,
    val hardwareTransform: Boolean = true,
    val disableHeavyEffects: Boolean = true,
    val lowLatencyAudio: Boolean = true,
    val retainChangedTextures: Boolean = true,
    val hudButtonScale: Float = 1.0f,
    val hudOpacity: Float = 0.85f,
    val hapticFeedback: Boolean = true,
    val showActionLabels: Boolean = true
) {
    companion object {
        fun forPreset(preset: PerformancePreset, current: PerformanceSettings): PerformanceSettings {
            return when (preset) {
                PerformancePreset.ULTRA_ANTI_LAG -> current.copy(
                    preset = preset,
                    renderBackend = RenderBackend.VULKAN,
                    resolutionScale = ResolutionScale.X1_NATIVE,
                    targetFps = TargetFps.FPS_60,
                    frameskip = FrameskipLevel.ONE,
                    cpuClock = CpuClockMode.MHZ_333,
                    lazyTextureCaching = true,
                    hardwareTransform = true,
                    disableHeavyEffects = true,
                    lowLatencyAudio = true,
                    retainChangedTextures = true
                )
                PerformancePreset.BALANCED -> current.copy(
                    preset = preset,
                    renderBackend = RenderBackend.VULKAN,
                    resolutionScale = ResolutionScale.X2_HD,
                    targetFps = TargetFps.FPS_60,
                    frameskip = FrameskipLevel.OFF,
                    cpuClock = CpuClockMode.AUTO,
                    lazyTextureCaching = true,
                    hardwareTransform = true,
                    disableHeavyEffects = false,
                    lowLatencyAudio = true,
                    retainChangedTextures = true
                )
                PerformancePreset.HIGH_FIDELITY -> current.copy(
                    preset = preset,
                    renderBackend = RenderBackend.OPENGL_ES,
                    resolutionScale = ResolutionScale.X3_FHD,
                    targetFps = TargetFps.FPS_60,
                    frameskip = FrameskipLevel.OFF,
                    cpuClock = CpuClockMode.MHZ_333,
                    lazyTextureCaching = false,
                    hardwareTransform = true,
                    disableHeavyEffects = false,
                    lowLatencyAudio = false,
                    retainChangedTextures = false
                )
                PerformancePreset.CUSTOM -> current.copy(preset = preset)
            }
        }
    }
}

data class ImportedIsoInfo(
    val isInstalled: Boolean = false,
    val fileName: String = "",
    val internalFilePath: String = "",
    val sizeBytes: Long = 0L,
    val format: String = "",
    val discId: String = "",
    val discTitle: String = "",
    val volumeLabel: String = "",
    val isVerifiedPspDisc: Boolean = false,
    val isBloodlinesMatch: Boolean = false,
    val iconPath: String? = null,
    val pic1Path: String? = null,
    val importedAtMillis: Long = 0L
) {
    val formattedSizeMb: String
        get() = if (sizeBytes <= 0L) "0 MB" else String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0))
}

sealed interface ImportProgressState {
    data object Idle : ImportProgressState
    data class Copying(
        val copiedBytes: Long,
        val totalBytes: Long,
        val speedMbps: Float
    ) : ImportProgressState {
        val fraction: Float
            get() = if (totalBytes > 0L) (copiedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
    }
    data object Verifying : ImportProgressState
    data class Error(val message: String) : ImportProgressState
}

enum class ActiveScreen {
    HOME,
    PLAY
}
