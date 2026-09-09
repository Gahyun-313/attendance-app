# 내 출석 기록

## 책임

`AttendanceHistoryScreen`은 학기 통계, 월간 달력, 날짜별 출석 기록을 표시한다. `HistoryViewModel`은 Repository가 제공하는 학기 통계·기록 Flow와 사용자가 조작하는 현재 연월·선택 날짜를 하나의 `HistoryUiState`로 결합한다.

## 데이터 흐름

```text
AttendanceRepository
  ├─ getSemesterStats() ─┐
  └─ getHistoryRecords() ─┼─ combine ─→ HistoryUiState
                         │                 ├─ 통계
달력 이전/다음 달 ────────┤                 ├─ 현재 연월
날짜 선택/재선택 ─────────┘                 ├─ 기록이 있는 날짜
                                           └─ 화면에 표시할 기록
```

Repository 데이터는 서버가 아니라 현재 `FakeAttendanceRepository`의 샘플 Flow다. 따라서 화면 구현은 실제 API 연동이 아니라, 향후 Repository 구현을 교체해도 UI 상태 흐름을 유지할 수 있는 구조를 연습하는 단계다.

## 상태 규칙

- 실제 화면은 현재 연월에서 시작한다. 테스트는 고정 연월을 주입한다.
- 선택 날짜가 없으면 전체 기록을 표시한다.
- 날짜를 선택하면 `dayOfMonth`가 같은 기록만 표시한다.
- 선택한 날짜를 다시 누르면 선택을 해제하고 전체 기록으로 돌아간다.
- 이전 달·다음 달로 이동하면 선택 날짜를 초기화한다.
- `recordDays`는 기록이 있는 날짜를 달력에 표시하기 위한 집합이다.

## 테스트 범위

- `HistoryViewModelTest`: 초기 결합 상태, 날짜 필터링·재선택 해제, 달 이동 시 선택 초기화
- `AttendanceHistoryScreenTest`: 통계·달력·기록 표시, 다음 달 콜백

## 실행

```text
./gradlew :app:testDebugUnitTest --tests "com.example.attendance.feature.history.presentation.HistoryViewModelTest"
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.attendance.feature.history.presentation.AttendanceHistoryScreenTest
```

이번 작업에서는 AGENTS.md 지침에 따라 위 명령을 자동 실행하지 않았다.

## 출석 정정 요청 (3-7)

기록 조회·필수 사유 검증·접수 상태는 [정정 요청 개요](CORRECTION_README.md)를 참고한다. [상태 해설](CORRECTION_STATE_NOTES.md), [UI 해설](CORRECTION_UI_NOTES.md), [테스트 해설](CORRECTION_TEST_NOTES.md), [실행 안내](CORRECTION_VALIDATION.md)를 함께 제공한다. Navigation 연결은 3-9에서 추가했다. 실제 서버·첨부 업로드는 후속 범위다. [기록 이동 해설](HISTORY_NAVIGATION_NOTES.md)을 함께 읽는다.
