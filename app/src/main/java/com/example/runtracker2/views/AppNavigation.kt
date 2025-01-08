package com.example.runtracker2.views

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.runtracker2.ViewModel


@Composable
fun AppNavigation(viewModel: ViewModel,requestPermissions: () -> Unit, startDestination : String) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("SetupView") {
            SetupView(
                viewModel = viewModel,
                navigateToRun = { navController.navigate("RunView") }
            )
        }
        composable("RunView") {

            RunView(
                viewModel = viewModel,
                requestPermissions = {requestPermissions()},
                navigateBack = {navController.popBackStack()},
                navigateToStatistics ={navController.navigate("StatisticsView")},
                navigateToSettings ={navController.navigate("SettingsView")},
                navigateToTracking ={navController.navigate("TrackingView")},
            )
        }
        composable("StatisticsView"){
            StatisticsView(
                viewModel = viewModel,
                navigateBack = {navController.popBackStack()},
                navigateToStatisticsTotalView = {navController.navigate("StatisticsTotalView")}

            )
        }
        composable("SettingsView"){
            SettingsView(
                viewModel = viewModel,
                navigateBack = {navController.popBackStack()},
                navigateToRun = { navController.navigate("RunView") },
                navigateToStatistics ={navController.navigate("StatisticsView")}
            )
        }
        composable("TrackingView"){
            TrackingView(
                viewModel = viewModel,
                navigateBack = {navController.popBackStack()},
                navigateToStatistics ={navController.navigate("StatisticsView")}

            )
        }

        composable("StatisticsTotalView"){
            StatisticsTotalView(
                viewModel = viewModel,
                navigateBack = {navController.popBackStack()}

            )
        }
    }
}