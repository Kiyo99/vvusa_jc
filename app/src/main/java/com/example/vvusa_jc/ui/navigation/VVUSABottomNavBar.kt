package com.example.vvusa_jc.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.vvusa_jc.R

/**
 * Represents an item in the bottom navigation bar
 */
data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: Int
)

/**
 * The items to be displayed in the bottom navigation bar
 */
object BottomNavItems {
    val items = listOf(
        BottomNavItem(
            route = Routes.HOME,
            title = "Home",
            icon = R.drawable.ic_home
        ),
        BottomNavItem(
            route = Routes.CAFETERIA,
            title = "Cafeteria",
            icon = R.drawable.ic_cafeteria
        ),
        BottomNavItem(
            route = Routes.HOSTEL,
            title = "Hostel",
            icon = R.drawable.ic_hostel
        ),
        BottomNavItem(
            route = Routes.WORKSTUDY,
            title = "Workstudy",
            icon = R.drawable.ic_work
        ),
        BottomNavItem(
            route = Routes.MARKETPLACE,
            title = "Market",
            icon = R.drawable.ic_shopping
        )
    )
}

@Composable
fun VVUSABottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Only show bottom nav bar if we're on a main screen
    val showBottomNav = BottomNavItems.items.any { item ->
        currentDestination?.route == item.route
    }

    if (showBottomNav) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            BottomNavItems.items.forEach { item ->
                AddItem(
                    item = item,
                    currentDestination = currentDestination,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun RowScope.AddItem(
    item: BottomNavItem,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    // Check if this is the currently selected item
    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

    NavigationBarItem(
        selected = selected,
        onClick = {
            navController.navigate(item.route) {
                // Pop up to the start destination of the graph to
                // avoid building up a large stack of destinations
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                // Avoid multiple copies of the same destination
                launchSingleTop = true
                // Restore state when reselecting a previously selected item
                restoreState = true
            }
        },
        icon = {
            Icon(
                painter = painterResource(id = item.icon),
                contentDescription = item.title
            )
        },
        label = { Text(text = item.title) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = Color.Gray,
            unselectedTextColor = Color.Gray,
            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}