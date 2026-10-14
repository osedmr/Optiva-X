package com.optivax.app.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.optivax.app.presentation.apps.AppManagerScreen
import com.optivax.app.presentation.dashboard.DashboardScreen
import com.optivax.app.presentation.files.FilesCleanScreen
import com.optivax.app.presentation.files.FilesHomeScreen
import com.optivax.app.presentation.files.FilesCategoryTab
import com.optivax.app.presentation.gallery.GalleryScreen
import com.optivax.app.presentation.theme.OptivaColors

data class NavigationItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

val navigationItems = listOf(
    NavigationItem(Screen.Dashboard, Icons.Default.Speed, "Ana Sayfa"),
    NavigationItem(Screen.GalleryClean, Icons.Default.PhotoLibrary, "Fotoğraflar"),
    NavigationItem(Screen.FilesClean, Icons.Default.FolderZip, "Dosyalar"),
    NavigationItem(Screen.AppManager, Icons.Default.Apps, "Uygulamalar")
)

/**
 * Floating Glassmorphic Pill Navigation Bar with 4 ergonomic tabs.
 * Sits safely above the system navigation bar with [navigationBarsPadding].
 */
@Composable
fun OptivaFloatingBottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(OptivaColors.SurfaceCard.copy(alpha = 0.96f))
                .border(
                    width = 1.dp,
                    color = OptivaColors.BorderSubtle,
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navigationItems.forEach { item ->
                val isSelected = currentRoute == item.screen.route ||
                    (item.screen == Screen.FilesClean && currentRoute?.startsWith("file_category/") == true)

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) OptivaColors.BrandPrimary else OptivaColors.TextMuted,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "NavIconColor"
                )

                val pillBackground by animateColorAsState(
                    targetValue = if (isSelected) OptivaColors.BrandPrimaryContainer else Color.Transparent,
                    label = "NavPillBackground"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(pillBackground)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (currentRoute != item.screen.route) {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            color = iconColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OptivaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToGallery = {
                    navController.navigate(Screen.GalleryClean.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToFiles = {
                    navController.navigate(Screen.FilesClean.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToAppManager = {
                    navController.navigate(Screen.AppManager.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Screen.GalleryClean.route) {
            GalleryScreen()
        }

        composable(Screen.FilesClean.route) {
            FilesHomeScreen { category -> navController.navigate("file_category/${category.name}") }
        }

        composable("file_category/{category}") { entry ->
            val category = FilesCategoryTab.entries.firstOrNull {
                it.name == entry.arguments?.getString("category")
            }
            if (category != null) {
                FilesCleanScreen(selectedCategoryTab = category, onBack = { navController.popBackStack() })
            }
        }

        // Backward compatibility route
        composable(Screen.StorageClean.route) {
            FilesHomeScreen { category -> navController.navigate("file_category/${category.name}") }
        }

        composable(Screen.AppManager.route) {
            AppManagerScreen()
        }
    }
}
