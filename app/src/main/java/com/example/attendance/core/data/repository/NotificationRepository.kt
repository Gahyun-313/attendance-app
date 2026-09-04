package com.example.attendance.core.data.repository

import com.example.attendance.core.model.NotificationItem
import com.example.attendance.core.model.SampleData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * 알림 데이터 소스 인터페이스
 */
interface NotificationRepository {

    /** 전체 알림 목록 */
    fun getNotifications(): Flow<List<NotificationItem>>

    /** 모든 알림을 읽음 처리 */
    suspend fun markAllAsRead()
}

/**
 * 서버 연동 전까지 사용하는 가짜 구현체 — [SampleData] 기반
 *
 * MutableStateFlow로 목록을 보관해 '모두 읽음 처리' 시
 * 이 Flow를 구독 중인 화면(알림/홈)이 자동 갱신된다.
 * TODO: FCM 푸시 + 서버 API 기반 실제 구현으로 교체
 */
class FakeNotificationRepository : NotificationRepository {

    private val notifications = MutableStateFlow(SampleData.notifications)

    override fun getNotifications(): Flow<List<NotificationItem>> = notifications

    override suspend fun markAllAsRead() {
        notifications.update { list -> list.map { it.copy(isRead = true) } }
    }
}