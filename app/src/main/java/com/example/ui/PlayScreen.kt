package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowDown
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowUp
import androidx.compose.material.icons.rounded.Launch
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.ImportedIsoInfo
import com.example.data.PerformancePreset
import com.example.data.PerformanceSettings
import com.example.ui.theme.AnimusCyan
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.AshMuted
import com.example.ui.theme.BasaltSurface
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ElevatedBasalt
import com.example.ui.theme.EmeraldReady
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ParchmentText
import com.example.ui.theme.WarmGoldBright
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun PlayScreen(
    isoInfo: ImportedIsoInfo,
    settings: PerformanceSettings,
    hasExternalCore: Boolean,
    onSelectPreset: (PerformancePreset) -> Unit,
    onOpenSettings: () -> Unit,
    onLaunchExternalCore: () -> Unit,
    onExitPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Enforce Auto-Landscape & Fullscreen Immersive Mode while in PlayScreen
    DisposableEffect(Unit) {
        val activity = context.findActivity()
        val window = activity?.window
        val originalOrientation = activity?.requestedOrientation
            ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            activity?.requestedOrientation = originalOrientation
            if (window != null) {
                WindowCompat.getInsetsController(window, window.decorView)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    var isPaused by remember { mutableStateOf(false) }

    BackHandler {
        if (isPaused) {
            onExitPlay()
        } else {
            isPaused = true
        }
    }

    // Interactive character & action state driven by the native controls
    var joyInput by remember { mutableStateOf(Offset.Zero) }
    var isSprinting by remember { mutableStateOf(false) }
    var isBlocking by remember { mutableStateOf(false) }
    var isCrouching by remember { mutableStateOf(false) }
    var eagleVisionTimer by remember { mutableFloatStateOf(0f) }
    var attackTimer by remember { mutableFloatStateOf(0f) }

    // World coordinates (normalized 0f..1f horizontally)
    var playerX by remember { mutableFloatStateOf(0.32f) }
    var playerYOffset by remember { mutableFloatStateOf(0f) } // negative = up in air
    var playerVelY by remember { mutableFloatStateOf(0f) }
    var facingRight by remember { mutableStateOf(true) }
    var guardX by remember { mutableFloatStateOf(0.68f) }
    var guardDir by remember { mutableFloatStateOf(-1f) }
    var guardStunTimer by remember { mutableFloatStateOf(0f) }
    var animClock by remember { mutableFloatStateOf(0f) }
    var currentFps by remember { mutableIntStateOf(settings.targetFps.fps) }
    var lastStatusAction by remember { mutableStateOf("SIAGA") }

    // Frame-paced game loop honoring TargetFps (60 FPS vs 30 FPS)
    LaunchedEffect(isPaused, settings.targetFps) {
        val targetFrameMs = if (settings.targetFps.fps >= 60) 16L else 33L
        var frameCounter = 0
        var fpsWindowStart = System.currentTimeMillis()
        var lastTick = System.nanoTime()

        while (true) {
            delay(targetFrameMs)
            val nowNano = System.nanoTime()
            val dt = ((nowNano - lastTick) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
            lastTick = nowNano

            frameCounter++
            val nowMs = System.currentTimeMillis()
            if (nowMs - fpsWindowStart >= 500L) {
                val measured = ((frameCounter * 1000f) / (nowMs - fpsWindowStart)).roundToInt()
                currentFps = measured.coerceIn(1, settings.targetFps.fps)
                frameCounter = 0
                fpsWindowStart = nowMs
            }

            if (isPaused) continue

            animClock += dt

            // Horizontal movement from joystick
            val speedBase = when {
                isCrouching -> 0.14f
                isSprinting -> 0.38f
                else -> 0.24f
            }
            if (abs(joyInput.x) > 0.08f) {
                playerX = (playerX + joyInput.x * speedBase * dt).coerceIn(0.08f, 0.92f)
                facingRight = joyInput.x > 0f
            }

            // Vertical jump physics
            if (playerYOffset < 0f || playerVelY < 0f) {
                playerYOffset += playerVelY * dt
                playerVelY += 2.2f * dt // gravity
                if (playerYOffset >= 0f) {
                    playerYOffset = 0f
                    playerVelY = 0f
                }
            }

            // Guard patrol
            if (guardStunTimer > 0f) {
                guardStunTimer = (guardStunTimer - dt).coerceAtLeast(0f)
            } else {
                guardX += guardDir * 0.09f * dt
                if (guardX > 0.84f) guardDir = -1f
                if (guardX < 0.54f) guardDir = 1f
            }

            if (attackTimer > 0f) attackTimer = (attackTimer - dt).coerceAtLeast(0f)
            if (eagleVisionTimer > 0f) eagleVisionTimer = (eagleVisionTimer - dt).coerceAtLeast(0f)
        }
    }

    fun triggerHaptic() {
        if (settings.hapticFeedback) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        // Cyprus Rooftop Interactive Viewport
        CyprusGameplayCanvas(
            playerX = playerX,
            playerYOffset = playerYOffset,
            facingRight = facingRight,
            isMoving = abs(joyInput.x) > 0.1f,
            isSprinting = isSprinting,
            isCrouching = isCrouching,
            isBlocking = isBlocking,
            attackProgress = attackTimer / 0.28f,
            eagleVisionActive = eagleVisionTimer > 0f,
            guardX = guardX,
            guardStunned = guardStunTimer > 0f,
            animClock = animClock,
            disableHeavyEffects = settings.disableHeavyEffects,
            modifier = Modifier.fillMaxSize()
        )

        // Top Minimal Console HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.displayCutout)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animus Sync Bar + Active Stance
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ObsidianBg.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Sync Diamond
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                if (eagleVisionTimer > 0f) AnimusCyan else CrimsonPrimary,
                                RoundedCornerShape(3.dp)
                            )
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            repeat(6) { idx ->
                                Box(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (idx < 5) ParchmentText else ParchmentText.copy(alpha = 0.3f)
                                        )
                                )
                            }
                        }
                        Text(
                            text = "${isoInfo.discId.ifEmpty { "ULES01367" }} • $lastStatusAction",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (eagleVisionTimer > 0f) AnimusCyan else WarmGoldBright
                        )
                    }
                }
            }

            // Telemetry & Pause Trigger
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = ObsidianBg.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Text(
                        text = "$currentFps FPS • ${settings.renderBackend.label} • ${settings.resolutionScale.label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldReady,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = ObsidianBg.copy(alpha = 0.78f),
                    border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.55f))
                ) {
                    IconButton(
                        onClick = { isPaused = !isPaused },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("pause_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Pause,
                            contentDescription = "Jeda & Pengaturan",
                            tint = ParchmentText
                        )
                    }
                }
            }
        }

        // Bottom Left Cluster: Virtual Analog Joystick + Sprint / Lock-Block Pills
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .windowInsetsPadding(WindowInsets.displayCutout)
                .padding(start = 22.dp, bottom = 18.dp)
                .alpha(settings.hudOpacity)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TriggerPillButton(
                        icon = Icons.Rounded.DirectionsRun,
                        label = "Lari",
                        active = isSprinting,
                        accent = WarmGoldBright,
                        showLabel = settings.showActionLabels,
                        onClick = {
                            triggerHaptic()
                            isSprinting = !isSprinting
                            if (isSprinting) isCrouching = false
                            lastStatusAction = if (isSprinting) "LARI CEPAT" else "SIAGA"
                        },
                        testTag = "btn_sprint"
                    )
                    TriggerPillButton(
                        icon = Icons.Rounded.Shield,
                        label = "Tangkis",
                        active = isBlocking,
                        accent = AnimusCyan,
                        showLabel = settings.showActionLabels,
                        onClick = {
                            triggerHaptic()
                            isBlocking = !isBlocking
                            lastStatusAction = if (isBlocking) "TANGKIS" else "SIAGA"
                        },
                        testTag = "btn_block"
                    )
                }

                VirtualAnalogStick(
                    scale = settings.hudButtonScale,
                    onMove = { offset -> joyInput = offset }
                )
            }
        }

        // Bottom Right Cluster: Native Action Buttons (Pukul, Lompat, Jongkok, Eagle Vision)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(WindowInsets.displayCutout)
                .padding(end = 24.dp, bottom = 18.dp)
                .alpha(settings.hudOpacity)
                .scale(settings.hudButtonScale)
        ) {
            Box(modifier = Modifier.size(188.dp)) {
                // Top-Left: Eagle Vision (maps to Triangle)
                NativeActionButton(
                    icon = Icons.Rounded.Visibility,
                    label = "Eagle",
                    accent = AnimusCyan,
                    active = eagleVisionTimer > 0f,
                    showLabel = settings.showActionLabels,
                    onAction = {
                        triggerHaptic()
                        eagleVisionTimer = if (eagleVisionTimer > 0f) 0f else 5.0f
                        lastStatusAction = if (eagleVisionTimer > 0f) "EAGLE VISION" else "SIAGA"
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = (-18).dp),
                    testTag = "btn_eagle_vision"
                )

                // Right-Top: Jongkok / Stealth (maps to Circle)
                NativeActionButton(
                    icon = Icons.Rounded.KeyboardDoubleArrowDown,
                    label = "Jongkok",
                    accent = ParchmentText,
                    active = isCrouching,
                    showLabel = settings.showActionLabels,
                    onAction = {
                        triggerHaptic()
                        isCrouching = !isCrouching
                        if (isCrouching) isSprinting = false
                        lastStatusAction = if (isCrouching) "JONGKOK STEALTH" else "BERDIRI"
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(y = (-12).dp),
                    testTag = "btn_crouch"
                )

                // Left-Center: Pukul / Hidden Blade (maps to Square — prominent primary strike)
                NativeActionButton(
                    icon = Icons.Rounded.Bolt,
                    label = "Pukul",
                    accent = CrimsonPrimary,
                    active = attackTimer > 0f,
                    isPrimary = true,
                    showLabel = settings.showActionLabels,
                    onAction = {
                        triggerHaptic()
                        attackTimer = 0.28f
                        isCrouching = false
                        isBlocking = false
                        if (abs(playerX - guardX) < 0.14f) {
                            guardStunTimer = 1.6f
                            lastStatusAction = "SERANGAN MASUK!"
                        } else {
                            lastStatusAction = "TEBASAN BILAH"
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(y = 10.dp),
                    testTag = "btn_attack"
                )

                // Bottom-Right: Lompat / Parkour (maps to Cross)
                NativeActionButton(
                    icon = Icons.Rounded.KeyboardDoubleArrowUp,
                    label = "Lompat",
                    accent = WarmGoldBright,
                    active = playerYOffset < -0.01f,
                    showLabel = settings.showActionLabels,
                    onAction = {
                        triggerHaptic()
                        if (playerYOffset >= -0.005f) {
                            isCrouching = false
                            playerVelY = -0.82f
                            lastStatusAction = "LOMPAT PARKOUR"
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(x = 18.dp),
                    testTag = "btn_jump"
                )
            }
        }

        // Pause & Vault Core Overlay
        AnimatedVisibility(
            visible = isPaused,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBg.copy(alpha = 0.84f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .widthIn(max = 540.dp)
                        .padding(20.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = BasaltSurface),
                    border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = isoInfo.discTitle.ifEmpty { "Assassin's Creed: Bloodlines" },
                                    style = MaterialTheme.typography.titleLarge,
                                    color = ParchmentText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "ISO Internal: ${isoInfo.fileName} (${isoInfo.formattedSizeMb})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AshMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = onOpenSettings) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = "Pengaturan Detail",
                                    tint = WarmGoldBright
                                )
                            }
                        }

                        // Instant Anti-Lag switcher inside Pause
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
                                    label = { Text(preset.label, maxLines = 1) },
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

                        if (hasExternalCore) {
                            OutlinedButton(
                                onClick = onLaunchExternalCore,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("launch_ppsspp_core_button"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, AnimusCyan.copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Launch,
                                    contentDescription = null,
                                    tint = AnimusCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Jalankan ISO via Core PPSSPP Terpasang",
                                    color = AnimusCyan
                                )
                            }
                        } else {
                            Text(
                                text = "Catatan: Launcher ini menyimpan & memverifikasi ISO Anda di memori internal serta menyediakan kontrol native. Untuk eksekusi penuh instruksi MIPS PSP UMD dari ISO, pasang core runtime PPSSPP di perangkat untuk handoff otomatis.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AshMuted
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onExitPlay,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("exit_play_button"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                                    contentDescription = null,
                                    tint = ParchmentText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Beranda", color = ParchmentText)
                            }

                            Button(
                                onClick = { isPaused = false },
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(50.dp)
                                    .testTag("resume_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = WarmGoldBright
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Lanjut Main", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VirtualAnalogStick(
    scale: Float,
    onMove: (Offset) -> Unit
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    val outerRadiusDp = (64 * scale.coerceIn(0.8f, 1.25f)).dp

    Box(
        modifier = Modifier
            .size(outerRadiusDp * 2)
            .pointerInput(Unit) {
                val maxDist = size.width * 0.34f
                detectDragGestures(
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onMove(Offset.Zero)
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        onMove(Offset.Zero)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val next = knobOffset + dragAmount
                        val dist = hypot(next.x, next.y)
                        val clamped = if (dist > maxDist && dist > 0f) {
                            next * (maxDist / dist)
                        } else {
                            next
                        }
                        knobOffset = clamped
                        onMove(Offset(clamped.x / maxDist, clamped.y / maxDist))
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            drawCircle(
                color = ObsidianBg.copy(alpha = 0.55f),
                radius = r - 2.dp.toPx()
            )
            drawCircle(
                color = AntiqueGold.copy(alpha = 0.4f),
                radius = r - 2.dp.toPx(),
                style = Stroke(width = 2.dp.toPx())
            )
            // Subtle directional ticks
            for (angleDeg in listOf(0f, 90f, 180f, 270f)) {
                val rad = angleDeg * (PI / 180.0)
                val inner = r * 0.72f
                val outer = r * 0.88f
                drawLine(
                    color = AshMuted.copy(alpha = 0.45f),
                    start = Offset(
                        center.x + (cos(rad) * inner).toFloat(),
                        center.y + (sin(rad) * inner).toFloat()
                    ),
                    end = Offset(
                        center.x + (cos(rad) * outer).toFloat(),
                        center.y + (sin(rad) * outer).toFloat()
                    ),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Draggable thumbstick cap
        Surface(
            modifier = Modifier
                .offset { IntOffset(knobOffset.x.roundToInt(), knobOffset.y.roundToInt()) }
                .size(54.dp),
            shape = CircleShape,
            color = BasaltSurface.copy(alpha = 0.92f),
            border = BorderStroke(
                2.dp,
                if (knobOffset == Offset.Zero) AntiqueGold.copy(alpha = 0.75f) else CrimsonPrimary
            ),
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (knobOffset == Offset.Zero) WarmGoldBright.copy(alpha = 0.7f) else CrimsonPrimary
                        )
                )
            }
        }
    }
}

@Composable
private fun TriggerPillButton(
    icon: ImageVector,
    label: String,
    active: Boolean,
    accent: Color,
    showLabel: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .height(48.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(50),
        color = if (active) accent.copy(alpha = 0.25f) else ObsidianBg.copy(alpha = 0.72f),
        border = BorderStroke(1.5.dp, if (active) accent else CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) accent else ParchmentText,
                modifier = Modifier.size(18.dp)
            )
            if (showLabel) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) accent else ParchmentText
                )
            }
        }
    }
}

@Composable
private fun NativeActionButton(
    icon: ImageVector,
    label: String,
    accent: Color,
    active: Boolean,
    showLabel: Boolean,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    testTag: String
) {
    var pressed by remember { mutableStateOf(false) }
    val buttonSize = if (isPrimary) 64.dp else 56.dp

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(buttonSize)
                .minimumInteractiveComponentSize()
                .scale(if (pressed || active) 0.92f else 1f)
                .testTag(testTag)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            pressed = true
                            onAction()
                            tryAwaitRelease()
                            pressed = false
                        }
                    )
                },
            shape = CircleShape,
            color = if (active || pressed) {
                accent.copy(alpha = 0.32f)
            } else {
                ObsidianBg.copy(alpha = 0.76f)
            },
            border = BorderStroke(
                width = if (isPrimary) 2.dp else 1.5.dp,
                color = if (active || pressed) accent else accent.copy(alpha = 0.72f)
            ),
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (active || pressed) WarmGoldBright else accent,
                    modifier = Modifier.size(if (isPrimary) 30.dp else 24.dp)
                )
            }
        }
        if (showLabel) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ParchmentText
            )
        }
    }
}

@Composable
private fun CyprusGameplayCanvas(
    playerX: Float,
    playerYOffset: Float,
    facingRight: Boolean,
    isMoving: Boolean,
    isSprinting: Boolean,
    isCrouching: Boolean,
    isBlocking: Boolean,
    attackProgress: Float,
    eagleVisionActive: Boolean,
    guardX: Float,
    guardStunned: Boolean,
    animClock: Float,
    disableHeavyEffects: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val roofY = h * 0.74f

        // 1. Mediterranean Twilight Sky (shifts to Animus Cyan-Indigo during Eagle Vision)
        val skyTop = if (eagleVisionActive) Color(0xFF041426) else Color(0xFF0A0D16)
        val skyBottom = if (eagleVisionActive) Color(0xFF0D2E4D) else Color(0xFF211A28)
        drawRect(
            brush = Brush.verticalGradient(colors = listOf(skyTop, skyBottom)),
            size = size
        )

        // Moon / Animus Pulse
        val moonCenter = Offset(w * 0.78f, h * 0.22f)
        if (!disableHeavyEffects) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        (if (eagleVisionActive) AnimusCyan else WarmGoldBright).copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = moonCenter,
                    radius = h * 0.28f
                ),
                center = moonCenter,
                radius = h * 0.28f
            )
        }
        drawCircle(
            color = if (eagleVisionActive) AnimusCyan.copy(alpha = 0.7f) else Color(0xFFEBD9B4),
            radius = h * 0.055f,
            center = moonCenter
        )

        // 2. Distant Cyprus Harbor Silhouette & Fortress Towers
        val distantColor = if (eagleVisionActive) Color(0xFF0A243C) else Color(0xFF131722)
        val towerPositions = listOf(0.10f, 0.28f, 0.52f, 0.86f)
        towerPositions.forEachIndexed { idx, xFrac ->
            val tw = w * (0.06f + (idx % 2) * 0.03f)
            val th = h * (0.28f + (idx % 3) * 0.08f)
            drawRect(
                color = distantColor,
                topLeft = Offset(w * xFrac, roofY - th),
                size = Size(tw, th)
            )
        }

        // 3. Stone Rooftop Parapet Foreground
        val stoneColor = if (eagleVisionActive) Color(0xFF102A42) else Color(0xFF1B202B)
        drawRect(
            color = stoneColor,
            topLeft = Offset(0f, roofY),
            size = Size(w, h - roofY)
        )
        // Parapet battlements
        val battlementCount = 12
        val step = w / battlementCount
        for (i in 0 until battlementCount) {
            if (i % 2 == 0) {
                drawRect(
                    color = if (eagleVisionActive) Color(0xFF163856) else Color(0xFF252C3B),
                    topLeft = Offset(i * step + 4f, roofY - 14.dp.toPx()),
                    size = Size(step - 8f, 14.dp.toPx())
                )
            }
        }
        drawLine(
            color = if (eagleVisionActive) AnimusCyan.copy(alpha = 0.5f) else AntiqueGold.copy(alpha = 0.3f),
            start = Offset(0f, roofY),
            end = Offset(w, roofY),
            strokeWidth = 2.dp.toPx()
        )

        // Torches on rooftop (bloom skipped when disableHeavyEffects == true)
        listOf(0.18f, 0.82f).forEach { tx ->
            val torchPos = Offset(w * tx, roofY - 44.dp.toPx())
            drawLine(
                color = Color(0xFF3E352B),
                start = Offset(torchPos.x, roofY),
                end = torchPos,
                strokeWidth = 4.dp.toPx()
            )
            if (!disableHeavyEffects) {
                val flicker = 1f + 0.1f * sin(animClock * 11f + tx * 10f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(WarmGoldBright.copy(alpha = 0.45f), Color.Transparent),
                        center = torchPos,
                        radius = 48.dp.toPx() * flicker
                    ),
                    center = torchPos,
                    radius = 48.dp.toPx() * flicker
                )
            }
            drawCircle(
                color = WarmGoldBright,
                radius = 5.dp.toPx(),
                center = torchPos
            )
        }

        // 4. Patrolling Cyprus Templar Guard (highlighted Crimson in Eagle Vision!)
        val gx = w * guardX
        val gColor = when {
            guardStunned -> AshMuted
            eagleVisionActive -> CrimsonPrimary
            else -> Color(0xFF545E70)
        }
        val gHeight = 62.dp.toPx()
        val guardTop = roofY - gHeight
        if (eagleVisionActive && !guardStunned) {
            drawCircle(
                color = CrimsonPrimary.copy(alpha = 0.35f),
                radius = 42.dp.toPx(),
                center = Offset(gx, roofY - gHeight * 0.5f)
            )
        }
        // Guard torso & helm
        drawRoundRect(
            color = gColor,
            topLeft = Offset(gx - 12.dp.toPx(), guardTop + 16.dp.toPx()),
            size = Size(24.dp.toPx(), 30.dp.toPx()),
            cornerRadius = CornerRadius(6.dp.toPx())
        )
        drawCircle(
            color = gColor,
            radius = 9.dp.toPx(),
            center = Offset(gx, guardTop + 9.dp.toPx())
        )
        // Guard legs
        val guardStride = if (guardStunned) 0f else sin(animClock * 4f) * 6.dp.toPx()
        drawLine(
            color = gColor,
            start = Offset(gx - 5.dp.toPx(), roofY - 16.dp.toPx()),
            end = Offset(gx - 5.dp.toPx() - guardStride, roofY),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = gColor,
            start = Offset(gx + 5.dp.toPx(), roofY - 16.dp.toPx()),
            end = Offset(gx + 5.dp.toPx() + guardStride, roofY),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 5. Hooded Assassin Character
        val px = w * playerX
        val py = roofY + playerYOffset * h
        val stanceHeight = if (isCrouching) 40.dp.toPx() else 64.dp.toPx()
        val pTop = py - stanceHeight
        val dir = if (facingRight) 1f else -1f

        val strideSpeed = if (isSprinting) 16f else 10f
        val legSwing = if (isMoving && playerYOffset >= -0.005f) sin(animClock * strideSpeed) * 9.dp.toPx() else 0f

        // Flowing Cloak / Cape
        val cloakFlow = (if (isMoving) -18.dp.toPx() else -8.dp.toPx()) * dir +
            sin(animClock * 5f) * 3.dp.toPx()
        val cloakPath = Path().apply {
            moveTo(px, pTop + 14.dp.toPx())
            lineTo(px + cloakFlow, py - 8.dp.toPx())
            lineTo(px - 6.dp.toPx() * dir, py - 12.dp.toPx())
            close()
        }
        drawPath(
            path = cloakPath,
            color = if (eagleVisionActive) AnimusCyan.copy(alpha = 0.85f) else CrimsonPrimary
        )

        // Legs
        drawLine(
            color = ParchmentText,
            start = Offset(px - 4.dp.toPx(), py - stanceHeight * 0.34f),
            end = Offset(px - 4.dp.toPx() - legSwing, py),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = ParchmentText,
            start = Offset(px + 4.dp.toPx(), py - stanceHeight * 0.34f),
            end = Offset(px + 4.dp.toPx() + legSwing, py),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Tunic Torso
        drawRoundRect(
            color = Color(0xFFE6E1D6),
            topLeft = Offset(px - 11.dp.toPx(), pTop + 14.dp.toPx()),
            size = Size(22.dp.toPx(), stanceHeight * 0.48f),
            cornerRadius = CornerRadius(6.dp.toPx())
        )

        // Crimson Sash Belt
        drawRect(
            color = CrimsonPrimary,
            topLeft = Offset(px - 11.dp.toPx(), pTop + stanceHeight * 0.45f),
            size = Size(22.dp.toPx(), 5.dp.toPx())
        )

        // Iconic Pointed Hood
        val hoodPath = Path().apply {
            moveTo(px - 10.dp.toPx(), pTop + 16.dp.toPx())
            lineTo(px + 4.dp.toPx() * dir, pTop)
            lineTo(px + 10.dp.toPx(), pTop + 16.dp.toPx())
            close()
        }
        drawPath(path = hoodPath, color = ParchmentText)

        // Block Shield Arc
        if (isBlocking) {
            drawArc(
                color = AnimusCyan,
                startAngle = if (facingRight) -55f else 125f,
                sweepAngle = 110f,
                useCenter = false,
                topLeft = Offset(px - 24.dp.toPx(), pTop + 6.dp.toPx()),
                size = Size(48.dp.toPx(), 48.dp.toPx()),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Hidden Blade Slash Arc
        if (attackProgress > 0f) {
            val slashAlpha = attackProgress.coerceIn(0f, 1f)
            drawArc(
                color = WarmGoldBright.copy(alpha = slashAlpha),
                startAngle = if (facingRight) -45f else 135f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(px - 16.dp.toPx() + (14.dp.toPx() * dir), pTop + 4.dp.toPx()),
                size = Size(54.dp.toPx(), 54.dp.toPx()),
                style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
            )
            drawLine(
                color = CrimsonPrimary.copy(alpha = slashAlpha),
                start = Offset(px + 8.dp.toPx() * dir, pTop + 26.dp.toPx()),
                end = Offset(px + 46.dp.toPx() * dir, pTop + 22.dp.toPx()),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
