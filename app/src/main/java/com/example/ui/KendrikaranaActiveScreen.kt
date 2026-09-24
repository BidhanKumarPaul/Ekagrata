package com.example.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.util.MindfulChimeHelper
import kotlinx.coroutines.delay

@Composable
fun KendrikaranaActiveScreen(
    session: ActiveSessionState.Active,
    allowedApps: List<AppInfo>,
    isStrictMode: Boolean = false,
    showSanskritMantras: Boolean = true,
    hanumanChalisaEnabled: Boolean = true,
    showExitConfirmation: Boolean = false,
    onRequestExitConfirmation: (Boolean) -> Unit = {},
    onTogglePause: () -> Unit,
    onEmergencyExit: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    // Redirect device back button to "End Early" confirmation dialog
    BackHandler(enabled = true) {
        onRequestExitConfirmation(true)
    }

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

    // Mantras list properly filtered according to Sanskrit and Hanuman Chalisa settings
    val mantraList = remember(showSanskritMantras, hanumanChalisaEnabled) {
        if (!showSanskritMantras && !hanumanChalisaEnabled) {
            emptyList()
        } else if (showSanskritMantras && hanumanChalisaEnabled) {
            KendrikaranaWisdom.allMantras
        } else if (showSanskritMantras) {
            KendrikaranaWisdom.allMantras.filter { !it.isHanumanChalisa }
        } else {
            KendrikaranaWisdom.allMantras.filter { it.isHanumanChalisa }
        }
    }

    var currentMantraIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(mantraList.size) {
        while (mantraList.isNotEmpty()) {
            delay(15000)
            currentMantraIndex = (currentMantraIndex + 1) % mantraList.size
        }
    }

    val currentMantra = if (mantraList.isNotEmpty()) {
        mantraList.getOrNull(currentMantraIndex % mantraList.size)
    } else null

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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Shield Status & Sound Chime Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SlateNavy.copy(alpha = 0.8f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Shield Active",
                            tint = CalmEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DISTRACTION SHIELD ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalmEmerald
                        )
                    }
                }

                // Motivational Bell Ring Button
                IconButton(
                    onClick = {
                        MindfulChimeHelper.playMotivationalChime()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SlateNavy.copy(alpha = 0.7f))
                        .border(1.dp, CardBorder, CircleShape)
                        .testTag("ring_chime_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Ring Motivation Sound",
                        tint = FocusAmber,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Middle Section: Goal Title, Circular Timer, and Mantras
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "KENDRĪKARAṆA",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold,
                    color = AcademicIndigo
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "\"${session.goalTitle}\"",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Circular Timer Ring with Breath Pulse
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(240.dp)
                        .scale(pulseScale)
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = CardBorder.copy(alpha = 0.4f),
                        strokeWidth = 10.dp
                    )

                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = if (session.isPaused) FocusAmber else CalmEmerald,
                        strokeWidth = 10.dp
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("kendrikarana_timer_display")
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (session.isPaused) "PAUSED" else "DEEP IMMERSION",
                            style = MaterialTheme.typography.labelMedium,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Sacred Focus Mantra & Hanuman Chalisa Banner
                if (currentMantra != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, CardBorder.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                        color = CardSurface.copy(alpha = 0.7f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        currentMantraIndex = if (currentMantraIndex - 1 < 0) mantraList.size - 1 else currentMantraIndex - 1
                                    },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription = "Previous Mantra",
                                        tint = SoftSkyBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = currentMantra.source,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentMantra.isHanumanChalisa) FocusAmber else AcademicIndigo
                                )

                                IconButton(
                                    onClick = {
                                        currentMantraIndex = (currentMantraIndex + 1) % mantraList.size
                                    },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Next Mantra",
                                        tint = SoftSkyBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "\"${currentMantra.meaning}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SoftSkyBlue,
                                textAlign = TextAlign.Center
                            )

                            if (currentMantra.transliteration.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentMantra.transliteration.lines().firstOrNull() ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
                // Strict Lockout vs Urvarā Section: Allowed Apps Tray in Kendrīkaraṇa mode
                if (isStrictMode) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        color = FocusCrimson.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FocusCrimson.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = FocusCrimson,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Strict Lockout: All Apps Blocked",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = FocusCrimson
                            )
                        }
                    }
                } else if (allowedApps.isNotEmpty()) {
                    Text(
                        text = "URVARĀ (ALLOWED APPS)",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
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

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Controls: Pause / Resume & Emergency Exit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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

                    Button(
                        onClick = { onRequestExitConfirmation(true) },
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
                            text = "END KENDRIKARANA",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                BkpWatermark(subtle = true)
            }
        }

        // Emergency Exit Confirmation Dialog
        if (showExitConfirmation) {
            AlertDialog(
                onDismissRequest = { onRequestExitConfirmation(false) },
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
                        text = "End Kendrīkaraṇa?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Are you sure you want to end this focus session? Your focus time accumulated so far will be saved to your active goal progress.",
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
                            onRequestExitConfirmation(false)
                            onEmergencyExit()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FocusCrimson,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("End Session & Save Progress")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { onRequestExitConfirmation(false) }
                    ) {
                        Text("Stay Focused", color = SoftSkyBlue)
                    }
                }
            )
        }
    }
}
