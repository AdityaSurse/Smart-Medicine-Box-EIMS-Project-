package com.aditya.smartmedbox.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aditya.smartmedbox.core.designsystem.theme.LocalSpacing
import com.aditya.smartmedbox.core.designsystem.theme.PillShape

object BottomNavigationBar {
    enum class Destination(
        val route: Screen,
        val icon: ImageVector,
        val label: String
    ) {
        HOME(Screen.Dashboard, Icons.Outlined.LocalPharmacy, "Home"),
        SCHEDULE(Screen.Schedule, Icons.Outlined.CalendarToday, "Schedule"),
        HISTORY(Screen.History, Icons.Outlined.BarChart, "History"),
        SETTINGS(Screen.Settings, Icons.Outlined.Tune, "Settings")
    }
}

typealias TopLevelDestination = BottomNavigationBar.Destination

@Composable
fun SmartMedBoxBottomBar(navController: NavController, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        HorizontalDivider(
            thickness = spacing.hairlineBorder,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing.bottomNavHeight)
                .padding(horizontal = spacing.gutterMobile),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TopLevelDestination.entries.forEach { destination ->
                val isSelected = currentDestination?.hasRoute(destination.route::class) == true
                val interactionSource = remember { MutableInteractionSource() }

                val itemColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(spacing.bottomNavHeight)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            role = Role.Tab,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        .semantics {
                            role = Role.Tab
                            selected = isSelected
                            contentDescription = destination.label
                        }
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null,
                        tint = itemColor,
                        modifier = Modifier.size(spacing.large)
                    )
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = itemColor,
                        modifier = Modifier.padding(top = spacing.micro)
                    )
                    // Stitch Tactile Active Dot Indicator (w-1 h-1 rounded-full bg-primary mt-1)
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .padding(top = spacing.extraSmall)
                                .size(spacing.extraSmall)
                                .clip(PillShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    } else {
                        Spacer(
                            modifier = Modifier
                                .padding(top = spacing.extraSmall)
                                .size(spacing.extraSmall)
                        )
                    }
                }
            }
        }
    }
}
