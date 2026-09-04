package com.example.attendance.core.di

import com.example.attendance.core.data.repository.AttendanceRepository
import com.example.attendance.core.data.repository.AuthRepository
import com.example.attendance.core.data.repository.FakeAttendanceRepository
import com.example.attendance.core.data.repository.FakeAuthRepository
import com.example.attendance.core.data.repository.FakeNotificationRepository
import com.example.attendance.core.data.repository.NotificationRepository

/**
 * 앱 전역 의존성 컨테이너 (수동 DI)
 *
 * 구글 공식 가이드의 'Manual dependency injection' 패턴
 * Hilt 없이도 ViewModel이 인터페이스에만 의존하도록 만들어준다
 * 추후 이 컨테이너를 Hilt 모듈로 옮기면 됨
 */
interface AppContainer {
    val authRepository: AuthRepository
    val attendanceRepository: AttendanceRepository
    val notificationRepository: NotificationRepository
}

/**
 * 기본 구현체 (현재는 Fake Repository를 제공)
 *
 * 서버 연동 시 이곳에서 Retrofit 인스턴스를 만들고 실제 Repository 구현으로 교체하면 됨
 */
class DefaultAppContainer : AppContainer {
    // lazy: 실제로 처음 사용될 때 생성
    override val authRepository: AuthRepository by lazy {
        FakeAuthRepository()
    }

    override val attendanceRepository: AttendanceRepository by lazy {
        FakeAttendanceRepository()
    }

    override val notificationRepository: NotificationRepository by lazy {
        FakeNotificationRepository()
    }
}
