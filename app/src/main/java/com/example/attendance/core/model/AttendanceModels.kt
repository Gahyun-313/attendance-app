package com.example.attendance.core.model

import androidx.compose.ui.graphics.Color
import com.example.attendance.ui.theme.*

/**
 * 출석 상태를 나타내는 열거형
 * - '내 출석 기록', '홈-최근 출석 기록', '출석 체크 화면'
 */
enum class AttendanceStatus(
    val label: String,  // 화면 표시용 한글 라벨
    val color: Color,   // 상태 강조 색
    val canRequestCorrection: Boolean   // 정정 요청 버튼 노출 여부 (지각/결석만 가능)
) {
    PRESENT(label = "출석", color = White, canRequestCorrection = false),
    LATE(label = "지각", color = Red50, canRequestCorrection = true),
    ABSENT(label = "결석", color = Red50, canRequestCorrection = true),
    NOT_YET(label = "미출석", color = White, canRequestCorrection = false),
}

/**
 * 오늘 수업 세션 정보
 * - '홈-오늘의 수업 세션', 'NFC 인증/출석 결과 모달', '출석 체크 화면'
 */
data class ClassSession(
    val id: Long,           // 세션 고유 ID
    val subject: String,    // 과목명
    val timeRange: String,  // 수업 시간
    val room: String,       // 강의실
    val isActive: Boolean = false   // 현재 출석 체크 가능한 세션인지 여부
)

/**
 * 출석 기록 한 건
 * - '홈-최근 출석 기록', '내 출석 기록-날짜별 기록'
 */
data class AttendanceRecord(
    val id: Long,           // 기록 고유 ID
    val subject: String,    // 과목명
    val detail: String,     // 보조 정보 (ex. n교시)
    val status: AttendanceStatus,   // 출석 상태
    val checkedAt: String? = null,  // 체크된 시각 표시 문자열
    val dayOfMonth: Int = 1, // 기록이 속한 날짜 (달력 표시용, 1~31)
    val correctionRequested: Boolean = false // 정정요청을 이미 제출했는지 여부
)

/**
 * 알림 한 건
 * - '알림', '홈-읽지 않은 알림'
 */
data class NotificationItem(
    val id: Long,           // 알림 고유 ID
    val title: String,      // 알림 제목
    val content: String,    // 알림 본문
    val timeAgo: String,    // 상대 시간 표시
    val isRead: Boolean = false     // 읽음 여부
)

/**
 * 이번 주 출석 통계
 * - '홈-이번 주 출석 현황'
 */
data class WeeklyStats(
    val attendedCount: Int,     // 이번 주 출석 횟수
    val totalCount: Int,        // 이번 주 전체 세션 수
    val attendanceRate: Int,    // 활성 기간 출석률 (%)
    val remainingSessions: Int  // 오늘 남은 세션 수
)

/**
 * 이번 학기 출석 통계
 * - '내 출석 기록-이번 학기 출석 현황'
 */
data class SemesterStats(
    val presentCount: Int,  // 출석 횟수
    val lateCount: Int,     // 지각 횟수
    val absentCount: Int,   // 결석 횟수
    val attendanceRate: Int // 출석률 (%)
)
