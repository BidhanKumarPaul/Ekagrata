package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppCategory
import com.example.model.AppInfo
import com.example.ui.components.AppIconView
import com.example.ui.components.BkpWatermark
import com.example.ui.theme.AcademicIndigo
import com.example.ui.theme.CalmEmerald
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.FocusAmber
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.SoftSkyBlue

private enum class DrawerLayoutMode {
    GRID,
    LIST
}

@Composable
fun AppDrawerSheet(
    searchQuery: String,
    selectedCategory: AppCategory,
    apps: List<AppInfo>,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (AppCategory) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onToggleAllowedInFocus: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Intercept back press cleanly to close the drawer
    BackHandler(enabled = true) {
        onDismiss()
    }

    var layoutMode by remember { mutableStateOf(DrawerLayoutMode.GRID) }
    var selectedAppForDetails by remember { mutableStateOf<AppInfo?>(null) }

    val urvaraAppsCount = remember(apps) {
        apps.count { it.isAllowedInFocus || it.category == AppCategory.URVARA }
    }

    val urvaraShelfApps = remember(apps) {
        apps.filter { it.isAllowedInFocus || it.category == AppCategory.URVARA }.take(8)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_drawer_sheet"),
        color = DeepObsidian
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Modern App Drawer Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "APP DRAWER",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CardSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Text(
                                text = "${apps.size} apps",
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftSkyBlue,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "$urvaraAppsCount apps permitted in Urvarā (focus mode)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // View Toggle Button (Grid vs List)
                    IconButton(
                        onClick = {
                            layoutMode = if (layoutMode == DrawerLayoutMode.GRID) DrawerLayoutMode.LIST else DrawerLayoutMode.GRID
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CardSurface)
                            .border(1.dp, CardBorder, CircleShape)
                            .testTag("toggle_view_mode_button")
                    ) {
                        Icon(
                            imageVector = if (layoutMode == DrawerLayoutMode.GRID) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Switch View Mode",
                            tint = SoftSkyBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CardSurface)
                            .border(1.dp, CardBorder, CircleShape)
                            .testTag("close_app_drawer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Drawer",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Modern Floating Pill Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("drawer_search_input"),
                    placeholder = {
                        Text(
                            text = "Search applications & tools...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = SoftSkyBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(26.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardSurface.copy(alpha = 0.9f),
                        unfocusedContainerColor = CardSurface.copy(alpha = 0.7f),
                        focusedBorderColor = SoftSkyBlue,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            // Modern Pill Category Chips Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AppCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    ModernCategoryPill(
                        category = category,
                        isSelected = isSelected,
                        onClick = { onCategoryChange(category) }
                    )
                }
            }

            // Apps Grid or List View
            if (apps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CardSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = SoftSkyBlue.copy(alpha = 0.6f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No applications found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No results matching \"$searchQuery\"" else "No apps in this category",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (searchQuery.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = { onSearchChange("") },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Clear Search", color = SoftSkyBlue)
                            }
                        }
                    }
                }
            } else {
                when (layoutMode) {
                    DrawerLayoutMode.GRID -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("app_list"),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Top Urvarā Shelf when in ALL view and not searching
                            if (selectedCategory == AppCategory.ALL && searchQuery.isEmpty() && urvaraShelfApps.isNotEmpty()) {
                                item(span = { GridItemSpan(4) }) {
                                    UrvaraQuickShelf(
                                        apps = urvaraShelfApps,
                                        onAppClick = { app ->
                                            onAppClick(app)
                                            onDismiss()
                                        },
                                        onViewAllUrvara = { onCategoryChange(AppCategory.URVARA) }
                                    )
                                }

                                item(span = { GridItemSpan(4) }) {
                                    Text(
                                        text = "ALL APPLICATIONS",
                                        style = MaterialTheme.typography.labelSmall,
                                        letterSpacing = 1.2.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                                    )
                                }
                            }

                            items(apps, key = { it.packageName }) { app ->
                                ModernAppGridItem(
                                    app = app,
                                    onClick = {
                                        onAppClick(app)
                                        onDismiss()
                                    },
                                    onLongClick = {
                                        selectedAppForDetails = app
                                    }
                                )
                            }

                            item(span = { GridItemSpan(4) }) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    BkpWatermark(asPill = true)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }

                    DrawerLayoutMode.LIST -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("app_list"),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(apps, key = { it.packageName }) { app ->
                                ModernAppListItem(
                                    app = app,
                                    onAppClick = {
                                        onAppClick(app)
                                        onDismiss()
                                    },
                                    onToggleFavorite = { onToggleFavorite(app.packageName) },
                                    onToggleAllowedInFocus = { onToggleAllowedInFocus(app.packageName) }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    BkpWatermark(asPill = true)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Modern App Action Sheet / Dialog for Grid long-press
    selectedAppForDetails?.let { app ->
        AppQuickActionDialog(
            app = app,
            onDismiss = { selectedAppForDetails = null },
            onLaunch = {
                selectedAppForDetails = null
                onAppClick(app)
                onDismiss()
            },
            onToggleAllowed = {
                onToggleAllowedInFocus(app.packageName)
                val wasUrvara = app.category == AppCategory.URVARA && app.isAllowedInFocus
                val willBeUrvara = !wasUrvara
                selectedAppForDetails = app.copy(
                    category = if (willBeUrvara) AppCategory.URVARA else AppCategory.OTHER,
                    isAllowedInFocus = willBeUrvara,
                    isEssential = willBeUrvara
                )
            },
            onToggleFavorite = {
                onToggleFavorite(app.packageName)
                selectedAppForDetails = app.copy(isFavorite = !app.isFavorite)
            }
        )
    }
}

@Composable
private fun ModernCategoryPill(
    category: AppCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon: ImageVector = when (category) {
        AppCategory.ALL -> Icons.Default.Apps
        AppCategory.URVARA -> Icons.Default.Security
        AppCategory.STUDY -> Icons.Default.School
        AppCategory.WORK -> Icons.Default.Work
        AppCategory.COMMUNICATION -> Icons.Default.Chat
        AppCategory.ENTERTAINMENT -> Icons.Default.PlayArrow
        AppCategory.SOCIAL -> Icons.Default.People
        AppCategory.GAMES -> Icons.Default.SportsEsports
        AppCategory.OTHER -> Icons.Default.MoreHoriz
    }

    val containerColor = when {
        isSelected && category == AppCategory.URVARA -> CalmEmerald
        isSelected -> SoftSkyBlue
        else -> CardSurface
    }

    val contentColor = when {
        isSelected -> DeepObsidian
        category == AppCategory.URVARA -> CalmEmerald
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) androidx.compose.ui.graphics.Color.Transparent else CardBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.categoryTitle,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun UrvaraQuickShelf(
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onViewAllUrvara: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        color = CardSurface.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CalmEmerald)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "URVARĀ (FOCUS TOOLS)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = CalmEmerald
                    )
                }

                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftSkyBlue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onViewAllUrvara() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(apps, key = { it.packageName }) { app ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(58.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onAppClick(app) }
                            .padding(4.dp)
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
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ModernAppGridItem(
    app: AppInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val isUrvara = app.isAllowedInFocus || app.category == AppCategory.URVARA

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            AppIconView(
                packageName = app.packageName,
                label = app.label,
                category = app.category,
                size = 54.dp
            )

            // Top-right Urvarā Badge
            if (isUrvara) {
                Surface(
                    shape = CircleShape,
                    color = DeepObsidian,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CalmEmerald),
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Urvarā",
                            tint = CalmEmerald,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            // Bottom-right Favorite Star
            if (app.isFavorite) {
                Surface(
                    shape = CircleShape,
                    color = DeepObsidian,
                    border = androidx.compose.foundation.BorderStroke(1.dp, FocusAmber),
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorite",
                            tint = FocusAmber,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = app.label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ModernAppListItem(
    app: AppInfo,
    onAppClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleAllowedInFocus: () -> Unit
) {
    val isUrvara = app.isAllowedInFocus || app.category == AppCategory.URVARA

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable { onAppClick() },
        color = CardSurface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                AppIconView(
                    packageName = app.packageName,
                    label = app.label,
                    category = app.category,
                    size = 46.dp
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SlateNavy.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = app.category.categoryTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftSkyBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (isUrvara) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CalmEmerald.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CalmEmerald.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "Urvarā",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CalmEmerald,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick actions: Toggle Urvarā (for Urvarā apps only) & Toggle Favorite
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (app.category == AppCategory.URVARA) {
                    IconButton(
                        onClick = onToggleAllowedInFocus,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = if (isUrvara) "Remove from Urvarā" else "Add to Urvarā",
                            tint = if (isUrvara) CalmEmerald else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (app.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (app.isFavorite) "Remove from Favorites" else "Add to Favorites",
                        tint = if (app.isFavorite) FocusAmber else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppQuickActionDialog(
    app: AppInfo,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onToggleAllowed: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val isUrvara = app.category == AppCategory.URVARA && app.isAllowedInFocus

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepObsidian,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIconView(
                    packageName = app.packageName,
                    label = app.label,
                    category = app.category,
                    size = 42.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isUrvara) "Urvarā Focus Allowed" else app.category.categoryTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isUrvara) CalmEmerald else SoftSkyBlue
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Focus Shield & Urvarā Allowed Toggle (for ALL apps)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isUrvara) CalmEmerald.copy(alpha = 0.6f) else CardBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isUrvara) "Focus Shield: Allowed in Urvarā" else "Focus Shield: Blocked",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isUrvara) CalmEmerald else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isUrvara)
                                    "Permitted during Kendrīkaraṇa focus sessions & Urvarā shelf."
                                else
                                    "Strictly blocked in Kendrīkaraṇa. Toggle on to add to Urvarā.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = isUrvara,
                            onCheckedChange = { onToggleAllowed() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DeepObsidian,
                                checkedTrackColor = CalmEmerald,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = DeepObsidian
                            ),
                            modifier = Modifier.testTag("app_quick_action_urvara_switch")
                        )
                    }
                }

                // Favorite Toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Favorite App",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Pinned to home screen favorites",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = app.isFavorite,
                            onCheckedChange = { onToggleFavorite() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = FocusAmber,
                                checkedTrackColor = FocusAmber.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onLaunch,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SoftSkyBlue)
            ) {
                Text("Open App", color = DeepObsidian, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Done", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}
