package com.example.attendance.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.attendance.AttendanceApp
import com.example.attendance.core.data.repository.AttendanceRepository
import com.example.attendance.core.model.AttendanceRecord
import com.example.attendance.core.model.SemesterStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.YearMonth

/** 내 출석 기록 화면에서 표시할 통계, 달력, 필터 결과를 하나로 묶은 상태. */
data class HistoryUiState(
    val stats: SemesterStats = SemesterStats(0, 0, 0, 0),
    val currentMonth: YearMonth = YearMonth.of(2025, 6),
    val selectedDay: Int? = null,
    val recordDays: Set<Int> = emptySet(),
    val records: List<AttendanceRecord> = emptyList()
)

private data class CalendarSelection(
    val month: YearMonth,
    val selectedDay: Int? = null
)

/**
 * Repository의 출석 데이터와 사용자의 달력 조작 상태를 결합한다.
 * 날짜를 다시 누르면 선택을 해제하고, 달을 이동하면 날짜 선택을 초기화한다.
 */
class HistoryViewModel(
    private val attendanceRepository: AttendanceRepository,
    initialMonth: YearMonth = YearMonth.now()
) : ViewModel() {

    // 실제 화면은 현재 연월에서 시작하고, 테스트는 고정 연월을 주입해 재현성을 유지한다.
    private val calendarSelection = MutableStateFlow(CalendarSelection(initialMonth))

    val uiState: StateFlow<HistoryUiState> = combine(
        attendanceRepository.getSemesterStats(),
        attendanceRepository.getHistoryRecords(),
        calendarSelection
    ) { stats, records, calendar ->
        val month = calendar.month
        val day = calendar.selectedDay
        HistoryUiState(
            stats = stats,
            currentMonth = month,
            selectedDay = day,
            recordDays = records.map { it.dayOfMonth }.toSet(),
            records = day?.let { selected ->
                records.filter { it.dayOfMonth == selected }
            } ?: records
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState()
    )

    /** 같은 날짜를 다시 누르면 선택을 해제해 전체 기록을 표시한다. */
    fun selectDay(day: Int) {
        calendarSelection.update { current ->
            current.copy(selectedDay = if (current.selectedDay == day) null else day)
        }
    }

    /** 이전 달로 이동하고 현재 날짜 선택을 초기화한다. */
    fun moveToPreviousMonth() {
        calendarSelection.update { current ->
            CalendarSelection(month = current.month.minusMonths(1))
        }
    }

    /** 다음 달로 이동하고 현재 날짜 선택을 초기화한다. */
    fun moveToNextMonth() {
        calendarSelection.update { current ->
            CalendarSelection(month = current.month.plusMonths(1))
        }
    }

    companion object {
        /** Activity의 AppContainer에서 Repository를 받아 ViewModel을 생성한다. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as AttendanceApp
                HistoryViewModel(app.container.attendanceRepository)
            }
        }
    }
}
