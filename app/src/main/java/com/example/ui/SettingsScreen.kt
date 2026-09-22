package com.example.ui

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.util.MindfulChimeHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    // Hardware back press returns cleanly to dashboard
    BackHandler(enabled = true) {
        onBackToDashboard()
    }

    var dailyTarget by remember(currentSettings) { mutableIntStateOf(currentSettings.dailyTargetMinutes) }
    var defaultDuration by remember(currentSettings) { mutableIntStateOf(currentSettings.defaultFocusDurationMinutes) }
    var strictMode by remember(currentSettings) { mutableStateOf(currentSettings.strictModeEnabled) }
    var soundChime by remember(currentSettings) { mutableStateOf(currentSettings.soundChimeEnabled) }
    var sanskritMantras by remember(currentSettings) { mutableStateOf(currentSettings.sanskritMantrasEnabled) }
    var hanumanChalisa by remember(currentSettings) { mutableStateOf(currentSettings.hanumanChalisaEnabled) }
    var keepScreenOn by remember(currentSettings) { mutableStateOf(currentSettings.keepScreenOn) }
    var selectedGoalId by remember(currentSettings) { mutableStateOf(currentSettings.activeGoalId) }

    var goalDropdownExpanded by remember { mutableStateOf(false) }
    var showSaveToast by remember { mutableStateOf(false) }

    LaunchedEffect(showSaveToast) {
        if (showSaveToast) {
            delay(1800)
            showSaveToast = false
        }
    }

    fun applyAndSave() {
        val updated = currentSettings.copy(
            dailyTargetMinutes = dailyTarget,
            defaultFocusDurationMinutes = defaultDuration,
            strictModeEnabled = strictMode,
            soundChimeEnabled = soundChime,
            sanskritMantrasEnabled = sanskritMantras,
            hanumanChalisaEnabled = hanumanChalisa,
            keepScreenOn = keepScreenOn,
            activeGoalId = selectedGoalId
        )
        onSaveSettings(updated)
        showSaveToast = true
    }

    val dailyTargetOptions = listOf(60, 120, 180, 240, 300, 360)
    val durationOptions = listOf(15, 25, 30, 45, 60, 90, 120)

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        color = DeepObsidian
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackToDashboard,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CardSurface)
                            .border(1.dp, CardBorder, CircleShape)
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Dashboard",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "SETTINGS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ekāgratā Launcher Configuration",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (showSaveToast) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CalmEmerald.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CalmEmerald)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = CalmEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Saved",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CalmEmerald
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // LAUNCHER SYSTEM INTEGRATION SECTION
            SettingsSectionHeader(
                title = "LAUNCHER ROLE & SYSTEM",
                subtitle = "Set Ekāgratā as your primary Android home launcher"
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, SoftSkyBlue.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                color = CardSurface
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = SoftSkyBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Set as Default Home Launcher",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Replace your distracting OEM launcher with Ekāgratā mindful interface.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                try {
                                    val homeIntent = Intent(Settings.ACTION_HOME_SETTINGS)
                                    context.startActivity(homeIntent)
                                } catch (_: Exception) {
                                    // Fallback to general settings
                                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SlateNavy,
                            contentColor = SoftSkyBlue
                        )
                    ) {
                        Text("Configure Default Launcher in Android Settings")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION: Daily Target & Active Primary Goal Selection (Dropdown + 7 newest goals)
            SettingsSectionHeader(
                title = "DAILY TARGET & ACTIVE GOAL",
                subtitle = "Select active goal via dropdown (automatically tracking 7 newest goals)"
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
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    dailyTarget = mins
                                    applyAndSave()
                                },
                                label = { Text("${mins / 60}h") },
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
                        text = "Active Primary Goal (Dropdown)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Spotlighted goal on the dashboard and targeted in Kendrīkaraṇa. Limited to 7 newest goals.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val activeGoalObj = goals.firstOrNull { it.id == selectedGoalId } ?: goals.firstOrNull()

                    ExposedDropdownMenuBox(
                        expanded = goalDropdownExpanded,
                        onExpandedChange = { goalDropdownExpanded = !goalDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = activeGoalObj?.title ?: "Select Goal (None Defined)",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalDropdownExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("active_goal_dropdown"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DeepObsidian,
                                unfocusedContainerColor = DeepObsidian,
                                focusedBorderColor = SoftSkyBlue,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = goalDropdownExpanded,
                            onDismissRequest = { goalDropdownExpanded = false },
                            modifier = Modifier.background(DeepObsidian)
                        ) {
                            if (goals.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No goals found. Create from dashboard.") },
                                    onClick = { goalDropdownExpanded = false }
                                )
                            } else {
                                goals.take(7).forEach { g ->
                                    val isSelected = g.id == selectedGoalId || (selectedGoalId == null && g == goals.firstOrNull())
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = g.title,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) SoftSkyBlue else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "${String.format("%.1f", g.completedHours)} / ${String.format("%.1f", g.targetHours)} hrs (${g.progressPercentage}%)",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = SoftSkyBlue,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedGoalId = g.id
                                            goalDropdownExpanded = false
                                            applyAndSave()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECTION: Kendrīkaraṇa & Sacred Focus Mantras
            SettingsSectionHeader(
                title = "KENDRĪKARAṆA & SACRED MANTRAS",
                subtitle = "Immersion duration, Hanuman Chalisa, audio chimes and shield rules"
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
                        text = "Default Focus Duration: $defaultDuration min",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

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
                                label = { Text("${mins}m") },
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

                    // Hanuman Chalisa & Focus Mantras Toggle
                    SettingsSwitchRow(
                        icon = Icons.Default.Info,
                        title = "Sacred Mantras & Hanuman Chalisa",
                        description = "Display Hanuman Chalisa focus verses and Vedic sutras in Kendrīkaraṇa mode.",
                        checked = hanumanChalisa,
                        onCheckedChange = {
                            hanumanChalisa = it
                            applyAndSave()
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Sound Chimes Toggle & Test Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = SoftSkyBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Mindful Audio Bells",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Acoustic bells for start, interval & completion.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = soundChime,
                            onCheckedChange = {
                                soundChime = it
                                applyAndSave()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Test Motivation Sound Button
                    OutlinedButton(
                        onClick = {
                            MindfulChimeHelper.playMotivationalChime()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FocusAmber)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ring Motivation Sound (Test)")
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Strict Mode Toggle
                    SettingsSwitchRow(
                        icon = Icons.Default.Security,
                        title = "Strict Lockout Mode",
                        description = "When enabled, hides and disallows Urvarā (allowed apps) during Kendrīkaraṇa for total zero-app lockdown.",
                        checked = strictMode,
                        onCheckedChange = {
                            strictMode = it
                            applyAndSave()
                        }
                    )
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

            // USER LINKS SECTION: "under save and return section and before developed by BKP section"
            SettingsSectionHeader(
                title = "CREATOR & PORTFOLIO LINKS",
                subtitle = "Clickable GitHub profile and developer website"
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                color = CardSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // GitHub Profile Link
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                uriHandler.openUri(currentSettings.githubUrl)
                            },
                        color = SlateNavy.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AcademicIndigo.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "GitHub Profile",
                                    tint = SoftSkyBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "GitHub Profile",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = currentSettings.githubUrl,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SoftSkyBlue,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Open GitHub",
                                tint = SoftSkyBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Personal Website Link
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                uriHandler.openUri(currentSettings.websiteUrl)
                            },
                        color = SlateNavy.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AcademicIndigo.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Personal Website",
                                    tint = CalmEmerald,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Personal Website",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = currentSettings.websiteUrl,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CalmEmerald,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Open Website",
                                tint = CalmEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // DEVELOPED BY BKP SECTION
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
                        text = "EKĀGRATĀ LAUNCHER",
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

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BkpWatermark(subtle = false)
            }

            Spacer(modifier = Modifier.height(24.dp))
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
