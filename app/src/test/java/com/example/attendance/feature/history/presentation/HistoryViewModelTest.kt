package com.example.attendance.feature.history.presentation

import app.cash.turbine.test
import com.example.attendance.MainDispatcherRule
import com.example.attendance.core.data.repository.AttendanceRepository
import com.example.attendance.core.model.AttendanceRecord
import com.example.attendance.core.model.AttendanceStatus
import com.example.attendance.core.model.ClassSession
import com.example.attendance.core.model.SemesterStats
import com.example.attendance.core.model.WeeklyStats
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth

class HistoryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun 초기_상태는_통계와_전체_기록을_표시한다() = runTest {
        val viewModel = HistoryViewModel(TestAttendanceRepository(), YearMonth.of(2025, 6))
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.stats.presentCount).isEqualTo(22)
            assertThat(state.records).hasSize(2)
            assertThat(state.selectedDay).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun 날짜를_선택하면_해당_날짜만_필터링하고_다시_누르면_전체로_돌아온다() = runTest {
        val viewModel = HistoryViewModel(TestAttendanceRepository(), YearMonth.of(2025, 6))
        viewModel.uiState.test {
            awaitItem()
            viewModel.selectDay(3)
            assertThat(awaitItem().records.map { it.dayOfMonth }).containsExactly(3)
            viewModel.selectDay(3)
            assertThat(awaitItem().records).hasSize(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun 달을_이동하면_선택한_날짜가_초기화된다() = runTest {
        val viewModel = HistoryViewModel(TestAttendanceRepository(), YearMonth.of(2025, 6))
        viewModel.uiState.test {
            awaitItem()
            viewModel.selectDay(3)
            awaitItem()
            viewModel.moveToNextMonth()
            val state = awaitItem()
            assertThat(state.currentMonth.monthValue).isEqualTo(7)
            assertThat(state.selectedDay).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private class TestAttendanceRepository : AttendanceRepository {
        private val records = MutableStateFlow(
            listOf(
                AttendanceRecord(1, "자료구조", "1교시", AttendanceStatus.PRESENT, dayOfMonth = 3),
                AttendanceRecord(2, "운영체제", "2교시", AttendanceStatus.LATE, dayOfMonth = 5)
            )
        )
        override fun getTodaySessions(): Flow<List<ClassSession>> = flowOf(emptyList())
        override fun getRecentRecords(): Flow<List<AttendanceRecord>> = flowOf(emptyList())
        override fun getHistoryRecords(): Flow<List<AttendanceRecord>> = records
        override fun getWeeklyStats(): Flow<WeeklyStats> = flowOf(WeeklyStats(0, 0, 0, 0))
        override fun getSemesterStats(): Flow<SemesterStats> = flowOf(SemesterStats(22, 3, 1, 88))
        override suspend fun getRecord(recordId: Long): AttendanceRecord? = records.value.find { it.id == recordId }
        override suspend fun checkAttendance(nfcTagUid: String): AttendanceStatus = AttendanceStatus.PRESENT
        override suspend fun requestCorrection(recordId: Long, reason: String, detail: String): Boolean = true
    }
}
