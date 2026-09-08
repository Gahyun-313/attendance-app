# HistoryViewModel 줄별 해설

원본: `presentation/HistoryViewModel.kt`

package/import는 생략했다. 아래 코드는 실제 소스의 실행 순서를 유지하면서 각 줄의 역할을 설명한 것이다.

```kotlin
// 화면에 필요한 통계, 달력, 선택 날짜, 필터 결과를 하나의 불변 상태로 묶는다.
data class HistoryUiState(
    // Repository가 제공하는 학기 통계이며 조회 전에는 0으로 시작한다.
    val stats: SemesterStats = SemesterStats(0, 0, 0, 0),
    // 실제 앱은 현재 달에서 시작하고 테스트는 고정 달을 주입할 수 있다.
    val currentMonth: YearMonth = YearMonth.now(),
    // null이면 특정 날짜를 선택하지 않은 상태다.
    val selectedDay: Int? = null,
    // 달력에서 기록이 있는 날짜를 강조하기 위한 집합이다.
    val recordDays: Set<Int> = emptySet(),
    // 선택 날짜가 있으면 필터되고 없으면 전체 기록이 들어간다.
    val records: List<AttendanceRecord> = emptyList()
)

// 달 이동과 날짜 선택을 한 번에 갱신하기 위한 내부 상태다.
private data class CalendarSelection(
    // 현재 달을 보관한다.
    val month: YearMonth,
    // 현재 달에서 선택한 날짜를 보관한다.
    val selectedDay: Int? = null
)

// ViewModel 수명 동안 상태와 사용자 이벤트를 관리한다.
class HistoryViewModel(
    // 구체적인 Fake가 아니라 출석 데이터 계약만 받는다.
    private val attendanceRepository: AttendanceRepository,
    // 운영 화면은 현재 달을 사용하고 테스트는 고정 값을 전달한다.
    initialMonth: YearMonth = YearMonth.now()
) : ViewModel() {
    // 달과 선택일을 하나의 Flow로 묶어 중간 조합 상태의 방출을 막는다.
    private val calendarSelection = MutableStateFlow(CalendarSelection(initialMonth))

    // 통계 Flow, 기록 Flow, 달력 상태를 최신 값 기준으로 결합한다.
    val uiState: StateFlow<HistoryUiState> = combine(
        // 학기 통계의 최신 값을 받는다.
        attendanceRepository.getSemesterStats(),
        // 전체 출석 기록의 최신 값을 받는다.
        attendanceRepository.getHistoryRecords(),
        // 사용자의 달력 조작 상태를 받는다.
        calendarSelection
    ) { stats, records, calendar ->
        // 결합된 내부 상태에서 화면에 필요한 값을 꺼낸다.
        val month = calendar.month
        val day = calendar.selectedDay
        // Repository 데이터와 선택 상태를 화면 상태로 변환한다.
        HistoryUiState(
            // 학기 통계를 그대로 전달한다.
            stats = stats,
            // 현재 달을 전달한다.
            currentMonth = month,
            // 현재 선택일을 전달한다.
            selectedDay = day,
            // 기록이 존재하는 날짜를 집합으로 만들어 달력에 표시한다.
            recordDays = records.map { it.dayOfMonth }.toSet(),
            // 선택일이 있으면 같은 날짜의 기록만 남기고, 없으면 전체 기록을 유지한다.
            records = day?.let { selected -> records.filter { it.dayOfMonth == selected } } ?: records
        )
    // ViewModel 범위에서 수집하고 구독자가 없을 때도 마지막 상태를 잠시 유지한다.
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    // 날짜를 다시 누르면 null로 바꾸고, 다른 날짜면 새 날짜를 선택한다.
    fun selectDay(day: Int) {
        calendarSelection.update { current -> current.copy(selectedDay = if (current.selectedDay == day) null else day) }
    }

    // 이전 달과 선택 해제를 한 번의 StateFlow 갱신으로 처리한다.
    fun moveToPreviousMonth() {
        calendarSelection.update { current -> CalendarSelection(current.month.minusMonths(1)) }
    }

    // 다음 달과 선택 해제를 한 번의 StateFlow 갱신으로 처리한다.
    fun moveToNextMonth() {
        calendarSelection.update { current -> CalendarSelection(current.month.plusMonths(1)) }
    }
}
```

핵심은 `combine` 자체보다 상태의 소유 경계다. Repository Flow는 외부 데이터이고 `CalendarSelection`은 사용자 입력이다. 둘을 결합한 결과만 UI에 노출하므로 Content는 Repository나 MutableStateFlow를 직접 알지 않는다.
