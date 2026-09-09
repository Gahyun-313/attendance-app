# 🔄 State Flow

[README로 돌아가기](../README.md)

## 상태 전달 방식

ViewModel은 화면에 필요한 값을 UiState로 묶어 StateFlow로 노출합니다. 화면에서는 생명주기를 고려해 상태를 수집하고, 변경된 상태를 Compose에 전달합니다.

`MutableStateFlow`는 갱신용으로 내부에서 사용하고, 외부에는 읽기 전용 상태를 노출합니다. 데이터 클래스의 `copy`는 변경할 필드만 지정하면서 나머지 값을 유지합니다.

## 기능별 상태

| 기능 | 주요 상태와 처리 |
| --- | --- |
| 로그인 | 학번·비밀번호, 로딩, 성공 여부, 오류 메시지 |
| 홈 | 세션, 통계, 최근 기록·알림의 Flow 조합 |
| 출석 | 스캔 진행, 처리 결과와 다이얼로그 표시 |
| 알림 | 목록 관찰과 읽음 이벤트 전달 |
| 출석 기록 | 학기 통계, 달력 월·선택 날짜, 필터링된 기록 |
| 정정 요청 | 대상 기록, 입력값, 조회·제출 상태, 입력 오류 |
| 마이페이지 | 정적 화면과 UI 이벤트; 전용 ViewModel 미구현 |

## 출석 기록의 Flow 조합

```text
getSemesterStats() ----+
getHistoryRecords() --+-> combine -> HistoryUiState -> 달력·기록 UI
CalendarSelection ----+
```

`combine`은 각 입력 Flow의 최신 값을 조합합니다. `stateIn`은 결과를 StateFlow로 만들며, `SharingStarted.WhileSubscribed(5_000)`은 마지막 구독자가 사라진 뒤 5초 동안 구독을 유지하도록 설정합니다.

달력 월과 선택 날짜는 `CalendarSelection` 하나에 묶었습니다. 이전·다음 달 이동 시 새 월과 날짜 선택 해제를 한 번에 반영합니다. 같은 날짜를 다시 선택하면 필터를 해제합니다.

실제 ViewModel의 기본 연월은 `YearMonth.now()`이고 테스트에서는 고정 연월을 전달할 수 있습니다. 다만 초기 UiState의 기본 월은 2025년 6월이며, Flow 수집 후 선택 상태의 월로 바뀝니다.

현재 기록 필터는 `dayOfMonth`를 기준으로 동작합니다. 선택 월을 Repository에 전달하거나 연월 기준으로 전체 기록을 필터링하지 않으므로 실제 월별 조회 기능은 후속 구현이 필요합니다.

## 정정 요청의 상태 전환

```text
SavedStateHandle의 recordId
  -> ID 검증
  -> Repository.getRecord()
  -> 대상 표시 또는 조회 오류

사유 입력 -> submit()
  -> 제출 가능 여부 확인
  -> 공백 사유 검사
  -> isSubmitting = true
  -> Repository.requestCorrection()
      -> 성공: isSubmitted = true
      -> 실패: 오류 표시, 재시도 허용
```

상세 내용은 선택 입력이며 필수 사유는 `isBlank()`로 검사합니다. 코루틴 실행 전에 제출 상태를 변경해 연속 클릭에 의한 중복 호출을 막습니다. 요청에는 제출 버튼을 누른 시점의 입력값을 사용합니다.

## 알림 공유

알림 화면에서 읽음 처리를 요청하면 공유 Repository의 상태가 바뀌고, 이를 관찰하는 화면이 새 값을 받습니다. 이는 메모리 내 데이터 공유이며 서버 알림이나 푸시 수신이 아닙니다.

## 코드 근거

- [HistoryViewModel](../app/src/main/java/com/example/attendance/feature/history/presentation/HistoryViewModel.kt)
- [CorrectionViewModel](../app/src/main/java/com/example/attendance/feature/history/presentation/CorrectionViewModel.kt)
- [LoginViewModel](../app/src/main/java/com/example/attendance/feature/auth/presentation/LoginViewModel.kt)
