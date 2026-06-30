package com.lksnext.ParkingMMartinez.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lksnext.ParkingMMartinez.ui.navigation.Screen
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange
import com.lksnext.ParkingMMartinez.R
import androidx.compose.ui.platform.testTag
import com.lksnext.ParkingMMartinez.ui.constants.TestTags

@Composable
fun LksFooter(navController: NavController, modifier: Modifier = Modifier) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        Triple(Screen.Map.route, Icons.Default.Map, stringResource(R.string.footer_map)),
        Triple(Screen.BookingsList.route, Icons.Default.CalendarMonth, stringResource(R.string.footer_bookings)),
        Triple(Screen.Alerts.route, Icons.Default.Notifications, stringResource(R.string.footer_alerts)),
        Triple(Screen.Profile.route, Icons.Default.Person, stringResource(R.string.footer_profile))
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = modifier
    ) {
        items.forEach { (route, icon, label) ->
            val isSelected = isTabSelected(route, currentRoute)

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != route) {
                        handleNavigationClick(navController, route)
                    }
                },
                label = { Text(label) },
                icon = { Icon(icon, contentDescription = label) },
                modifier = Modifier.testTag("${TestTags.FOOTER_TAB_PREFIX}$route"),
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = LksOrange,
                    selectedTextColor = LksOrange,
                    indicatorColor = LksOrange.copy(alpha = 0.1f),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

// --- SUB-COMPONENTES ---

private fun isTabSelected(route: String, currentRoute: String?): Boolean {
    return if (route == Screen.Map.route) {
        currentRoute == Screen.Map.route ||
                (currentRoute?.startsWith("booking") == true && currentRoute != Screen.BookingsList.route)
    } else {
        currentRoute == route
    }
}

private fun handleNavigationClick(navController: NavController, targetRoute: String) {
    // Always use the standard pop-to-start + launchSingleTop navigation.
    // The previous special-case that called popBackStack() based on previousBackStackEntry
    // was fragile: if the back stack was [Map, BookingsList] (the state after confirming a
    // booking), previousBackStackEntry was "map" which did NOT start with "booking", but any
    // slight difference in back-stack state (e.g. on real devices) caused the wrong branch to
    // fire and the Map tab click had the same effect as the hardware back button instead of
    // navigating to Map properly.
    navigateToNormalTab(navController, targetRoute)
}

private fun navigateToNormalTab(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo(Screen.Map.route) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}