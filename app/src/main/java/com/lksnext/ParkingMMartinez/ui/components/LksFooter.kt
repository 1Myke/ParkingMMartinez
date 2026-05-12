package com.lksnext.ParkingMMartinez.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lksnext.ParkingMMartinez.ui.navigation.Screen
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange
import com.lksnext.ParkingMMartinez.R


@Composable
fun LksFooter(navController: NavController) {
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
        tonalElevation = 8.dp
    ) {
        items.forEach { (route, icon, label) ->
            val isSelected = currentRoute == route

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                label = { Text(label) },
                icon = {
                    Icon(icon, contentDescription = label)
                },
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