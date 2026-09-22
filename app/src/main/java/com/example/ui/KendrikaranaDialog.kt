package com.example.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.TextButton
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
import com.example.model.AppCategory
import com.example.model.AppInfo
import com.example.model.FocusMode
import com.example.model.Goal
import com.example.ui.components.BkpWatermark
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
fun KendrikaranaDialog(
    activeGoal: Goal?,
    goals: List<Goal>,
    allowedApps: List<AppInfo>,
    allApps: List<AppInfo> = emptyList(),
    totalAppsCount: Int,
    defaultMinutes: Int = 45,
    onStart: (goalId: Long?, goalTitle: String, durationMinutes: Int, mode: FocusMode, allowedPackages: Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedGoal by remember { mutableStateOf(activeGoal ?: goals.firstOrNull()) }
    var selectedDuration by remember { mutableIntStateOf(defaultMinutes) }
    var selectedMode by remember { mutableStateOf(FocusMode.DEEP) }
    val urvaraApps = remember(allApps, allowedApps) {
        val list = if (allApps.isNotEmpty()) allApps.filter { it.category == AppCategory.URVARA } else allowedApps.filter { it.category == AppCategory.URVARA }
        list
    }
    var selectedAllowedPackages by remember(urvaraApps) {
        mutableStateOf(urvaraApps.filter { it.isAllowedInFocus }.map { it.packageName }.toSet())
    }

    val durations = listOf(15, 25, 45, 50, 90, 120)
    val modes = listOf(
        FocusMode.DEEP,
        FocusMode.STUDY,
        FocusMode.QUICK,
        FocusMode.READING,
        FocusMode.CODING,
        FocusMode.EXAM
    )

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
                        text = "KENDRĪKARAṆA MODE",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = SoftSkyBlue
                    )
                    Text(
                        text = "Distraction-Free Deep Focus",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Focus Goal Section
                Text(
                    text = "FOCUS GOAL",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = AcademicIndigo
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (goals.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        color = CardSurface
                    ) {
                        Text(
                            text = "General Study & Deep Work",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        goals.take(4).forEach { goal ->
                            val isSelected = selectedGoal?.id == goal.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedGoal = goal },
                                color = if (isSelected) SlateNavy else CardSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) SoftSkyBlue else CardBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = goal.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) SoftSkyBlue else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${goal.completedHours}h of ${goal.targetHours}h (${goal.progressPercentage}%)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Selected",
                                            tint = SoftSkyBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Duration Selector
                Text(
                    text = "IMMERSION DURATION",
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
                    durations.forEach { duration ->
                        val isSelected = selectedDuration == duration
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDuration = duration },
                            label = { Text("$duration min") },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SoftSkyBlue,
                                selectedLabelColor = DeepObsidian,
                                selectedLeadingIconColor = DeepObsidian,
                                containerColor = CardSurface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Mode Selection
                Text(
                    text = "FOCUS PRESET",
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
                                selectedContainerColor = FocusAmber,
                                selectedLabelColor = DeepObsidian,
                                containerColor = CardSurface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // App Blocking Configuration Section (Urvarā)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "URVARĀ (APP BLOCKER)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = AcademicIndigo
                    )

                    TextButton(
                        onClick = {
                            selectedAllowedPackages = if (selectedAllowedPackages.isEmpty()) {
                                allowedApps.map { it.packageName }.toSet()
                            } else {
                                emptySet()
                            }
                        }
                    ) {
                        Text(
                            text = if (selectedAllowedPackages.isEmpty()) "Allow Urvarā Apps" else "Block All Apps",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedAllowedPackages.isEmpty()) SoftSkyBlue else FocusAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Allowed Apps Info Banner
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                    color = CardSurface.copy(alpha = 0.6f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (selectedAllowedPackages.isEmpty()) FocusAmber else CalmEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedAllowedPackages.isEmpty()) {
                                    "Strict Lockdown: ALL $totalAppsCount apps blocked from opening"
                                } else {
                                    "${selectedAllowedPackages.size} Urvarā apps accessible • ${totalAppsCount - selectedAllowedPackages.size} apps strictly blocked"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Only Urvarā apps can be toggled for Kendrīkaraṇa
                        if (urvaraApps.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                urvaraApps.forEach { app ->
                                    val isAllowed = selectedAllowedPackages.contains(app.packageName)
                                    FilterChip(
                                        selected = isAllowed,
                                        onClick = {
                                            selectedAllowedPackages = if (isAllowed) {
                                                selectedAllowedPackages - app.packageName
                                            } else {
                                                selectedAllowedPackages + app.packageName
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = app.label,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (isAllowed) Icons.Default.Security else Icons.Default.Lock,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CalmEmerald.copy(alpha = 0.3f),
                                            selectedLabelColor = SoftSkyBlue,
                                            containerColor = CardSurface,
                                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Watermark footer
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    BkpWatermark(subtle = true)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val title = selectedGoal?.title ?: "Unbroken Study"
                    onStart(selectedGoal?.id, title, selectedDuration, selectedMode, selectedAllowedPackages)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("confirm_start_kendrikarana_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftSkyBlue,
                    contentColor = DeepObsidian
                )
            ) {
                Text(
                    text = "COMMENCE KENDRĪKARAṆA",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
