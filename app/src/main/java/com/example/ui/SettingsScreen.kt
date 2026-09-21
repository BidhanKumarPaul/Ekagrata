package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserSettingsEntity
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    currentSettings: UserSettingsEntity,
    goals: List<Goal>,
    allowedAppsCount: Int,
    totalAppsCount: Int,
    onSaveSettings: (UserSettingsEntity) -> Unit,
    onOpenAppDrawer: () -> Unit,
    onBackToDashboard: () -> Unit,
    onResetDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dailyTarget by remember(currentSettings) { mutableIntStateOf(currentSettings.dailyTargetMinutes) }
    var defaultDuration by remember(currentSettings) { mutableIntStateOf(currentSettings.defaultFocusDurationMinutes) }
    var strictMode by remember(currentSettings) { mutableStateOf(currentSettings.strictModeEnabled) }
    var soundChime by remember(currentSettings) { mutableStateOf(currentSettings.soundChimeEnabled) }
    var sanskritMantras by remember(currentSettings) { mutableStateOf(currentSettings.sanskritMantrasEnabled) }
    var keepScreenOn by remember(currentSettings) { mutableStateOf(currentSettings.keepScreenOn) }
    var selectedGoalId by remember(currentSettings) { mutableStateOf(currentSettings.activeGoalId) }

    var showSaveToast by remember { mutableStateOf(false) }

    LaunchedEffect(showSaveToast) {
        if (showSaveToast) {
            delay(2500)
            showSaveToast = false
        }
    }

    val durationOptions = listOf(15, 25, 45, 60, 90, 120)
    val dailyTargetOptions = listOf(60, 120, 180, 240, 300, 360)

    fun applyAndSave() {
        val updated = currentSettings.copy(
            dailyTargetMinutes = dailyTarget,
            defaultFocusDurationMinutes = defaultDuration,
            strictModeEnabled = strictMode,
            soundChimeEnabled = soundChime,
            sanskritMantrasEnabled = sanskritMantras,
            keepScreenOn = keepScreenOn,
            activeGoalId = selectedGoalId
        )
        onSaveSettings(updated)
        showSaveToast = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepObsidian,
                        SlateNavy.copy(alpha = 0.9f),
                        DeepObsidian
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top Bar with Back Button & Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackToDashboard,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CardSurface)
                            .border(1.dp, CardBorder, CircleShape)
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Dashboard",
                            tint = SoftSkyBlue
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "SETTINGS",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = SoftSkyBlue
                        )
                        Text(
                            text = "Kendrīkaraṇa Preferences",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Watermark pill in header
                BkpWatermark(asPill = true)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Confirmation Banner if triggered
            if (showSaveToast) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = CalmEmerald.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CalmEmerald)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = CalmEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Settings successfully saved to local database!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = CalmEmerald
                        )
                    }
                }
            }

            // SECTION 1: Kendrīkaraṇa (Focus) Configuration
            SettingsSectionHeader(
                title = "KENDRĪKARAṆA MODE",
                subtitle = "Deep focus duration & concentration engine rules"
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(18.dp)),
                color = CardSurface
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Default Duration Chips
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = SoftSkyBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Default Focus Duration",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        durationOptions.forEach { mins ->
                            val isSelected = defaultDuration == mins
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    defaultDuration = mins
                                    applyAndSave()
                                },
                                label = { Text("$mins min") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SoftSkyBlue,
                                    selectedLabelColor = DeepObsidian,
                                    containerColor = DeepObsidian,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Strict Mode Toggle
                    SettingsSwitchRow(
                        icon = Icons.Default.Security,
                        title = "Strict Kendrīkaraṇa Mode",
                        description = "Completely locks out non-whitelisted apps (instead of standard friction warning)",
                        checked = strictMode,
                        onCheckedChange = {
                            strictMode = it
                            applyAndSave()
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Sanskrit Mantras Toggle
                    SettingsSwitchRow(
                        icon = Icons.Default.Visibility,
                        title = "Sanskrit Concentration Mantras",
                        description = "Displays authentic concentration aphorisms (केन्द्रीकरण मन्त्राः) during sessions",
                        checked = sanskritMantras,
                        onCheckedChange = {
                            sanskritMantras = it
                            applyAndSave()
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Sound Chimes Toggle
                    SettingsSwitchRow(
                        icon = Icons.Default.VolumeUp,
                        title = "Mindful Chimes & Bell",
                        description = "Plays calming start and completion tones to anchor attention",
                        checked = soundChime,
                        onCheckedChange = {
                            soundChime = it
                            applyAndSave()
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Keep Screen Awake
                    SettingsSwitchRow(
                        icon = Icons.Default.Notifications,
                        title = "Keep Screen On In Kendrīkaraṇa",
                        description = "Prevents device from sleeping while active focus timer is running",
                        checked = keepScreenOn,
                        onCheckedChange = {
                            keepScreenOn = it
                            applyAndSave()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 2: Daily Target & Active Goal
            SettingsSectionHeader(
                title = "DAILY TARGET & ACTIVE GOAL",
                subtitle = "Set your daily immersion threshold and choose active goal"
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(18.dp)),
                color = CardSurface
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Daily Focus Target: ${dailyTarget / 60}h ${dailyTarget % 60}m",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dailyTargetOptions.forEach { mins ->
                            val isSelected = dailyTarget == mins
                            val label = "${mins / 60}h"
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    dailyTarget = mins
                                    applyAndSave()
                                },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FocusAmber,
                                    selectedLabelColor = DeepObsidian,
                                    containerColor = DeepObsidian,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Active Primary Goal",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "The goal spotlighted on the main dashboard and targeted during Kendrīkaraṇa",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (goals.isEmpty()) {
                        Text(
                            text = "No goals created yet. Create a goal from the Dashboard.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            goals.forEach { g ->
                                val isSelected = selectedGoalId == g.id || (selectedGoalId == null && g == goals.firstOrNull())
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedGoalId = g.id
                                            applyAndSave()
                                        },
                                    color = if (isSelected) SlateNavy else DeepObsidian,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) SoftSkyBlue else CardBorder
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = g.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) SoftSkyBlue else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${g.completedHours}h of ${g.targetHours}h (${g.progressPercentage}%)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Active",
                                                tint = SoftSkyBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 3: App Whitelist Access
            SettingsSectionHeader(
                title = "FOCUS APP ACCESS",
                subtitle = "Manage apps permitted during Kendrīkaraṇa"
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(18.dp)),
                color = CardSurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Allowed Apps: $allowedAppsCount of $totalAppsCount",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap to view and toggle which applications can be opened in Kendrīkaraṇa mode.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenAppDrawer,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftSkyBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Manage")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION 4: Developer Attribution & Watermark Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, AcademicIndigo.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                color = SlateNavy.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "EKAGRATA LAUNCHER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = SoftSkyBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Kendrīkaraṇa Deep Focus Architecture",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mandatory Watermark prominently showcased
                    BkpWatermark(asPill = true)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Engineered with intentional simplicity to eliminate digital distractions and foster single-pointed concentration.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onResetDefaults,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset to Defaults")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Save & Return Action
            Button(
                onClick = {
                    applyAndSave()
                    onBackToDashboard()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_settings_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftSkyBlue,
                    contentColor = DeepObsidian
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SAVE & RETURN TO DASHBOARD",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Watermark
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BkpWatermark(subtle = false)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = AcademicIndigo
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SoftSkyBlue,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DeepObsidian,
                checkedTrackColor = SoftSkyBlue,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = DeepObsidian
            )
        )
    }
}
