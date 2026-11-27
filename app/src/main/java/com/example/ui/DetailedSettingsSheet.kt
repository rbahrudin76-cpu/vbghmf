package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CpuClockMode
import com.example.data.FrameskipLevel
import com.example.data.PerformancePreset
import com.example.data.PerformanceSettings
import com.example.data.RenderBackend
import com.example.data.ResolutionScale
import com.example.data.TargetFps
import com.example.ui.theme.AnimusCyan
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.AshMuted
import com.example.ui.theme.BasaltSurface
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ElevatedBasalt
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ParchmentText
import com.example.ui.theme.WarmGoldBright

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedSettingsSheet(
    settings: PerformanceSettings,
    onDismiss: () -> Unit,
    onSelectPreset: (PerformancePreset) -> Unit,
    onSelectBackend: (RenderBackend) -> Unit,
    onSelectResolution: (ResolutionScale) -> Unit,
    onSelectTargetFps: (TargetFps) -> Unit,
    onSelectFrameskip: (FrameskipLevel) -> Unit,
    onSelectCpuClock: (CpuClockMode) -> Unit,
    onToggleLazyTexture: (Boolean) -> Unit,
    onToggleHardwareTransform: (Boolean) -> Unit,
    onToggleDisableHeavyEffects: (Boolean) -> Unit,
    onToggleLowLatencyAudio: (Boolean) -> Unit,
    onToggleRetainTextures: (Boolean) -> Unit,
    onChangeHudScale: (Float) -> Unit,
    onChangeHudOpacity: (Float) -> Unit,
    onToggleHaptic: (Boolean) -> Unit,
    onToggleShowLabels: (Boolean) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BasaltSurface,
        contentColor = ParchmentText
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Text(
                text = "Pengaturan Detail Performa & Kontrol",
                style = MaterialTheme.typography.titleLarge,
                color = ParchmentText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = BasaltSurface,
                contentColor = WarmGoldBright,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CrimsonPrimary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Performa & Anti-Lag") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Kontrol & HUD") }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTab == 0) {
                    SectionLabel("Preset Profil")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            PerformancePreset.ULTRA_ANTI_LAG,
                            PerformancePreset.BALANCED,
                            PerformancePreset.HIGH_FIDELITY
                        ).forEach { preset ->
                            val selected = settings.preset == preset
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectPreset(preset) },
                                label = { Text(preset.label) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CrimsonContainer,
                                    selectedLabelColor = WarmGoldBright,
                                    containerColor = ElevatedBasalt,
                                    labelColor = AshMuted
                                )
                            )
                        }
                    }

                    SectionLabel("Backend Render Grafis")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RenderBackend.entries.forEach { backend ->
                            OptionChip(
                                title = backend.label,
                                subtitle = backend.subtitle,
                                selected = settings.renderBackend == backend,
                                onClick = { onSelectBackend(backend) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    SectionLabel("Resolusi Render Internal")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ResolutionScale.entries.forEach { res ->
                            OptionChip(
                                title = res.label,
                                subtitle = res.dimensions.substringBefore(" •"),
                                selected = settings.resolutionScale == res,
                                onClick = { onSelectResolution(res) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    SectionLabel("Target Frame Rate & Frameskip")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TargetFps.entries.forEach { fps ->
                            OptionChip(
                                title = "${fps.fps} FPS",
                                subtitle = if (fps == TargetFps.FPS_60) "Paling halus" else "Hemat baterai",
                                selected = settings.targetFps == fps,
                                onClick = { onSelectTargetFps(fps) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FrameskipLevel.entries.forEach { skip ->
                            OptionChip(
                                title = skip.label,
                                subtitle = "Frameskip",
                                selected = settings.frameskip == skip,
                                onClick = { onSelectFrameskip(skip) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    SectionLabel("Frekuensi CPU PSP Virtual")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CpuClockMode.entries.forEach { clock ->
                            OptionChip(
                                title = if (clock.mhz == 0) "Auto" else "${clock.mhz} MHz",
                                subtitle = when (clock) {
                                    CpuClockMode.AUTO -> "Adaptif"
                                    CpuClockMode.MHZ_222 -> "Hemat"
                                    CpuClockMode.MHZ_333 -> "Anti-Lag"
                                },
                                selected = settings.cpuClock == clock,
                                onClick = { onSelectCpuClock(clock) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    SectionLabel("Optimasi Shader & Memori Anti-Lag")
                    DetailSwitchItem(
                        title = "Lazy Texture Caching",
                        subtitle = "Hindari upload tekstur ulang di setiap frame untuk cegah stutter.",
                        checked = settings.lazyTextureCaching,
                        onCheckedChange = onToggleLazyTexture
                    )
                    DetailSwitchItem(
                        title = "Hardware Transform & Skinning",
                        subtitle = "Pindahkan kalkulasi tulang karakter ke GPU.",
                        checked = settings.hardwareTransform,
                        onCheckedChange = onToggleHardwareTransform
                    )
                    DetailSwitchItem(
                        title = "Matikan Efek Berat (Bloom / Blur)",
                        subtitle = "Sangat disarankan agar pertarungan massal tetap 60 FPS.",
                        checked = settings.disableHeavyEffects,
                        onCheckedChange = onToggleDisableHeavyEffects
                    )
                    DetailSwitchItem(
                        title = "Retain Changed Textures",
                        subtitle = "Simpan cache tekstur berubah untuk kurangi lag kamera.",
                        checked = settings.retainChangedTextures,
                        onCheckedChange = onToggleRetainTextures
                    )
                    DetailSwitchItem(
                        title = "Audio Low-Latency Buffer",
                        subtitle = "Sinkronkan efek suara tebasan & langkah tanpa delay.",
                        checked = settings.lowLatencyAudio,
                        onCheckedChange = onToggleLowLatencyAudio
                    )
                } else {
                    SectionLabel("Pratinjau Ikon Aksi Native")
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ObsidianBg,
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PreviewHudBadge(
                                icon = Icons.Rounded.Bolt,
                                label = "Pukul",
                                accent = CrimsonPrimary,
                                scale = settings.hudButtonScale,
                                opacity = settings.hudOpacity,
                                showLabel = settings.showActionLabels
                            )
                            PreviewHudBadge(
                                icon = Icons.Rounded.DirectionsRun,
                                label = "Lompat",
                                accent = WarmGoldBright,
                                scale = settings.hudButtonScale,
                                opacity = settings.hudOpacity,
                                showLabel = settings.showActionLabels
                            )
                            PreviewHudBadge(
                                icon = Icons.Rounded.Shield,
                                label = "Jongkok",
                                accent = ParchmentText,
                                scale = settings.hudButtonScale,
                                opacity = settings.hudOpacity,
                                showLabel = settings.showActionLabels
                            )
                            PreviewHudBadge(
                                icon = Icons.Rounded.Visibility,
                                label = "Eagle",
                                accent = AnimusCyan,
                                scale = settings.hudButtonScale,
                                opacity = settings.hudOpacity,
                                showLabel = settings.showActionLabels
                            )
                        }
                    }

                    SectionLabel("Skala Ukuran Tombol (${String.format("%.0f%%", settings.hudButtonScale * 100)})")
                    Slider(
                        value = settings.hudButtonScale,
                        onValueChange = onChangeHudScale,
                        valueRange = 0.75f..1.35f,
                        colors = SliderDefaults.colors(
                            thumbColor = WarmGoldBright,
                            activeTrackColor = CrimsonPrimary,
                            inactiveTrackColor = ElevatedBasalt
                        )
                    )

                    SectionLabel("Transparansi Tombol (${String.format("%.0f%%", settings.hudOpacity * 100)})")
                    Slider(
                        value = settings.hudOpacity,
                        onValueChange = onChangeHudOpacity,
                        valueRange = 0.35f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = WarmGoldBright,
                            activeTrackColor = CrimsonPrimary,
                            inactiveTrackColor = ElevatedBasalt
                        )
                    )

                    DetailSwitchItem(
                        title = "Getaran Haptic Saat Menekan Tombol",
                        subtitle = "Memberi umpan balik taktil saat menyerang atau menghindar.",
                        checked = settings.hapticFeedback,
                        onCheckedChange = onToggleHaptic
                    )

                    DetailSwitchItem(
                        title = "Tampilkan Label Nama Aksi",
                        subtitle = "Tampilkan teks Pukul, Jongkok, Lompat, Eagle di bawah ikon.",
                        checked = settings.showActionLabels,
                        onCheckedChange = onToggleShowLabels
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_close_settings_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text("Simpan & Tutup", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = WarmGoldBright
    )
}

@Composable
private fun OptionChip(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(58.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) CrimsonContainer else ElevatedBasalt,
        border = BorderStroke(1.dp, if (selected) CrimsonPrimary else CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) WarmGoldBright else ParchmentText,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = AshMuted,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DetailSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ElevatedBasalt)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = ParchmentText
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = AshMuted
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = WarmGoldBright,
                checkedTrackColor = CrimsonPrimary,
                uncheckedThumbColor = AshMuted,
                uncheckedTrackColor = BasaltSurface
            )
        )
    }
}

@Composable
private fun PreviewHudBadge(
    icon: ImageVector,
    label: String,
    accent: Color,
    scale: Float,
    opacity: Float,
    showLabel: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale.coerceIn(0.75f, 1.15f))
            .alpha(opacity)
    ) {
        Surface(
            shape = CircleShape,
            color = BasaltSurface,
            border = BorderStroke(1.5.dp, accent.copy(alpha = 0.8f)),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = label, tint = accent)
            }
        }
        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = ParchmentText)
        }
    }
}
