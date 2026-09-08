# 출석 정정 요청

## 범위와 파일 역할

3-7은 정정 대상 조회, 필수 사유 검증, 선택 상세 입력, 비동기 제출과 성공·실패 표시를 구현한다. 실제 앱의 수동 DI와 기존 AttendanceRepository 계약을 사용한다.

| 파일 | 책임 |
| --- | --- |
| presentation/CorrectionViewModel.kt | CorrectionUiState, recordId 조회, 입력 갱신, 검증·중복 방지·접수 결과, 수동 Factory |
| presentation/CorrectionRequestScreen.kt | 상태 구독·완료 콜백, Content·Preview, 대상·입력·오류·버튼 표시 |
| core/data/repository/AttendanceRepository.kt | getRecord와 requestCorrection 계약 및 기존 Fake 구현 |
| core/di/AppContainer.kt | 화면들이 공유할 Repository 인스턴스 제공 |
| CorrectionViewModelTest.kt | 인자·검증·요청·실패·취소 단위 테스트 9건 |
| CorrectionRequestScreenTest.kt | 입력·버튼·오류·콜백 UI 테스트 6건 |

Repository와 AppContainer 경로는 앱 패키지 `com/example/attendance/` 기준이다. 테스트는 각각 src/test와 src/androidTest의 같은 feature/history/presentation 패키지에 있다.

## 파일 간 호출과 상태 전달

```text
[후속 3-9 연결] route recordId(Long)
                    ↓ 같은 키 "recordId"
CreationExtras → createSavedStateHandle() ─┐
테스트의 SavedStateHandle(mapOf(...)) ────┤
                                         ↓
CorrectionViewModel.Factory → CorrectionViewModel
        ↑ AppContainer            │ getRecord(recordId) [함수 호출]
        └─ AttendanceRepository ←─┘
                  │ 기록 또는 null
                  ↓
           MutableStateFlow<CorrectionUiState>
                  │ [상태 구독: 직접 함수 호출과 다름]
                  ↓
CorrectionRequestScreen.collectAsStateWithLifecycle()
                  ↓ uiState와 콜백 전달
            CorrectionContent
              ├─ 사유 입력 → onReasonChange → copy(reason, isReasonError)
              ├─ 상세 입력 → onDetailChange → copy(detail)
              ├─ 취소 → onBack [호출자가 이동 결정]
              └─ 제출 → submit()
                         ├─ 조회·제출·완료 중 / 대상 없음 → 반환
                         ├─ 빈 사유 → isReasonError = true
                         └─ isSubmitting = true [launch 전에 잠금]
                              → requestCorrection(ID, 사유, 상세)
                              → 성공: isSubmitted = true
                              → false/예외: 오류 + 재시도 가능
                                      ↓ 상태 구독
                     Screen.LaunchedEffect → onSubmitted
                     [실제 popBackStack은 후속 연결]
```

## 상태 규칙과 실패 처리

- 초기 isLoading=true이며, 유효한 Long ID로 한 건을 조회한다. 누락·0·음수는 조회하지 않는다. 없는 기록 또는 조회 예외는 오류를 표시하고 제출을 막는다. 조회 실패의 재시도는 현재 화면 재진입 방식이다.
- 사유는 isBlank로 검사해 공백만 있는 입력도 거절한다. 새 사유 입력은 필수 입력 오류를 해제한다. 상세는 선택 사항이다.
- submit은 호출 시점의 상태를 보관하고 launch 전에 isSubmitting을 true로 바꾼다. UI 비활성화와 ViewModel 가드를 함께 사용해 코루틴 실행 전의 빠른 연속 클릭도 막는다.
- 입력 필드는 제출 중에도 갱신될 수 있지만, 진행 중인 요청은 클릭 시점의 값을 사용한다. 응답 반영에는 최신 상태의 copy를 사용해 새 입력을 덮어쓰지 않는다.
- Boolean false와 일반 예외는 오류로 표시한다. isSubmitting을 해제해 재시도할 수 있으며 이전 오류는 다음 요청 시작 시 지운다. 성공 후에는 추가 제출을 막는다.
- CancellationException은 일반 실패가 아니므로 다시 던진다. 화면 수명에 따른 취소 전파를 유지한다. 취소가 실제 서버 접수 취소를 보장하는 것은 아니다.

## 문법·생명주기·아키텍처

### SavedStateHandle과 Factory

SavedStateHandle은 ViewModel 소유자의 저장 상태와 기본 인자에 접근하는 키-값 컨테이너다. 이번 단계에서는 recordId만 읽는다. 단순 remember가 Composition에 값을 기억하는 것과 달리, 저장 상태 소유자와 연결된 handle은 시스템 복원 과정에 참여한다. 다만 사유·상세는 handle에 쓰지 않으므로 프로세스 재생성 시 초안이 복원되는 구현은 아니다.

Factory의 initializer는 CreationExtras에서 Application을 얻고 createSavedStateHandle()로 handle을 만든다. AppContainer의 Repository를 생성자로 전달하므로 ViewModel 자체는 Context나 NavController를 요구하지 않는다. 테스트에서는 handle과 Repository를 직접 만들어 주입할 수 있다.

Navigation 그래프가 아직 없으므로 ARG_RECORD_ID는 CorrectionViewModel에 둔다. 3-9에서 동일 키와 NavType.LongType으로 인자를 선언하고 해당 NavBackStackEntry를 ViewModel 소유자로 사용해야 한다. 인자 없는 Activity에서 기본 Factory를 호출하는 것만으로 특정 기록 ID가 생기지는 않는다.

### StateFlow, copy, update, suspend

MutableStateFlow는 최신 상태를 보관하며 asStateFlow로 외부 쓰기를 제한한다. data class의 copy는 일부 값만 교체한 새 상태를 만든다. update는 최신 상태를 기준으로 안전하게 갱신하므로 내부 람다에는 Repository 호출 같은 부수 효과를 넣지 않는다.

suspend는 함수가 대기 중 스레드를 점유하지 않고 중단·재개할 수 있다는 뜻이며 자동으로 IO 스레드를 선택한다는 뜻은 아니다. viewModelScope.launch는 ViewModel 수명에 묶인 작업을 시작한다. 화면이 잠시 중지되어 상태 수집이 멈추는 것과 ViewModel이 제거되어 작업이 취소되는 것은 다르다.

### Screen, Content, 효과

Screen은 ViewModel의 상태를 생명주기에 맞춰 수집하고 Content에 전달한다. Content는 상태와 콜백만 받아 UI를 그리는 state hoisting 구조다. 입력 필드의 onValueChange는 값 변경 요청이며 실제 화면 값을 바꾸려면 상태 소유자가 새 값을 돌려줘야 한다.

LaunchedEffect는 키가 바뀌거나 Composition에 새로 진입할 때 실행된다. isSubmitted=true의 단순 재구성에서는 완료 콜백을 반복하지 않지만, 같은 성공 ViewModel을 유지한 채 Composition에 재진입하면 다시 호출될 수 있다. 현재는 성공 후 화면을 제거하는 후속 Navigation 연결을 전제로 하며 영구적인 일회성 이벤트 소비 보장을 구현한 것은 아니다. rememberUpdatedState는 effect를 재시작하지 않고 최신 콜백을 사용하게 한다.

### 테스트 구조

StandardTestDispatcher는 launch를 즉시 진행하지 않아 코루틴 시작 전에 제출 상태가 잠기는지 검증하기 좋다. advanceUntilIdle은 같은 테스트 스케줄러에 예약된 작업과 delay를 가상 시간으로 처리한다. 지연이 있는 Fake의 성공을 submit 직후 곧바로 단언하지 않는다.

테스트 Repository는 AttendanceRepository by FakeAttendanceRepository()로 무관한 계약을 위임하고 getRecord·requestCorrection만 재정의한다. UI 테스트는 mutableStateOf로 입력을 되돌려주며, 작은 기기에서는 performScrollTo 후 하단 버튼을 누른다. 단위 테스트는 로직을, UI 테스트는 Content의 표시·입력 연결을 검증한다. Navigation과 Factory의 실제 복원 통합 테스트는 이번 범위가 아니다.

## 가이드와의 차이 및 구현 한계

- 아직 없는 ScreenRoute와 NavController 대신 기능 내 인자 키와 이동 콜백을 사용한다. MainActivity는 HomePrototypeScreen을 유지하며 기록 화면의 정정 이동도 연결하지 않았다.
- 조회 상태·없는 ID·요청 실패 표시를 보완하고, launch 이전 잠금으로 중복 제출을 막았다.
- AttendanceRecord는 dayOfMonth만 제공하므로 가이드의 고정 연월·수업 시간을 표시하지 않는다.
- 파일 선택 버튼은 가이드의 배치용 자리이며 실제 피커·증빙 업로드는 없다. 화면에 준비 중이라고 표시한다.
- FakeAttendanceRepository는 지연 후 true를 반환할 뿐 실제 접수 저장, 출석 상태 변경, 서버 통신을 하지 않는다. 다른 화면의 기록이 자동으로 변경되었다고 설명하지 않는다.

## 줄별 해설과 검증 안내

- [상태·ViewModel](CORRECTION_STATE_NOTES.md)
- [화면·Preview](CORRECTION_UI_NOTES.md)
- [단위·UI 테스트](CORRECTION_TEST_NOTES.md)
- [검증 절차와 실행 상태](CORRECTION_VALIDATION.md)

## 이해를 위해 스스로 설명해 볼 질문

1. recordId가 빠진 경우 0으로 대체하는 대신 오류로 다루는 이유는 무엇인가?
2. 사유 입력 콜백만 호출하고 상태를 돌려주지 않으면 TextField에 어떤 문제가 생기는가?
3. 버튼을 비활성화하는 것과 launch 전에 ViewModel 상태를 잠그는 것은 각각 무엇을 막는가?
4. 입력을 보관한 state와 update 안의 it은 어느 시점의 값인가?
5. suspend, viewModelScope, collectAsStateWithLifecycle은 각각 어떤 수명을 다루는가?
6. CancellationException을 일반 실패로 바꾸지 않는 이유는 무엇인가?
7. SavedStateHandle을 사용해도 현재 사유 입력이 프로세스 재생성 뒤 복원되지 않는 이유는 무엇인가?
8. Fake 접수 성공과 실제 서버 접수 완료의 차이는 무엇인가?
