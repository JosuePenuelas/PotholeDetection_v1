package com.example.potholedetection_v1.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.text.font.FontWeight
import com.example.potholedetection_v1.navigation.AppRoutes
import com.example.potholedetection_v1.navigation.DrawerContent
import com.example.potholedetection_v1.navigation.DrawerItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // List of drawer items
    val items = listOf(
        DrawerItem(
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Filled.Home,
            route = AppRoutes.MAIN
        ),
        DrawerItem(
            title = "Map",
            selectedIcon = Icons.Filled.Place,
            unselectedIcon = Icons.Filled.Place,
            route = AppRoutes.MAP
        ),
        DrawerItem(
            title = "History",
            selectedIcon = Icons.Filled.DateRange,
            unselectedIcon = Icons.Filled.DateRange,
            route = AppRoutes.HISTORY
        ),
        DrawerItem(
            title = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Filled.Settings,
            route = AppRoutes.SETTINGS
        ),
        DrawerItem(
            title = "Export Data",
            selectedIcon = Icons.Filled.Share,
            unselectedIcon = Icons.Filled.Share,
            route = AppRoutes.EXPORT
        )
    )

    val navigationBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navigationBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerContent = {
            DrawerContent(
                items = items,
                currentRoute = currentRoute,
                onItemClick = { item ->
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentRoute) {
                                AppRoutes.MAIN -> "Pothole Detector"
                                AppRoutes.MAP -> "Map View"
                                AppRoutes.HISTORY -> "Detection History"
                                AppRoutes.SETTINGS -> "Settings"
                                AppRoutes.EXPORT -> "Export Data"
                                else -> "Pothole Detector"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = AppRoutes.MAIN,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(AppRoutes.MAIN) {
                    HomeScreen(navController)
                }
                composable(AppRoutes.MAP) {
                    MapScreen()
                }
                composable(AppRoutes.HISTORY) {
                    HistoryScreen()
                }
                composable(AppRoutes.SETTINGS) {
                    SettingsScreen()
                }
                composable(AppRoutes.EXPORT) {
                    ExportDataScreen()
                }
            }
        }
    }
}