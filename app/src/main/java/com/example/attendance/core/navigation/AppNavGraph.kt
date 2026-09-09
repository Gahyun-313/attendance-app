package com.example.attendance.core.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navigation
import androidx.navigation.navArgument
import com.example.attendance.feature.attendance.presentation.AttendanceScreen
import com.example.attendance.feature.auth.presentation.LoginScreen
import com.example.attendance.feature.history.presentation.AttendanceHistoryScreen
import com.example.attendance.feature.history.presentation.CorrectionRequestScreen
import com.example.attendance.feature.home.presentation.HomeScreen
import com.example.attendance.feature.mypage.presentation.MyPageScreen
import com.example.attendance.feature.notification.presentation.NotificationScreen
import com.example.attendance.feature.splash.presentation.SplashScreen

/**
 * 앱의 여덟 화면을 연결하고 탭 화면에만 하단 바를 배치한다.
 * Main 하위 그래프는 로그아웃 시 제거되며, 인증 Repository 처리는 3-8 연습 과제로 남긴다.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = ScreenRoute.Splash.route
) {
    val entry by navController.currentBackStackEntryAsState()
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (entry?.destination?.route in mainTabRoutes) BottomNavigationBar(navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding)
        ) {
            composable(ScreenRoute.Splash.route) {
                SplashScreen(onTimeout = {
                    navController.navigate(ScreenRoute.Login.route) {
                        popUpTo(ScreenRoute.Splash.route) { inclusive = true }
                        launchSingleTop = true
                    }
                })
            }
            composable(ScreenRoute.Login.route) {
                LoginScreen(onLoginSuccess = {
                    navController.navigate(ScreenRoute.Main.route) {
                        popUpTo(ScreenRoute.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                })
            }
            navigation(startDestination = ScreenRoute.Home.route, route = ScreenRoute.Main.route) {
                composable(ScreenRoute.Home.route) {
                    HomeScreen(
                        onCheckAttendance = { navController.navigate(ScreenRoute.Attendance.route) { launchSingleTop = true } },
                        onMoreNotifications = { navController.navigateToTab(ScreenRoute.Notification.route) },
                        onMoreRecords = { navController.navigateToTab(ScreenRoute.History.route) }
                    )
                }
                composable(ScreenRoute.Notification.route) {
                    NotificationScreen(navController = navController)
                }
                composable(ScreenRoute.History.route) {
                    AttendanceHistoryScreen(onRequestCorrection = { recordId ->
                        navController.navigate(ScreenRoute.Correction.createRoute(recordId)) { launchSingleTop = true }
                    })
                }
                composable(ScreenRoute.MyPage.route) {
                    MyPageScreen(onLogout = {
                        mainTabRoutes.forEach { navController.clearBackStack(it) }
                        navController.navigate(ScreenRoute.Login.route) {
                            popUpTo(ScreenRoute.Main.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    })
                }
                composable(ScreenRoute.Attendance.route) {
                    AttendanceScreen(navController = navController)
                }
                composable(
                    route = ScreenRoute.Correction.route,
                    arguments = listOf(navArgument(ScreenRoute.Correction.ARG_RECORD_ID) { type = NavType.LongType })
                ) {
                    CorrectionRequestScreen(
                        onBack = { navController.popBackStack() },
                        onSubmitted = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
