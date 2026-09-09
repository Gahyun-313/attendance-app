# 기록 화면의 정정 요청 연결

## 호출 순서

HistoryRecordRow의 버튼 → HistoryContent.onRequestCorrection(record.id) → AttendanceHistoryScreen의 외부 콜백 → AppNavGraph.navigate(Correction.createRoute(id)) 순서다. 화면이 NavController를 직접 갖지 않아 기존 Content 테스트·Preview를 유지한다.

## 변경 부분 줄별 해설

다음은 실제 코드에서 이번 연결과 관련된 부분을 발췌했다. Content의 기존 통계·달력 표시 코드는 생략했으며 [기존 UI 해설](HISTORY_UI_NOTES.md)과 함께 읽는다.

```kotlin
// Screen은 상태 구독과 외부 이동 콜백 연결을 담당한다.
@Composable
// 기존 ViewModel 매개변수 앞에 정정 이동 이벤트를 받는다.
fun AttendanceHistoryScreen(
    // 호출자는 Long ID로 실제 이동을 결정한다. Preview 등에서는 기본 빈 콜백을 사용할 수 있다.
    onRequestCorrection: (Long) -> Unit = {},
    // 기존 수동 Factory를 사용하며 NavHost 안에서는 해당 entry의 ViewModelStore에 보관된다.
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    // 화면 생명주기에 맞춰 최신 상태를 수집한다.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // 상태와 사용자 이벤트를 Content에 전달한다.
    HistoryContent(
        // 표시할 통계·달력·기록 상태다.
        uiState = uiState,
        // 이전 달 버튼의 함수 참조다.
        onPrevMonth = viewModel::moveToPreviousMonth,
        // 다음 달 버튼의 함수 참조다.
        onNextMonth = viewModel::moveToNextMonth,
        // 날짜 선택을 ViewModel에 전달한다.
        onDayClick = viewModel::selectDay,
        // 자체 Scaffold가 없는 기록 Screen에 상태바 여백을 적용한다.
        modifier = Modifier.statusBarsPadding(),
        // 실제 이동은 위에서 받은 외부 콜백에 위임한다.
        onRequestCorrection = onRequestCorrection
    )
}
```

HistoryContent에는 기존 modifier 다음에 `onRequestCorrection: (Long) -> Unit = {}`를 추가했다. 이벤트 매개변수를 기본값과 함께 뒤에 추가해 기존 테스트·Preview 호출을 유지한다.

```kotlin
// 현재 화면에 표시하는 각 기록에 대해 행을 만든다.
uiState.records.forEach { record ->
    // 기존 공통 출석 기록 행을 재사용한다.
    HistoryRecordRow(
        // 현재 행의 기록과 상태를 전달한다.
        record = record,
        // 이 행의 ID를 캡처한 람다가 클릭 시 외부 이동 요청을 보낸다.
        onRequestCorrection = { onRequestCorrection(record.id) }
    )
}
```

람다는 생성 시점의 record를 캡처한다. 버튼을 누르기 전에는 navigate를 실행하지 않으며, 정정 가능 여부 표시 정책은 기존 HistoryRecordRow가 유지한다. 실제 Long 인자 전달과 정정 완료 후 복귀는 AppNavGraphTest에서 검증하도록 작성했다. 실행 결과는 [Navigation 검증 문서](../navigation/VALIDATION.md)에 별도로 기록한다.
