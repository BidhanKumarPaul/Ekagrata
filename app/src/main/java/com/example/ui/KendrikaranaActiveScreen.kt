package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppInfo
import com.example.model.KendrikaranaWisdom
import com.example.ui.components.AppIconView
import com.example.ui.components.BkpWatermark
import com.example.ui.theme.AcademicIndigo
import com.example.ui.theme.CalmEmerald
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.FocusAmber
import com.example.ui.theme.FocusCrimson
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.SoftSkyBlue
import kotlinx.coroutines.delay

@Composable
fun KendrikaranaActiveScreen(
    session: ActiveSessionState.Active,
    allowedApps: List<AppInfo>,
    showSanskritMantras: Boolean = true,
    onTogglePause: () -> Unit,
    onEmergencyExit: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var showExitConfirmation by remember { mutableStateOf(false) }

    val remainingMinutes = session.remainingSeconds / 60
    val remainingSecs = session.remainingSeconds % 60
    val formattedTime = String.format("%02d:%02d", remainingMinutes, remainingSecs)

    val progress = (session.remainingSeconds.toFloat() / session.totalSeconds.toFloat()).coerceIn(0f, 1f)

    // Breathing circle pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Rotating Sanskrit Sutra
    var currentSutraIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(20000)
            currentSutraIndex = (currentSutraIndex + 1) % KendrikaranaWisdom.sutras.size
        }
    }
    val currentSutra = KendrikaranaWisdom.sutras[currentSutraIndex]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        SlateNavy.copy(alpha = 0.95f),
                        DeepObsidian,
                        Color(0xFF06090E)
                    ),
                    radius = 900f
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Kendrīkaraṇa Mode Indicator & Watermark pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (session.isPaused) FocusAmber else CalmEmerald,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (session.isPaused) "PAUSED" else "KENDRĪKARAṆA ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = if (session.isPaused) FocusAmber else CalmEmerald
                        )
                    }
                }

                BkpWatermark(asPill = true)
            }

            // Central Area: Goal Title, Breath Pulse Timer, Sanskrit Sutra
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = session.goalTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${session.mode.title} • Immersion",
                    style = MaterialTheme.typography.labelMedium,
                    color = AcademicIndigo,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Circular Timer Dial with Pranayama Pulse
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(240.dp)
                        .scale(pulseScale)
                ) {
                    // Outer background ring
                    CircularProgressIndicator(
                        progress = 1f,
                        modifier = Modifier.fillMaxSize(),
                        color = CardBorder.copy(alpha = 0.4f),
                        strokeWidth = 10.dp
                    )

                    // Active progress ring
                    CircularProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxSize(),
                        color = if (session.isPaused) FocusAmber else SoftSkyBlue,
                        strokeWidth = 10.dp
                    )

                    // Inner Timer Display
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formattedTime,
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = FontFamily.SansSerif,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-1).sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (session.isPaused) "TAP RESUME" else "STAY CENTERED",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Sanskrit Wisdom Sutra Banner
                if (showSanskritMantras) {
                    Spacer(modifier = Modifier.height(22.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                        color = CardSurface.copy(alpha = 0.6f)
                    ) {
                        AnimatedContent(
                            targetState = currentSutra,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "sutra_transition"
                        ) { sutra ->
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = sutra.devanagari,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftSkyBlue,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"${sutra.meaning}\"",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Section: Allowed Apps, Action Controls, and Watermark
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Allowed Apps Drawer Tray
                if (allowedApps.isNotEmpty()) {
                    Text(
                        text = "ALLOWED TOOLS",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(allowedApps, key = { it.packageName }) { app ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(60.dp)
                                    .clickable { onAppClick(app) }
                            ) {
                                AppIconView(
                                    packageName = app.packageName,
                                    label = app.label,
                                    category = app.category,
                                    size = 48.dp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = app.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Controls: Pause / Resume & Emergency Exit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Pause / Resume Button
                    OutlinedButton(
                        onClick = onTogglePause,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("kendrikarana_pause_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (session.isPaused) SoftSkyBlue else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = if (session.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (session.isPaused) "Resume" else "Pause",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (session.isPaused) "RESUME" else "PAUSE",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Emergency Exit Button
                    Button(
                        onClick = { showExitConfirmation = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("kendrikarana_exit_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CardSurface,
                            contentColor = FocusCrimson
                        )
                    ) {
                        Text(
                            text = "END EARLY",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Watermark
                BkpWatermark(subtle = true)
            }
        }

        // Emergency Exit Confirmation Dialog
        if (showExitConfirmation) {
            AlertDialog(
                onDismissRequest = { showExitConfirmation = false },
                containerColor = DeepObsidian,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = FocusCrimson,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Break Kendrīkaraṇa?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Ending your Kendrīkaraṇa session early breaks your flow streak and forfeits bonus concentration XP.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        BkpWatermark(subtle = true)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitConfirmation = false
                            onEmergencyExit()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FocusCrimson,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Break Focus")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitConfirmation = false }) {
                        Text("Stay Focused", color = SoftSkyBlue)
                    }
                }
            )
        }
    }
}
