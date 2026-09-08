# 내 출석 기록 검증 기록

## 작성한 검증

- ViewModel 단위 테스트 3개 작성
- Compose UI 테스트 2개 작성
- 테스트 코드는 아직 실행하지 않음

## 사용자가 실행할 명령

실행 위치는 저장소 루트 `D:\Dev\attendance-app`다.

```text
./gradlew :app:testDebugUnitTest --tests "com.example.attendance.feature.history.presentation.HistoryViewModelTest"
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.attendance.feature.history.presentation.AttendanceHistoryScreenTest
```

## 기대 결과

- JVM 테스트 3개가 통과한다.
- 연결된 Android 기기 또는 에뮬레이터에서 Compose UI 테스트 2개가 통과한다.
- 실행 결과는 Gradle 기본 리포트인 `app/build/reports/tests/`와 `app/build/reports/androidTests/` 아래에서 확인한다.

## 수동 확인 항목

- 이전 달·다음 달 버튼이 콜백을 호출하는지 확인한다.
- 기록이 있는 날짜를 선택하면 해당 날짜 기록만 남는지 확인한다.
- 같은 날짜를 다시 누르면 전체 기록으로 돌아오는지 확인한다.
- 달을 이동하면 선택 날짜가 해제되는지 확인한다.
- 기록이 없는 날짜를 선택했을 때 목록이 비어 있는지 확인한다.
