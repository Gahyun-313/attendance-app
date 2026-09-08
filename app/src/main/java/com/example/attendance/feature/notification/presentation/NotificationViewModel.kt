package com.example.attendance.feature.notification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.attendance.AttendanceApp
import com.example.attendance.core.data.repository.NotificationRepository
import com.example.attendance.core.model.NotificationItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 알림 화면 UI 상태
 *
 * @property unread 읽지 않은 알림 목록
 * @property read   읽은 알림 목록
 */
data class NotificationUiState(
    val unread: List<NotificationItem> = emptyList(),
    val read: List<NotificationItem> = emptyList()
) {
    /** 알림이 하나도 없는지 여부 (빈 상태 표시용) */
    val isEmpty: Boolean get() = unread.isEmpty() && read.isEmpty()
}

/**
 * 알림 화면 ViewModel
 *
 * Repository의 알림 Flow를 읽음/안읽음으로 분류해 노출하고,
 * '모두 읽음 처리' 이벤트를 Repository에 위임한다.
 */
class NotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    /** 알림 목록 상태 — Repository 변경 시 자동 갱신 */
    val uiState: StateFlow<NotificationUiState> =
        notificationRepository.getNotifications()
            .map { list ->
                NotificationUiState(
                    unread = list.filter { !it.isRead },
                    read = list.filter { it.isRead }
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NotificationUiState()
            )

    /** '모두 읽음 처리' 클릭 */
    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }

    companion object {
        /** AppContainer에서 Repository를 꺼내 ViewModel을 생성하는 Factory */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as AttendanceApp
                NotificationViewModel(notificationRepository = app.container.notificationRepository)
            }
        }
    }
}
