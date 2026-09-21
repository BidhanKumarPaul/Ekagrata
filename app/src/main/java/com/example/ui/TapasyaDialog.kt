package com.example.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppInfo
import com.example.model.FocusMode
import com.example.model.Goal
import com.example.ui.theme.AcademicIndigo
import com.example.ui.theme.CalmEmerald
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.FocusAmber
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.SoftSkyBlue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TapasyaDialog(
    activeGoal: Goal?,
    goals: List<Goal>,
    allowedApps: List<AppInfo>,
    totalAppsCount: Int,
    onStart: (goalId: Long?, goalTitle: String, durationMinutes: Int, mode: FocusMode) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedGoal by remember { mutableStateOf(activeGoal ?: goals.firstOrNull()) }
    var selectedDuration by remember { mutableIntStateOf(50) }
    var selectedMode by remember { mutableStateOf(FocusMode.DEEP) }

    val durations = listOf(25, 45, 50, 90, 120)
    val modes = listOf(FocusMode.DEEP, FocusMode.STUDY, FocusMode.QUICK, FocusMode.READING, FocusMode.CODING, FocusMode.EXAM)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepObsidian,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TAPASYA MODE",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = SoftSkyBlue
                    )
                    Text(
                        text = "Distraction-Free Deep Study",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Goal Section
                Text(
                    text = "TARGET GOAL",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = AcademicIndigo
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                    color = CardSurface
                ) {
                    Text(
                        text = selectedGoal?.title ?: "General Concentration",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Duration Section
                Text(
                    text = "DURATION (MINUTES)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = AcademicIndigo
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    durations.forEach { minutes ->
                        val isSelected = selectedDuration == minutes
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDuration = minutes },
                            label = { Text("$minutes min") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SoftSkyBlue.copy(alpha = 0.25f),
                                selectedLabelColor = SoftSkyBlue,
                                containerColor = CardSurface,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = CardBorder,
                                selectedBorderColor = SoftSkyBlue
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Focus Mode Section
                Text(
                    text = "FOCUS MODE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = AcademicIndigo
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    modes.forEach { mode ->
                        val isSelected = selectedMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedMode = mode
                                selectedDuration = mode.defaultMinutes
                            },
                            label = { Text(mode.title) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AcademicIndigo.copy(alpha = 0.25f),
                                selectedLabelColor = AcademicIndigo,
                                containerColor = CardSurface,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = CardBorder,
                                selectedBorderColor = AcademicIndigo
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Allowed Apps Summary
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    color = CardSurface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CalmEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            val allowedCount = allowedApps.size
                            val blockedCount = (totalAppsCount - allowedCount).coerceAtLeast(0)
                            Text(
                                text = "App Shield Active",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$allowedCount permitted • $blockedCount restricted during focus",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onStart(
                        selectedGoal?.id,
                        selectedGoal?.title ?: "Deep Work",
                        selectedDuration,
                        selectedMode
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("enter_tapasya_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftSkyBlue,
                    contentColor = DeepObsidian
                )
            ) {
                Text(
                    text = "ENTER TAPASYA ($selectedDuration MIN)",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        },
        dismissButton = null
    )
}
