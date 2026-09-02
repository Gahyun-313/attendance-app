package com.example.attendance.core.model

/**
 * UI 개발용 샘플 데이터 모음
 *
 * 서버 연동 전 임시 데이터로 활용
 */
object SampleData {

    /** 홈 화면 */
    // 오늘 수업 세션 목록
    val todaySessions = listOf(
        ClassSession(
            id = 1L,
            subject = "데이터구조",
            timeRange = "09:00 - 10:30",
            room = "공학관 302호"
        ),
        ClassSession(
            id = 2L,
            subject = "운영체제",
            timeRange = "13:00 – 14:30",
            room = "공학관 305호",
            isActive = true // 현재 출석 체크가 가능한 세션
        ),
        ClassSession(
            id = 3L,
            subject = "알고리즘",
            timeRange = "15:30 – 17:00",
            room = "공학관 401호"
        ),
    )

    // 최근 출석 기록
    val recentRecords = listOf(
        AttendanceRecord(
            id = 1L,
            subject = "데이터구조",
            detail = "2교시",
            status = AttendanceStatus.PRESENT,
            checkedAt = "오늘 09:03"
        ),
        AttendanceRecord(
            id = 2L,
            subject = "운영체제",
            detail = "4교시",
            status = AttendanceStatus.ABSENT
        ),
        AttendanceRecord(
            id = 3L,
            subject = "알고리즘",
            detail = "5교시",
            status = AttendanceStatus.LATE,
            checkedAt = "3일 전 15:31"
        ),
    )

    /** 알림 화면 */
    val notifications = listOf(
        NotificationItem(
            id = 1L,
            title = "출석 확인 완료",
            content = "1교시 Java 프로그래밍 출석이 정상 처리되었습니다.",
            timeAgo = "방금 전"
        ),
        NotificationItem(
            id = 2L,
            title = "출석 마감 임박",
            content = "2교시 알고리즘 수업 출석 마감 10분 전입니다.",
            timeAgo = "5분 전"
        ),
        NotificationItem(
            id = 3L,
            title = "출석 누락 경고",
            content = "3교시 데이터베이스 수업 출석이 확인되지 않았습니다.",
            timeAgo = "1시간 전"
        ),
        NotificationItem(
            id = 4L,
            title = "공지사항",
            content = "이번 주 금요일 수업은 비대면으로 진행됩니다.",
            timeAgo = "3일 전",
            isRead = true
        ),
        NotificationItem(
            id = 5L,
            title = "출석 확인 완료",
            content = "운영체제 출석이 정상 처리되었습니다.",
            timeAgo = "5일 전",
            isRead = true
        ),
    )

    /** 내 출석 기록 화면 - 날짜별 기록 */
    val historyRecords = listOf(
        AttendanceRecord(
            id = 1L,
            subject = "데이터구조",
            detail = "",
            status = AttendanceStatus.PRESENT,
            dayOfMonth = 18
        ),
        AttendanceRecord(
            id = 2L,
            subject = "알고리즘",
            detail = "",
            status = AttendanceStatus.LATE,
            dayOfMonth = 20
        ),
        AttendanceRecord(
            id = 3L,
            subject = "데이터구조",
            detail = "",
            status = AttendanceStatus.PRESENT,
            dayOfMonth = 18
        ),
        AttendanceRecord(
            id = 4L,
            subject = "디지털논리회로",
            detail = "",
            status = AttendanceStatus.ABSENT,
            dayOfMonth = 20
        ),
    )
}