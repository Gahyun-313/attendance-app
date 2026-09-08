# 출석 체크 화면 이해 노트 — STEP 3-4

이번 단계는 '출석 시작 → NFC 스캔 → 결과' 플로우를 관리하는 화면이다. 실제 NFC 태깅은 아직 없고 Repository가 지연 후 결과를 돌려주는 방식으로 시뮬레이션한다. 핵심은 진행 중인 스캔 `Job`을 사용자가 모달을 닫는 시점에 취소하는 처리이며, 테스트에서는 `kotlinx-coroutines-test`의 가상 시간으로 취소·완료 두 경로를 구분해 검증했다.

## 읽는 순서

1. 이 문서의 책임·데이터 출처·실행 흐름을 읽는다.
2. [AttendanceViewModel 줄별 해설](ATTENDANCE_STATE_NOTES.md)에서 스캔 Job 보관과 취소를 따라간다.
3. [AttendanceScreen·AttendanceContent 줄별 해설](ATTENDANCE_UI_NOTES.md)에서 상태에 따라 모달이 뜨고 닫히는 과정을 확인한다.
4. [단위·UI 테스트 줄별 해설](ATTENDANCE_TEST_NOTES.md)에서 `advanceUntilIdle()`로 가상 시간을 다루는 방식을 확인한다.
5. [직접 검증한 결과](VALIDATION.md)에서 실행한 명령과 통과 결과를 확인한다.

실제 소스의 주석은 유지보수에 필요한 정도로 작성했다. 줄별 해설에는 package/import를 제외한 실행 코드를 모두 담고, 메서드 위에는 책임과 흐름을 묶어서 설명했다. 설명 코드에서 주석을 제거하면 해당 실제 코드와 같아야 한다.

## 현재 구현과 가이드의 차이

- 실제 `NfcAdapter` 콜백은 없다. `startAttendance()`는 고정 문자열 `"FAKE-NFC-TAG-UID"`를 항상 넘긴다. 실 NFC 연동 시 이 값을 콜백에서 읽은 태그 UID로 교체해야 한다(코드의 TODO 주석 참고).
- `session`은 `getTodaySessions()` 결과에서 `isActive`가 true인 첫 세션 하나만 사용한다. 오늘 세션이 여러 개 활성 상태일 수 있는 경우나 활성 세션이 없는 경우의 UI 분기는 이번 범위에 없다.
- `AttendanceScreen`은 `NavHostController`를 인자로 받아 `onBack`에서 `popBackStack()`을 호출하지만, 이 화면 자체를 네비게이션 그래프에 등록하는 작업은 아직 하지 않았다(STEP 3-9에서 처리). 지금은 `AttendanceContentPreview`로만 배치를 확인한다.
- `FakeAttendanceRepository.checkAttendance()`는 지연 후 항상 `AttendanceStatus.PRESENT`를 반환한다. 지각·결석 결과 분기는 UI(`toResultType()`)에는 있지만 지금의 Fake로는 실제로 도달하지 않는다.
- 단위 테스트 2건과 Compose UI 테스트 2건을 작성했고, 사용자가 직접 실행해 4건 모두 통과를 확인했다(자세한 내용은 VALIDATION.md).

## 화면 데이터의 출처

| UiState 필드 | Repository 호출 | 현재 앱 Fake의 방식 | UI에서의 사용 |
| --- | --- | --- | --- |
| `session` | `AttendanceRepository.getTodaySessions()` | SampleData를 flowOf로 한 번 방출 → `first()`로 한 번만 수집 | 결과 모달·NFC 모달의 대상 세션 |
| `status` / `result` | `AttendanceRepository.checkAttendance(nfcTagUid)` | `delay(2_000)` 후 고정 `AttendanceStatus.PRESENT` 반환 | 상태 카드 라벨, 결과 모달 유형 |
| `isNfcScanning` | 없음 (ViewModel 자체 상태) | `startAttendance()`~`checkAttendance()` 완료 사이만 true | NFC 스캔 모달 표시 여부 |
| `checkedAt` | 없음 (로컬 시각) | `LocalDateTime.now()`를 포맷 | 결과 모달의 처리 시각 |

`getTodaySessions()`는 `first()`로 단 한 번만 수집하므로, 홈 화면처럼 upstream이 바뀌면 세션이 다시 갱신되는 구조가 아니다. 화면에 진입한 시점의 세션 목록을 그대로 쓴다.

## 파일별 책임과 의존 관계

```text
AttendanceApp.kt
  └─ DefaultAppContainer (core/di/AppContainer.kt)
       └─ attendanceRepository → FakeAttendanceRepository
                  ↓ 생성자 주입
AttendanceViewModel.kt (Factory → AttendanceViewModel)
  ├─ init { }: getTodaySessions().first()로 활성 세션 로드
  ├─ startAttendance(): scanJob 시작, checkAttendance 호출
  ├─ cancelScan(): scanJob.cancel()
  └─ dismissResult(): result = null
                ↓ 구독
AttendanceScreen.kt / AttendanceScreen
  ├─ Factory로 현재 소유자의 ViewModel 확보
  ├─ collectAsStateWithLifecycle로 상태 수집
  ├─ isNfcScanning → NfcScanDialog 표시
  ├─ result → AttendanceResultDialog 표시 (toResultType()으로 매핑)
  └─ AttendanceContent에 상태·콜백 전달
                ↓ 표시
AttendanceScreen.kt / AttendanceContent
  ├─ BackTopBar
  ├─ 출석 상태 카드 (Card + Text)
  └─ AccentButton("출석 시작") — status가 NOT_YET일 때만 활성화
```

`AttendanceViewModel`이 다이얼로그를 직접 그리지 않는다. `AttendanceScreen`이 `uiState.isNfcScanning`, `uiState.result` 값을 보고 어떤 다이얼로그를 띄울지 결정한다. ViewModel은 상태만 갖고 있고 Composable을 참조하지 않는다.

## 실제 동작 순서

```text
AttendanceScreen 진입
  → viewModel(factory = AttendanceViewModel.Factory)
  → init 블록에서 viewModelScope.launch로 오늘 세션 조회
  → getTodaySessions().first()로 한 번만 수집
  → sessions.find { it.isActive }를 session에 저장

'출석 시작' 클릭
  → onStartAttendance → viewModel.startAttendance()
  → session이 없거나 이미 스캔 중이면 즉시 반환
  → isNfcScanning = true → NfcScanDialog 표시
  → scanJob에 새 코루틴 대입, checkAttendance(nfcTagUid) 호출 (2초 지연)

케이스 A: 사용자가 NfcScanDialog를 닫음 (onDismiss = viewModel::cancelScan)
  → scanJob?.cancel() → checkAttendance 대기 중이던 코루틴이 취소됨
  → isNfcScanning = false, result는 그대로 null → 결과 모달 안 뜸

케이스 B: 2초 지연이 끝까지 진행됨
  → checkAttendance가 AttendanceStatus.PRESENT 반환
  → isNfcScanning = false, status/result = PRESENT, checkedAt = 현재 시각
  → result != null → AttendanceResultDialog 표시
  → 결과 모달의 onDismiss = viewModel::dismissResult → result = null
```

케이스 A와 B는 같은 `scanJob` 하나를 두고 경쟁한다. `cancel()`이 먼저 호출되면 `checkAttendance`의 `delay(2_000)` 지점에서 `CancellationException`이 발생해 그 아래 `_uiState.update { ... }`가 실행되지 않는다. 취소 후에도 결과가 반영되면 이 취소 처리가 깨진 것이다.

## Job 취소를 이해하기

**`scanJob: Job?`**은 `startAttendance()`가 실행할 때마다 새로 생성해 프로퍼티에 대입하는 코루틴 핸들이다. ViewModel의 다른 메서드(`cancelScan()`)가 이 프로퍼티를 통해 나중에 그 코루틴을 취소할 수 있다.

**`scanJob?.cancel()`**은 안전 호출 연산자로 `scanJob`이 아직 null이 아닐 때만 취소를 요청한다. `cancel()`은 코루틴에 취소 신호를 보낼 뿐이며, `delay()`처럼 취소에 협조적인 지점에서 `CancellationException`을 던져 실행을 멈춘다. 이미 끝난 코루틴에 호출해도 예외가 나지 않는다.

**가상 시간과 `advanceUntilIdle()`**: `runTest`는 기본적으로 `delay()`를 실제로 기다리지 않고 가상 시계를 사용하는 `TestDispatcher` 위에서 실행된다. `advanceUntilIdle()`을 호출하면 예약된 모든 지연 작업의 가상 시간을 한 번에 끝까지 흘려보낸다. 취소 테스트는 이 호출 **전에** `cancelScan()`을 실행해야 지연이 끝나기 전에 취소가 걸린다. 완료 테스트는 반대로 취소 없이 `advanceUntilIdle()`을 호출해 지연이 끝난 뒤의 결과를 확인한다.

**중복 실행 방지**: `startAttendance()` 시작부의 `if (_uiState.value.isNfcScanning) return`은 스캔 도중 버튼을 다시 눌러 두 번째 `scanJob`이 첫 번째를 덮어쓰는 상황을 막는다. 이 가드가 없으면 첫 스캔의 `scanJob` 참조를 잃어 `cancelScan()`으로 취소할 수 없게 된다.

## Kotlin·Compose 문법과 설계

| 문법·개념 | 이 코드에서 이해할 점 |
| --- | --- |
| `data class AttendanceUiState` | 화면에 필요한 다섯 값을 불변 프로퍼티로 묶는다. `result: AttendanceStatus?`처럼 nullable 기본값 null은 "아직 결과 없음"을 표현한다. |
| `var scanJob: Job?` | ViewModel의 가변 프로퍼티다. UiState와 달리 사용자에게 노출되는 화면 상태가 아니라 내부 제어용 핸들이라 별도로 둔다. |
| `_uiState.update { it.copy(...) }` | 현재 값을 읽어 일부 필드만 바꾼 새 값으로 원자적으로 교체한다. 동시에 두 곳에서 `update`를 호출해도 갱신이 덮어써지지 않는다. |
| `viewModelScope.launch { }` | ViewModel의 생명주기에 묶인 코루틴을 시작한다. ViewModel이 제거되면 `scanJob`을 포함해 시작된 코루틴이 모두 취소된다. |
| `sessions.find { it.isActive }` | 목록에서 조건을 만족하는 첫 원소 하나를 찾거나, 없으면 null을 반환한다. 여러 개가 조건을 만족해도 첫 번째만 쓴다. |
| `checkAttendance(nfcTagUid = "...")` | 이름 붙인 인자로 어떤 값이 무엇을 의미하는지 호출부에서 드러낸다. |
| `when (this) { ... -> ... }` (`toResultType()`) | `AttendanceStatus`의 네 값을 모두 분기해 `AttendanceResultType`으로 매핑하는 확장 함수다. 분기를 빠뜨리면 컴파일 에러가 난다(enum은 `when`이 스마트 캐스트로 완전성을 검사). |
| `if (uiState.isNfcScanning && session != null) { Dialog(...) }` | Compose에서 조건에 따라 컴포저블을 그리거나 생략하는 방식이다. 별도의 `visible` 플래그를 다이얼로그 내부에 두지 않는다. |
| `viewModel::cancelScan` | 메서드 참조. `{ viewModel.cancelScan() }`과 같은 동작을 하는 함수 값을 만든다. |
| `enabled = canStart` | `AccentButton`에 활성/비활성 상태를 전달해 이미 출석했다면 클릭을 막는다. 색상(`Blue600`/`Blue200`)도 같은 조건으로 바꿔 시각적으로도 구분한다. |
| Screen/Content 분리 | Screen이 ViewModel·NavController·다이얼로그 표시를 담당하고 Content는 상태만 받아 카드와 버튼을 그린다. Preview와 UI 테스트는 Content만 호출한다. |

## 현재 한계

- 실제 NFC 하드웨어 연동은 없다. 태그 UID는 항상 고정 문자열이다.
- 활성 세션이 둘 이상이거나 없는 경우의 UI 분기는 없다.
- `checkAttendance`가 항상 `PRESENT`만 반환하므로 지각·결석 결과 모달은 코드는 있지만 지금 Fake로는 실행 경로에 도달하지 않는다.
- 이 화면을 네비게이션 그래프에 연결하는 작업은 하지 않았다(STEP 3-9에서 처리).
- 서버 연동, 오류 처리(스캔 실패, 통신 오류)는 다루지 않는다.

## 이해를 위해 스스로 설명해 볼 질문

1. `scanJob`을 ViewModel의 프로퍼티로 보관하는 이유는 무엇이고, 이걸 지역 변수로 바꾸면 어떤 문제가 생기는가?
2. `cancelScan()`이 `_uiState.value.result`를 바꾸지 않는데도 결과 모달이 뜨지 않는 이유는 무엇인가?
3. 가상 시간 테스트에서 `advanceUntilIdle()`을 호출하는 시점이 취소 테스트와 완료 테스트에서 왜 다른가?
4. `startAttendance()` 시작부의 중복 실행 방지 가드를 지우면 어떤 시나리오에서 문제가 생기는가?
5. `AttendanceContent`가 `AttendanceViewModel`을 직접 참조하지 않는 것이 왜 테스트를 쉽게 만드는가?
6. 실제 NFC 콜백으로 교체할 때 `startAttendance()`의 어느 부분을 바꿔야 하는가?
