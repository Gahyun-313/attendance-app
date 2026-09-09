package com.example.attendance.core.navigation

import com.example.attendance.feature.history.presentation.CorrectionViewModel

/** 문자열 경로와 인자 계약을 한곳에 모은다. Main은 로그인 이후 화면들의 상위 그래프다. */
sealed class ScreenRoute(val route: String) {
    data object Splash : ScreenRoute("splash")
    data object Login : ScreenRoute("login")
    data object Main : ScreenRoute("main")
    data object Home : ScreenRoute("home")
    data object Notification : ScreenRoute("notification")
    data object History : ScreenRoute("history")
    data object MyPage : ScreenRoute("mypage")
    data object Attendance : ScreenRoute("attendance")
    data object Correction : ScreenRoute("correction/{recordId}") {
        const val ARG_RECORD_ID = CorrectionViewModel.ARG_RECORD_ID
        /** 기록의 ID를 실제 이동 가능한 경로로 변환한다. */
        fun createRoute(recordId: Long) = "correction/$recordId"
    }
}
