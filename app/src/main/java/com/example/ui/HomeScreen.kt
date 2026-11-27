package com.example.ui

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DiscFull
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ImportProgressState
import com.example.data.ImportedIsoInfo
import com.example.data.PerformancePreset
import com.example.data.PerformanceSettings
import com.example.data.TargetFps
import com.example.ui.theme.AnimusCyan
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.AshMuted
import com.example.ui.theme.BasaltSurface
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ElevatedBasalt
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldReady
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ParchmentText
import com.example.ui.theme.WarmGoldBright

@Composable
fun HomeScreen(
    uiState: LauncherUiState,
    onPickIsoClick: () -> Unit,
    onOpenGuideClick: () -> Unit,
    onDeleteIsoRequest: () -> Unit,
    onConfirmDeleteIso: () -> Unit,
    onDismissDeleteIso: () -> Unit,
    onDismissImportError: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    onSelectPreset: (PerformancePreset) -> Unit,
    onToggleDisableHeavyEffects: (Boolean) -> Unit,
    onToggleLazyTexture: (Boolean) -> Unit,
    onToggleTarget60Fps: (Boolean) -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 720.dp

            Column(modifier = Modifier.fillMaxSize()) {
                if (isWideScreen) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            HeroGameHeader(isoInfo = uiState.isoInfo)
                            IsoVaultCard(
                                isoInfo = uiState.isoInfo,
                                importState = uiState.importState,
                                onPickIsoClick = onPickIsoClick,
                                onOpenGuideClick = onOpenGuideClick,
                                onDeleteIsoRequest = onDeleteIsoRequest,
                                onDismissImportError = onDismissImportError
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            PrePlayAntiLagCard(
                                settings = uiState.settings,
                                onSelectPreset = onSelectPreset,
                                onToggleDisableHeavyEffects = onToggleDisableHeavyEffects,
                                onToggleLazyTexture = onToggleLazyTexture,
                                onToggleTarget60Fps = onToggleTarget60Fps,
                                onOpenSettingsClick = onOpenSettingsClick
                            )
                            ControlPreviewSummaryCard(
                                settings = uiState.settings,
                                onOpenSettingsClick = onOpenSettingsClick
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HeroGameHeader(isoInfo = uiState.isoInfo)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 600.dp)
                                .align(Alignment.CenterHorizontally)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            IsoVaultCard(
                                isoInfo = uiState.isoInfo,
                                importState = uiState.importState,
                                onPickIsoClick = onPickIsoClick,
                                onOpenGuideClick = onOpenGuideClick,
                                onDeleteIsoRequest = onDeleteIsoRequest,
                                onDismissImportError = onDismissImportError
                            )

                            PrePlayAntiLagCard(
                                settings = uiState.settings,
                                onSelectPreset = onSelectPreset,
                                onToggleDisableHeavyEffects = onToggleDisableHeavyEffects,
                                onToggleLazyTexture = onToggleLazyTexture,
                                onToggleTarget60Fps = onToggleTarget60Fps,
                                onOpenSettingsClick = onOpenSettingsClick
                            )

                            ControlPreviewSummaryCard(
                                settings = uiState.settings,
                                onOpenSettingsClick = onOpenSettingsClick
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                StickyPlayBottomBar(
                    isoInstalled = uiState.isoInfo.isInstalled,
                    isImporting = uiState.importState is ImportProgressState.Copying ||
                        uiState.importState is ImportProgressState.Verifying,
                    settings = uiState.settings,
                    onPlayClick = onPlayClick,
                    onPickIsoClick = onPickIsoClick
                )
            }
        }
    }

    if (uiState.showDeleteIsoConfirm) {
        AlertDialog(
            onDismissRequest = onDismissDeleteIso,
            containerColor = BasaltSurface,
            titleContentColor = ParchmentText,
            textContentColor = AshMuted,
            title = { Text("Hapus File ISO dari Aplikasi?") },
            text = {
                Text(
                    "File ${uiState.isoInfo.fileName} (${uiState.isoInfo.formattedSizeMb}) akan dihapus dari memori internal aplikasi untuk mengosongkan ruang penyimpanan."
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmDeleteIso,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteIso) {
                    Text("Batal", color = AshMuted)
                }
            }
        )
    }
}

@Composable
private fun HeroGameHeader(isoInfo: ImportedIsoInfo) {
    val extractedPic1 = remember(isoInfo.pic1Path) {
        isoInfo.pic1Path?.let { runCatching { BitmapFactory.decodeFile(it)?.asImageBitmap() }.getOrNull() }
    }
    val extractedIcon0 = remember(isoInfo.iconPath) {
        isoInfo.iconPath?.let { runCatching { BitmapFactory.decodeFile(it)?.asImageBitmap() }.getOrNull() }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(258.dp)
    ) {
        if (extractedPic1 != null) {
            Image(
                bitmap = extractedPic1,
                contentDescription = "Latar game Assassin's Creed: Bloodlines",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_cyprus_hero),
                contentDescription = "Ilustrasi benteng Mediterania Cyprus untuk Assassin's Creed: Bloodlines",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ObsidianBg.copy(alpha = 0.35f),
                            ObsidianBg.copy(alpha = 0.55f),
                            ObsidianBg
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = ObsidianBg.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ScreenRotation,
                            contentDescription = null,
                            tint = WarmGoldBright,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "NATIVE PSP LAUNCHER",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarmGoldBright
                        )
                    }
                }

                StatusPill(isReady = isoInfo.isInstalled)
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = BasaltSurface,
                    border = BorderStroke(1.5.dp, AntiqueGold.copy(alpha = 0.65f)),
                    shadowElevation = 8.dp
                ) {
                    if (extractedIcon0 != null) {
                        Image(
                            bitmap = extractedIcon0,
                            contentDescription = "Ikon UMD disc",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_art),
                            contentDescription = "Emblem Bloodlines",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isoInfo.isInstalled && isoInfo.discTitle.isNotBlank()) {
                            isoInfo.discTitle
                        } else {
                            "Assassin's Creed: Bloodlines"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        color = ParchmentText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isoInfo.isInstalled) {
                            "${isoInfo.discId.ifEmpty { "PSP UMD" }} • ${isoInfo.formattedSizeMb} Internal"
                        } else {
                            "Edisi Eropa (PSP / PSN) • Auto Landscape"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = AshMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPill(isReady: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isReady) EmeraldContainer.copy(alpha = 0.9f) else GoldContainer.copy(alpha = 0.9f),
        border = BorderStroke(
            1.dp,
            if (isReady) EmeraldReady.copy(alpha = 0.6f) else AntiqueGold.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (isReady) EmeraldReady else WarmGoldBright)
            )
            Text(
                text = if (isReady) "SIAP MAIN" else "BUTUH ISO",
                style = MaterialTheme.typography.labelSmall,
                color = if (isReady) EmeraldReady else WarmGoldBright
            )
        }
    }
}

@Composable
private fun IsoVaultCard(
    isoInfo: ImportedIsoInfo,
    importState: ImportProgressState,
    onPickIsoClick: () -> Unit,
    onOpenGuideClick: () -> Unit,
    onDeleteIsoRequest: () -> Unit,
    onDismissImportError: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = BasaltSurface),
        border = BorderStroke(
            1.dp,
            if (isoInfo.isInstalled) EmeraldReady.copy(alpha = 0.35f) else CardBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isoInfo.isInstalled) EmeraldContainer else CrimsonContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isoInfo.isInstalled) Icons.Rounded.CheckCircle else Icons.Rounded.Storage,
                            contentDescription = null,
                            tint = if (isoInfo.isInstalled) EmeraldReady else CrimsonPrimary
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isoInfo.isInstalled) {
                            "Data Game Tersimpan di APK"
                        } else {
                            "Masukkan File ISO Game"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = ParchmentText
                    )
                    Text(
                        text = if (isoInfo.isInstalled) {
                            "Tersimpan permanen di memori internal — langsung main tanpa pilih folder lagi."
                        } else {
                            "Import 1x ke internal aplikasi (~500–600 MB). Tidak perlu pilih folder berulang."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = AshMuted
                    )
                }
            }

            AnimatedVisibility(visible = importState is ImportProgressState.Error) {
                val errMsg = (importState as? ImportProgressState.Error)?.message.orEmpty()
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CrimsonContainer,
                    border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = CrimsonPrimary
                        )
                        Text(
                            text = errMsg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ParchmentText,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onDismissImportError) {
                            Text("Tutup", color = WarmGoldBright)
                        }
                    }
                }
            }

            when (importState) {
                is ImportProgressState.Copying -> {
                    val animatedProgress by animateFloatAsState(
                        targetValue = importState.fraction,
                        label = "iso_copy_progress"
                    )
                    val copiedMb = String.format("%.1f", importState.copiedBytes / (1024.0 * 1024.0))
                    val totalMb = String.format("%.1f", importState.totalBytes / (1024.0 * 1024.0))
                    val pct = (importState.fraction * 100).toInt()
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Menyalin ISO ke aplikasi... $pct%",
                                style = MaterialTheme.typography.labelLarge,
                                color = WarmGoldBright
                            )
                            Text(
                                text = "$copiedMb / $totalMb MB (${String.format("%.1f", importState.speedMbps)} MB/s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = AshMuted
                            )
                        }
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50)),
                            color = CrimsonPrimary,
                            trackColor = ElevatedBasalt
                        )
                    }
                }

                is ImportProgressState.Verifying -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Memverifikasi struktur UMD ISO-9660 & PARAM.SFO...",
                            style = MaterialTheme.typography.labelLarge,
                            color = AnimusCyan
                        )
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50)),
                            color = AnimusCyan,
                            trackColor = ElevatedBasalt
                        )
                    }
                }

                else -> {
                    if (isoInfo.isInstalled) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ElevatedBasalt,
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = isoInfo.fileName,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ParchmentText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = isoInfo.format.ifEmpty { "ISO" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WarmGoldBright
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Ukuran Vault: ${isoInfo.formattedSizeMb}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldReady
                                    )
                                    Text(
                                        text = "ID: ${isoInfo.discId.ifEmpty { "PSP-UMD" }}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AshMuted
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onPickIsoClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("replace_iso_button"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SwapHoriz,
                                    contentDescription = null,
                                    tint = ParchmentText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ganti File ISO", color = ParchmentText)
                            }

                            OutlinedButton(
                                onClick = onDeleteIsoRequest,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("delete_iso_button"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Hapus file ISO",
                                    tint = CrimsonPrimary
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ElevatedBasalt,
                            border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FolderZip,
                                    contentDescription = null,
                                    tint = AntiqueGold
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Target File: Assassins Creed - Bloodlines (Europe) (PSP)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ParchmentText
                                    )
                                    Text(
                                        text = "Unduh terpisah dari web lalu ekstrak ke .iso / .cso (~500–600 MB).",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AshMuted
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onPickIsoClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("import_iso_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.UploadFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Import File ISO")
                            }

                            OutlinedButton(
                                onClick = onOpenGuideClick,
                                modifier = Modifier
                                    .height(50.dp)
                                    .testTag("iso_guide_button"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.55f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                                    contentDescription = null,
                                    tint = WarmGoldBright,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cari ISO", color = WarmGoldBright)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrePlayAntiLagCard(
    settings: PerformanceSettings,
    onSelectPreset: (PerformancePreset) -> Unit,
    onToggleDisableHeavyEffects: (Boolean) -> Unit,
    onToggleLazyTexture: (Boolean) -> Unit,
    onToggleTarget60Fps: (Boolean) -> Unit,
    onOpenSettingsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = BasaltSurface),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = null,
                        tint = WarmGoldBright
                    )
                    Column {
                        Text(
                            text = "Pengaturan Anti-Lag Sebelum Main",
                            style = MaterialTheme.typography.titleMedium,
                            color = ParchmentText
                        )
                        Text(
                            text = "${settings.renderBackend.label} • ${settings.resolutionScale.label} • ${settings.targetFps.fps} FPS",
                            style = MaterialTheme.typography.labelSmall,
                            color = AnimusCyan
                        )
                    }
                }

                IconButton(
                    onClick = onOpenSettingsClick,
                    modifier = Modifier.testTag("open_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "Buka pengaturan detail",
                        tint = WarmGoldBright
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
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
                        label = {
                            Text(
                                text = preset.label,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        leadingIcon = if (selected) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.Bolt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrimsonContainer,
                            selectedLabelColor = WarmGoldBright,
                            selectedLeadingIconColor = WarmGoldBright,
                            containerColor = ElevatedBasalt,
                            labelColor = AshMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = CardBorder,
                            selectedBorderColor = CrimsonPrimary
                        )
                    )
                }
            }

            QuickToggleRow(
                icon = Icons.Rounded.GraphicEq,
                title = "Matikan Efek Berat (Bloom/Blur)",
                subtitle = "Mengurangi drop FPS saat bertarung di kota",
                checked = settings.disableHeavyEffects,
                onCheckedChange = onToggleDisableHeavyEffects,
                testTag = "toggle_heavy_effects"
            )

            QuickToggleRow(
                icon = Icons.Rounded.Memory,
                title = "Lazy Texture Caching",
                subtitle = "Mencegah patah-patah saat kamera berputar cepat",
                checked = settings.lazyTextureCaching,
                onCheckedChange = onToggleLazyTexture,
                testTag = "toggle_lazy_texture"
            )

            QuickToggleRow(
                icon = Icons.Rounded.Bolt,
                title = "Target 60 FPS Smooth",
                subtitle = "Matikan ke 30 FPS jika perangkat terasa panas",
                checked = settings.targetFps == TargetFps.FPS_60,
                onCheckedChange = onToggleTarget60Fps,
                testTag = "toggle_60fps"
            )

            OutlinedButton(
                onClick = onOpenSettingsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("open_detail_settings_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = null,
                    tint = WarmGoldBright,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Atur Detail Grafis, CPU Clock & Kontrol",
                    color = ParchmentText
                )
            }
        }
    }
}

@Composable
private fun QuickToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ElevatedBasalt)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) WarmGoldBright else AshMuted,
            modifier = Modifier.size(20.dp)
        )
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
            modifier = Modifier.testTag(testTag),
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
private fun ControlPreviewSummaryCard(
    settings: PerformanceSettings,
    onOpenSettingsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = BasaltSurface),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GoldContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Gamepad,
                        contentDescription = null,
                        tint = WarmGoldBright
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Kontrol Aksi Native (Bukan Simbol Emulator)",
                    style = MaterialTheme.typography.titleMedium,
                    color = ParchmentText
                )
                Text(
                    text = "Analog halus + ikon Pukul, Lompat, Jongkok, Eagle Vision • Skala ${String.format("%.0f%%", settings.hudButtonScale * 100)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AshMuted
                )
            }
            TextButton(onClick = onOpenSettingsClick) {
                Text("Ubah", color = WarmGoldBright)
            }
        }
    }
}

@Composable
private fun StickyPlayBottomBar(
    isoInstalled: Boolean,
    isImporting: Boolean,
    settings: PerformanceSettings,
    onPlayClick: () -> Unit,
    onPickIsoClick: () -> Unit
) {
    Surface(
        color = BasaltSurface,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    if (isoInstalled) onPlayClick() else onPickIsoClick()
                },
                enabled = !isImporting,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .height(58.dp)
                    .testTag("play_button"),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isoInstalled) CrimsonPrimary else ElevatedBasalt,
                    contentColor = ParchmentText
                )
            ) {
                Icon(
                    imageVector = if (isoInstalled) Icons.Rounded.PlayArrow else Icons.Rounded.DiscFull,
                    contentDescription = null,
                    tint = if (isoInstalled) WarmGoldBright else AntiqueGold,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = if (isoInstalled) "MAIN SEKARANG" else "MASUKKAN ISO DULU",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ParchmentText
                    )
                    Text(
                        text = if (isoInstalled) {
                            "Auto Landscape • ${settings.preset.label} • ${settings.targetFps.fps} FPS"
                        } else {
                            "Pilih file .iso / .cso untuk disimpan ke dalam aplikasi"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isoInstalled) ParchmentText.copy(alpha = 0.85f) else AshMuted
                    )
                }
            }
        }
    }
}
