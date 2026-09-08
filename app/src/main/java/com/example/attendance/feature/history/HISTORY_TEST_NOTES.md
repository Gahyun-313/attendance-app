# History 테스트 줄별 해설

## HistoryViewModelTest

```kotlin
// Turbine으로 StateFlow의 방출을 순서대로 관찰한다.
viewModel.uiState.test {
    // 초기 결합 상태를 받는다.
    val state = awaitItem()
    // Repository 통계가 화면 상태에 들어왔는지 확인한다.
    assertThat(state.stats.presentCount).isEqualTo(22)
    // 선택 전에는 전체 기록이 표시되는지 확인한다.
    assertThat(state.records).hasSize(2)
    // 초기 선택일은 없어야 한다.
    assertThat(state.selectedDay).isNull()
}
```

테스트 Repository는 `AttendanceRepository`를 구현한 작은 Fake다. 기록과 통계만 테스트에 필요한 값으로 제공하고, 사용하지 않는 세션·출석 체크·정정 요청은 빈 목록 또는 성공값을 반환한다.

검증 항목은 다음과 같다.

- 초기 통계와 전체 기록 결합
- 날짜 선택 시 같은 `dayOfMonth` 기록만 필터링
- 같은 날짜 재선택 시 전체 기록 복원
- 달 이동 시 선택일 초기화

## AttendanceHistoryScreenTest

`HistoryContent`에 상태를 직접 전달해 Repository·ViewModel·Activity 없이 UI를 검증한다. 제목·달력 연월·기록 텍스트가 표시되는지 확인하고, 달력의 콘텐츠 설명을 기준으로 다음 달 콜백이 호출되는지 확인한다. 이 테스트는 실제 Navigation 연결이나 기기 달력 동작 전체를 검증하지 않는다.
