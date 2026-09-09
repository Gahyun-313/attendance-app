# AttendanceHistoryScreen 줄별 해설

원본: `presentation/AttendanceHistoryScreen.kt`

## Screen

```kotlin
// ViewModel을 생성하고 최신 상태를 생명주기와 함께 수집한다.
@Composable
fun AttendanceHistoryScreen(
    // 행에서 올라온 ID의 실제 이동은 AppNavGraph에 위임한다.
    onRequestCorrection: (Long) -> Unit = {},
    // 수동 DI Factory를 사용해 Activity의 AppContainer에서 Repository를 전달받는다.
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    // StateFlow의 현재 값을 읽고 STARTED 이상에서만 수집한다.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // 화면 상태와 이벤트 함수를 상태 없는 Content에 전달한다.
    HistoryContent(
        uiState = uiState,
        onPrevMonth = viewModel::moveToPreviousMonth,
        onNextMonth = viewModel::moveToNextMonth,
        onDayClick = viewModel::selectDay,
        // 자체 Scaffold가 없는 Screen에 상태바 여백을 적용한다.
        modifier = Modifier.statusBarsPadding(),
        // 정정 이동 콜백을 Content까지 전달한다.
        onRequestCorrection = onRequestCorrection
    )
}
```

## Content

`HistoryContent`는 제목 → 통계 카드 → 달력 → 날짜별 기록 순서로 그린다. `uiState.records`를 반복해 `HistoryRecordRow`를 호출하며, 정정 버튼의 ID를 외부 콜백으로 전달하고 실제 이동은 3-9 AppNavGraph가 담당한다. 변경 코드 전체는 HISTORY_NAVIGATION_NOTES.md를 참고한다.

```kotlin
// 외부 상태와 콜백만 받아 Preview와 UI 테스트에서 직접 호출할 수 있다.
@Composable
fun HistoryContent(
    uiState: HistoryUiState,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    // 정정 버튼에서 클릭한 행의 Long ID를 전달한다.
    onRequestCorrection: (Long) -> Unit = {}
) {
    // 긴 기록 목록을 세로로 스크롤할 수 있는 화면 열을 만든다.
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // 제목을 표시한다.
        Text("내 출석 기록")
        // 학기 통계 네 항목을 공통 컴포넌트로 표시한다.
        StatSummaryRow(items = listOf(/* 출석, 지각, 결석, 출석률 */))
        // 현재 연월과 날짜 선택 상태를 달력에 전달한다.
        MonthCalendar(uiState.currentMonth, uiState.selectedDay, uiState.recordDays, onPrevMonth, onNextMonth, onDayClick)
        // 선택 결과로 만들어진 기록 목록을 표시한다.
        uiState.records.forEach { record -> HistoryRecordRow(record = record, onRequestCorrection = { onRequestCorrection(record.id) }) }
    }
}
```

## 달력 계산

`LocalDate.of(month.year, month.monthValue, 1)`로 해당 월 1일의 요일을 구하고, 일요일 시작 기준 오프셋만큼 빈 칸을 둔 뒤 날짜를 7개씩 행으로 배치한다. `selectedDay`는 진한 원, `recordDays`는 연한 원, 나머지는 투명 배경으로 표시한다. 실제 앱의 시작 연월은 `YearMonth.now()`이며, Preview에서 고정 상태를 사용할 수 있다.
