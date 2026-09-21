package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
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
import com.example.ui.components.AppIconView
import com.example.ui.theme.AcademicIndigo
import com.example.ui.theme.CalmEmerald
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.FocusAmber
import com.example.ui.theme.FocusCrimson
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.SoftSkyBlue

@Composable
fun TapasyaActiveScreen(
    session: ActiveSessionState.Active,
    allowedApps: List<AppInfo>,
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
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        SlateNavy,
                        DeepObsidian
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("tapasya_active_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header: Goal Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "TAPASYA IN PROGRESS",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 2.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = AcademicIndigo
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${session.goalTitle}\"",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${session.mode.title} • Resisted Distractions: ${session.interruptionsAvoided}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CalmEmerald
                )
            }

            // Center Circular Timer with Breathing Pulse
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                // Background subtle ring
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(SoftSkyBlue.copy(alpha = 0.05f))
                )

                // Progress Indicator
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(240.dp),
                    color = if (session.isPaused) FocusAmber else SoftSkyBlue,
                    strokeWidth = 10.dp,
                    trackColor = CardBorder
                )

                // Inner Time Display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-1).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (session.isPaused) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PAUSED",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = FocusAmber
                        )
                    }
                }
            }

            // Bottom Section: Allowed Apps Dock & Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Permitted Tools Bar
                Text(
                    text = "PERMITTED TOOLS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(allowedApps.take(6), key = { it.packageName }) { app ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(64.dp)
                                .clickable { onAppClick(app) }
                                .padding(horizontal = 4.dp)
                        ) {
                            AppIconView(
                                packageName = app.packageName,
                                label = app.label,
                                category = app.category,
                                size = 46.dp
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

                Spacer(modifier = Modifier.height(24.dp))

                // Action Controls: Pause & Emergency Exit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onTogglePause,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (session.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (session.isPaused) "Resume" else "Pause")
                    }

                    Button(
                        onClick = { showExitConfirmation = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("emergency_exit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CardSurface,
                            contentColor = FocusCrimson
                        )
                    ) {
                        Text(
                            text = "Emergency Exit",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // Emergency Friction Confirmation Modal
    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            containerColor = DeepObsidian,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = FocusAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Break Deep Focus?")
                }
            },
            text = {
                Text(
                    text = "Your Tapasya session has $remainingMinutes minutes remaining for \"${session.goalTitle}\".\n\nStaying focused builds your streak and resilience. Are you sure you want to end early?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmation = false
                        onEmergencyExit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusCrimson)
                ) {
                    Text("Exit Anyway")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) {
                    Text("Stay Focused", color = SoftSkyBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
