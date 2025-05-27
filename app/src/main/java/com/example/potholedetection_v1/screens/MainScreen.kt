package com.example.potholedetection_v1.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.potholedetection_v1.PotholeViewModel
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

    // Crear un ViewModel compartido
    val viewModel: PotholeViewModel = viewModel()

    val context = LocalContext.current

    // Añadir esta parte para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions.entries.any { it.key.contains("LOCATION") && it.value }

        if (!locationGranted) {
            Toast.makeText(
                context,
                "Location permission is required for the map to work properly",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Solicitar permisos al iniciar
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

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
                    HomeScreen(navController, viewModel)
                }
                composable(AppRoutes.MAP) {
                    MapScreen(viewModel)
                }
                composable(AppRoutes.HISTORY) {
                    HistoryScreen()
                }
                composable(AppRoutes.SETTINGS) {
                    SettingsScreen(viewModel)
                }
                composable(AppRoutes.EXPORT) {
                    ExportDataScreen()
                }
            }
        }
    }
}