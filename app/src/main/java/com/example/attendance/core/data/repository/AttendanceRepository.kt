package com.example.attendance.core.data.repository

import com.example.attendance.core.model.AttendanceRecord
import com.example.attendance.core.model.AttendanceStatus
import com.example.attendance.core.model.ClassSession
import com.example.attendance.core.model.SampleData
import com.example.attendance.core.model.SemesterStats
import com.example.attendance.core.model.WeeklyStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

/**
 * 출석 관련 데이터 소스 인터페이스
 *
 * 조회는 Flow로 노출해 데이터 변경 시 UI가 자동 갱신되도록 하고,
 * 변경(출석 체크, 정정 요청)은 suspend 함수로 정의한다.
 */
interface AttendanceRepository {

    /** 오늘 수업 세션 목록 */
    fun getTodaySessions(): Flow<List<ClassSession>>

    /** 최근 출석 기록 (홈 화면) */
    fun getRecentRecords(): Flow<List<AttendanceRecord>>

    /** 학기 전체 출석 기록 (내 출석 기록 화면) */
    fun getHistoryRecords(): Flow<List<AttendanceRecord>>

    /** 이번 주 출석 통계 (홈 화면) */
    fun getWeeklyStats(): Flow<WeeklyStats>

    /** 이번 학기 출석 통계 (내 출석 기록 화면) */
    fun getSemesterStats(): Flow<SemesterStats>

    /** ID로 출석 기록 단건 조회 (정정 요청 화면) */
    suspend fun getRecord(recordId: Long): AttendanceRecord?

    /**
     * NFC 출석 체크 수행
     *
     * 세션은 서버가 태그에 연결된 현재 ACTIVE 세션을 역추적하므로
     * 클라이언트가 sessionId를 넘기지 않고 스캔된 NFC 태그의 UID만 전달한다.
     *
     * @param nfcTagUid 스캔된 NFC 태그의 UID
     * @return 처리된 출석 상태 (출석/지각 등)
     */
    suspend fun checkAttendance(nfcTagUid: String): AttendanceStatus

    /**
     * 출석 정정 요청 제출
     * @return 접수 성공 여부
     */
    suspend fun requestCorrection(recordId: Long, reason: String, detail: String): Boolean
}

/**
 * 서버 연동 전까지 사용하는 가짜 구현체 — [SampleData] 기반
 *
 * 기록 목록은 MutableStateFlow로 들고 있어
 * 출석 체크/정정 요청 시 목록이 갱신되면 화면도 자동으로 반영된다.
 * TODO: Retrofit API + Room 캐시 기반 실제 구현으로 교체
 */
class FakeAttendanceRepository : AttendanceRepository {

    // 화면 갱신을 위해 기록을 상태로 보관한다.
    private val historyRecords = MutableStateFlow(SampleData.historyRecords)

    override fun getTodaySessions(): Flow<List<ClassSession>> =
        flowOf(SampleData.todaySessions)

    override fun getRecentRecords(): Flow<List<AttendanceRecord>> =
        flowOf(SampleData.recentRecords)

    override fun getHistoryRecords(): Flow<List<AttendanceRecord>> = historyRecords

    override fun getWeeklyStats(): Flow<WeeklyStats> = flowOf(
        WeeklyStats(
            attendedCount = 3,
            totalCount = 10,
            attendanceRate = 55,
            remainingSessions = 2
        )
    )

    override fun getSemesterStats(): Flow<SemesterStats> = flowOf(
        SemesterStats(
            presentCount = 22,
            lateCount = 3,
            absentCount = 1,
            attendanceRate = 88
        )
    )

    override suspend fun getRecord(recordId: Long): AttendanceRecord? =
        historyRecords.value.find { it.id == recordId }

    override suspend fun checkAttendance(nfcTagUid: String): AttendanceStatus {
        delay(2_000) // NFC 태깅 시간 시뮬레이션
        return AttendanceStatus.PRESENT
    }

    override suspend fun requestCorrection(
        recordId: Long,
        reason: String,
        detail: String
    ): Boolean {
        delay(500) // 서버 접수 시뮬레이션
        return true
    }
}
