package com.example.attendance.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.attendance.AttendanceApp
import com.example.attendance.core.data.repository.AttendanceRepository
import com.example.attendance.core.data.repository.NotificationRepository
import com.example.attendance.core.model.AttendanceRecord
import com.example.attendance.core.model.ClassSession
import com.example.attendance.core.model.NotificationItem
import com.example.attendance.core.model.WeeklyStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * 홈 화면 UI 상태
 *
 * @property weeklyStats         이번 주 출석 통계
 * @property todaySessions       오늘 수업 세션 목록
 * @property unreadNotifications 읽지 않은 알림 (최근 2건)
 * @property recentRecords       최근 출석 기록
 */
data class HomeUiState(
    val weeklyStats: WeeklyStats = WeeklyStats(0, 0, 0, 0),
    val todaySessions: List<ClassSession> = emptyList(),
    val unreadNotifications: List<NotificationItem> = emptyList(),
    val recentRecords: List<AttendanceRecord> = emptyList()
)

/**
 * 홈 화면 ViewModel
 *
 * 여러 Repository의 Flow를 combine으로 합쳐 하나의 UiState로 노출한다.
 * Repository 데이터가 변경되면(예: 알림 읽음 처리) 화면이 자동 갱신된다.
 */
class HomeViewModel(
    attendanceRepository: AttendanceRepository,
    notificationRepository: NotificationRepository
) : ViewModel() {

    /** 홈 화면 상태 — 4개의 Flow를 합쳐 구성 */
    val uiState: StateFlow<HomeUiState> = combine(
        attendanceRepository.getWeeklyStats(),
        attendanceRepository.getTodaySessions(),
        notificationRepository.getNotifications(),
        attendanceRepository.getRecentRecords()
    ) { stats, sessions, notifications, records ->
        HomeUiState(
            weeklyStats = stats,
            todaySessions = sessions,
            // Repository 순서를 유지하며 읽지 않은 알림 중 앞의 2건만 홈에 노출한다.
            unreadNotifications = notifications.filter { !it.isRead }.take(2),
            recentRecords = records
        )
    }.stateIn(
        scope = viewModelScope,
        // 마지막 구독자가 사라진 뒤 5초간 수집을 유지한다. 값은 기본 정책에 따라 캐시된다.
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    companion object {
        /** AppContainer에서 Repository를 꺼내 ViewModel을 생성하는 Factory */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as AttendanceApp
                HomeViewModel(
                    attendanceRepository = app.container.attendanceRepository,
                    notificationRepository = app.container.notificationRepository
                )
            }
        }
    }
}
