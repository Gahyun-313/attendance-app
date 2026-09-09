package com.example.attendance.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

/** 하단 탭 노출 여부와 저장된 탭 상태 정리에 함께 사용하는 경로 목록이다. */
val mainTabRoutes = listOf(
    ScreenRoute.Home.route, ScreenRoute.Notification.route,
    ScreenRoute.History.route, ScreenRoute.MyPage.route
)

/**
 * 같은 탭 재선택은 무시하고 홈을 기준으로 탭 백스택을 정리한다.
 * 떠나는 탭의 상태를 저장하고 다시 선택할 때 복원해 ViewModel과 스크롤 상태를 유지한다.
 */
fun NavHostController.navigateToTab(route: String) {
    require(route in mainTabRoutes)
    if (currentDestination?.route == route) return
    navigate(route) {
        popUpTo(ScreenRoute.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** 현재 목적지를 관찰하며 홈·알림·기록·마이 탭과 선택 상태를 표시한다. */
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val entry by navController.currentBackStackEntryAsState()
    val labels = listOf("홈", "알림", "기록", "마이")
    val icons = listOf(Icons.Default.Home, Icons.Default.Notifications, Icons.AutoMirrored.Filled.List, Icons.Default.Person)
    NavigationBar {
        mainTabRoutes.forEachIndexed { index, route ->
            NavigationBarItem(
                selected = entry?.destination?.route == route,
                onClick = { navController.navigateToTab(route) },
                icon = { Icon(icons[index], contentDescription = null) },
                label = { Text(labels[index]) }
            )
        }
    }
}
