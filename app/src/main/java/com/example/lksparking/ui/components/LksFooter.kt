package com.example.lksparking.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.lksparking.ui.navigation.Screen
import com.example.lksparking.ui.theme.LksOrange


@Composable
fun LksFooter(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        Triple(Screen.Map.route, Icons.Default.Map, "Map"),
        Triple(Screen.BookingsList.route, Icons.Default.CalendarMonth, "Bookings"),
        Triple(Screen.Alerts.route, Icons.Default.Notifications, "Alerts"),
        Triple(Screen.Profile.route, Icons.Default.Person, "Profile")
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