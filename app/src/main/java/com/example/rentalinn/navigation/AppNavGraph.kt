package com.example.rentalinn.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.rentalinn.screens.admin.AdminDashboardScreen
import com.example.rentalinn.screens.admin.vehicle.AddVehicleScreen
import com.example.rentalinn.screens.admin.vehicle.EditVehicleScreen
import com.example.rentalinn.screens.admin.vehicle.VehicleDetailScreen
import com.example.rentalinn.screens.admin.vehicle.VehicleLandingScreen
import com.example.rentalinn.screens.login.LoginScreen
import com.example.rentalinn.screens.onboarding.OnboardingScreen
import com.example.rentalinn.screens.register.RegisterScreen
import com.example.rentalinn.screens.splash.SplashScreen
import com.example.rentalinn.screens.user.UserDashboardScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object AdminDashboard : Screen("admin_dashboard")
    object UserDashboard : Screen("user_dashboard")
    object VehicleLanding : Screen("vehicle_landing")
    object AddVehicle : Screen("add_vehicle")
    object EditVehicle : Screen("edit_vehicle/{vehicleId}")
    object VehicleDetail : Screen("vehicle_detail/{vehicleId}")
    
    fun createRoute(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                route.replace("{$arg}", arg)
            }
        }
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToAdminDashboard = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToUserDashboard = {
                    navController.navigate(Screen.UserDashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.UserDashboard.route) {
            UserDashboardScreen(
                navController = navController,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.UserDashboard.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Vehicle routes
        composable(Screen.VehicleLanding.route) {
            VehicleLandingScreen(navController = navController)
        }
        
        composable(Screen.AddVehicle.route) {
            AddVehicleScreen(navController = navController)
        }
        
        composable(
            route = Screen.EditVehicle.route,
            arguments = listOf(
                navArgument("vehicleId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getInt("vehicleId") ?: 0
            EditVehicleScreen(vehicleId = vehicleId, navController = navController)
        }
        
        composable(
            route = Screen.VehicleDetail.route,
            arguments = listOf(
                navArgument("vehicleId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getInt("vehicleId") ?: 0
            VehicleDetailScreen(vehicleId = vehicleId, navController = navController)
        }
    }
}
