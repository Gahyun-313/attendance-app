# 홈 화면 이해 노트 — STEP 3-3

이번 단계는 정적인 홈 화면 초안에서 한 단계 나아가, Repository가 제공하는 네 Flow를 하나의 화면 상태로 연결한다. 구현은 `attendance-app`의 수동 DI 구조에 맞췄다. 서버 통신이나 Hilt 전환을 완료한 단계는 아니다.

## 읽는 순서

1. 이 문서의 책임·데이터 출처·실행 흐름을 읽는다.
2. [HomeViewModel 줄별 해설](HOME_STATE_NOTES.md)에서 combine과 stateIn을 따라간다.
3. [HomeScreen·HomeContent 줄별 해설](HOME_UI_NOTES.md)에서 상태가 어떻게 UI로 바뀌는지 확인한다.
4. [단위·UI 테스트 줄별 해설](HOME_TEST_NOTES.md)에서 구독과 가상 시간, 표시·콜백 검증을 확인한다.
5. [직접 검증하는 방법](VALIDATION.md)으로 테스트와 빌드를 실행한다.

실제 소스의 주석은 유지보수에 필요한 정도로 작성했다. 줄별 해설에는 package/import를 제외한 실행 코드를 모두 담고, 메서드 위에는 책임과 흐름을 묶어서 설명했다. 설명 코드에서 주석을 제거하면 해당 실제 코드와 같아야 한다.

## 현재 구현과 가이드의 차이

- `HomePrototypeScreen`은 STEP 2-1의 정적 UI로 보존했다. 새 `HomeScreen`은 ViewModel을 수집하는 화면이다. `MainActivity`는 이제 AppNavGraph를 호스팅하며 로그인 성공 후 새 HomeScreen을 표시한다. 이번 연결의 실행 검증은 별도로 필요하다.
- 3-9에서 `BottomNavigationBar`, `ScreenRoute`, AppNavGraph를 추가했다. 새 HomeScreen은 `onCheckAttendance`, `onMoreNotifications`, `onMoreRecords`를 인자로 받는다. 실제 이동과 하단 탭 연결은 AppNavGraph가 담당한다.
- 테스트는 가이드의 첫 `awaitItem()`을 곧바로 Repository 결과라고 가정하지 않는다. `stateIn`이 가진 빈 초기 상태를 먼저 확인하고, `runCurrent()`로 수집을 진행한 뒤 결합 결과를 검사한다.
- 초기 데이터 검증뿐 아니라 알림 읽음 처리, 통계·세션·기록 변경, 빈 목록, UI 콜백도 확인하는 테스트 코드를 작성했다. 이번 작업에서 테스트·빌드는 실행하지 않았다.

## 화면 데이터의 출처

| UiState 필드 | Repository 호출 | 현재 앱 Fake의 방식 | UI에서의 사용 |
| --- | --- | --- | --- |
| `weeklyStats` | `AttendanceRepository.getWeeklyStats()` | 고정 WeeklyStats를 flowOf로 한 번 방출 | 출석 횟수·비율·남은 세션 |
| `todaySessions` | `AttendanceRepository.getTodaySessions()` | SampleData를 flowOf로 한 번 방출 | 세션 카드 반복 표시 |
| `unreadNotifications` | `NotificationRepository.getNotifications()` | MutableStateFlow로 알림 목록 보관 | 읽지 않은 알림 중 앞의 두 건 |
| `recentRecords` | `AttendanceRepository.getRecentRecords()` | SampleData를 flowOf로 한 번 방출 | 최근 기록 행 반복 표시 |

출석 데이터가 실제 앱에서 계속 갱신되는 구현은 아직 없다. `combine`은 upstream이 새 값을 내보내면 갱신할 수 있지만, `flowOf` 자체가 변경을 감지해 다시 방출하지는 않는다. 테스트의 `MutableAttendanceRepository`는 이 갱신 계약을 검증하기 위한 대체 객체이며 앱에 주입되지 않는다.

`getHistoryRecords()`는 이번 홈 화면의 데이터 출처가 아니다. 학기 전체 기록을 바꾸면 홈 최근 기록까지 자동으로 바뀐다고 가정하면 안 된다.

## 파일별 책임과 의존 관계

```text
AttendanceApp.kt
  └─ DefaultAppContainer (core/di/AppContainer.kt)
       ├─ attendanceRepository → FakeAttendanceRepository
       └─ notificationRepository → FakeNotificationRepository
                  ↓ 생성자 주입
HomeViewModel.kt (Factory → HomeViewModel)
  └─ 네 Flow → combine → stateIn → StateFlow<HomeUiState>
                                           ↓ 구독
HomeScreen.kt / HomeScreen
  ├─ Factory로 현재 소유자의 ViewModel 확보
  ├─ collectAsStateWithLifecycle로 상태 수집
  ├─ Scaffold와 LogoTopBar 구성
  └─ HomeContent에 상태·콜백·Scaffold 여백 전달
                ↓ 표시
HomeScreen.kt / HomeContent
  ├─ StatSummaryRow → StatItem 목록
  ├─ SessionCard → 출석 체크 콜백
  ├─ NotificationRow → 알림 표시
  └─ RecentRecordRow → 기록 표시
```

`HomeViewModel`이 `HomeScreen()`을 호출하는 것은 아니다. Screen이 상태 스트림을 구독한다. ViewModel은 NavController, Composable, Android View를 직접 참조하지 않는다.

Factory는 ViewModel에 필요한 두 Repository를 컨테이너에서 꺼내 생성자로 전달한다. AndroidX의 `viewModel()`이 ViewModelStoreOwner에 속한 인스턴스를 재사용하므로 재구성마다 새 객체가 생성되는 것은 아니다. Application은 Manifest의 `.AttendanceApp` 등록을 통해 준비되고, 컨테이너의 `by lazy`는 최초 접근 시 Fake를 만든다.

## 실제 동작 순서

```text
HomeScreen을 호출하는 상위 화면
  → viewModel(factory = HomeViewModel.Factory)
  → HomeUiState 기본값(통계 0, 목록 비어 있음) 준비
  → Screen이 StateFlow 구독
  → WhileSubscribed 정책에 따라 네 upstream Flow 수집 시작
  → 네 Flow가 모두 한 번 이상 방출
  → 각 Flow의 최신 값을 combine 람다에 전달
  → 전체 알림에서 읽음 항목 제외 → 앞의 두 건 선택
  → 새 HomeUiState 생성 → StateFlow 갱신
  → Compose State 변경 → UI 재구성

다른 기능 또는 테스트가 동일한 NotificationRepository.markAllAsRead() 호출
  → Fake의 목록을 copy(isRead = true)로 갱신
  → 알림 Flow 방출
  → combine이 최신 통계·세션·기록과 새 알림을 결합
  → unreadNotifications가 빈 목록인 새 상태
  → 홈의 알림 행 제거
```

네 Flow를 순서대로 모두 다시 조회해야 하는 방식이 아니다. 기존에 수집한 최신 값과 변경된 값을 결합한다. 빠른 연속 변경이나 느린 구독에서는 중간 상태가 합쳐지거나 건너뛰어질 수 있어 모든 변경마다 UI가 정확히 한 번씩 그려진다고 보장하지 않는다.

```text
SessionCard의 출석 체크 버튼
  → HomeContent의 onCheckAttendance
  → HomeScreen에 전달된 onCheckAttendance
  → 이후 호출자가 구현할 이동 처리

알림/기록 SectionHeader의 더보기
  → onMoreNotifications / onMoreRecords
  → 호출자가 구현할 이동 처리
```

버튼을 누르는 것만으로 NFC 인증이나 출석 처리가 수행되지는 않는다. 이번 단계는 이동 의도를 외부에 전달하는 UI 계약까지다.

## combine과 stateIn을 구분하기

**combine**은 네 데이터 스트림의 최신 값을 조합해 `HomeUiState`를 계산한다. 네 upstream이 모두 최초 값을 제공해야 결합 결과를 만들 수 있다. 한 Flow가 아직 방출하지 않았다면 `HomeUiState()`가 보이는 초기 구간이 길어질 수 있다.

**stateIn**은 계산 결과를 ViewModel 범위에서 공유되는 StateFlow로 만든다. `initialValue`가 있으므로 `.value`를 즉시 읽을 수 있다. StateFlow는 현재 값 하나를 보관하고, 같은 값은 equals 기준으로 중복 방출하지 않는다. 이 데이터 클래스의 리스트도 값 비교에 참여한다.

**WhileSubscribed(5_000)**은 구독자가 생기면 upstream 수집을 시작하고, 마지막 구독자가 사라진 후 5초를 기다려 중단한다. 5초가 상태의 유효기간이거나 데이터를 자동으로 지우는 시간은 아니다. 이 코드의 기본 replay 정책에서는 마지막 상태를 보관하며, 중단 후 새 구독이 생기면 upstream을 다시 수집한다. ViewModel이 제거되면 viewModelScope와 그 작업은 취소된다.

**collectAsStateWithLifecycle**은 Screen의 생명주기가 기본 STARTED 이상일 때 수집한다. 이 수집이 중지되더라도 다른 구독자가 남아 있으면 upstream은 계속 수집된다. 화면 회전처럼 잠깐 끊기는 경우를 완충하는 것이 5초 대기 정책의 목적이다.

## Kotlin·Compose 문법과 설계

| 문법·개념 | 이 코드에서 이해할 점 |
| --- | --- |
| `data class`, `val`, `emptyList()` | 화면에 필요한 값을 불변 프로퍼티로 묶는다. 기본 상태의 빈 목록 타입은 선언된 List 타입에서 추론한다. val 자체가 모든 객체의 깊은 불변성을 보장하는 것은 아니다. |
| `List<ClassSession>` | 세션 객체 여러 개를 읽기 전용 List 인터페이스로 받는다. UI는 목록을 소유·수정하지 않는다. |
| `combine(...) { stats, ... -> }` | 마지막 인자인 변환 람다를 괄호 밖에 둔 후행 람다 문법이다. 매개변수 순서는 앞의 네 Flow 순서와 같다. |
| `filter { !it.isRead }.take(2)` | it은 알림 한 건, !는 부정이다. 읽지 않은 항목을 먼저 고르고 그중 앞의 두 건만 선택한다. 별도의 날짜 정렬은 수행하지 않는다. |
| `stateIn(scope, started, initialValue)` | 이름 붙인 인자로 수명·시작 정책·초깃값을 구분한다. stateIn과 combine은 서로 다른 책임이다. |
| `companion object`, `initializer`, `as` | 클래스 이름으로 Factory에 접근하고, CreationExtras의 Application을 AttendanceApp으로 캐스팅하여 의존성을 전달한다. |
| `by` | Screen에서는 Compose State 값 접근 위임, 테스트의 `AttendanceRepository by FakeAttendanceRepository()`에서는 인터페이스 구현 위임이다. 같은 키워드지만 역할이 다르다. |
| `() -> Unit` | 인자와 반환 데이터가 없는 콜백이다. 함수를 값으로 전달하고 사용자가 클릭할 때 실행한다. |
| `${...}` | 숫자 프로퍼티를 문자열 안에 삽입한다. `3 / 10`, `55%`, `2회`처럼 표시용 문구를 만든다. |
| `forEach { session -> ... }` | 목록 원소마다 UI 구성 함수를 호출한다. 세션별 가변 로컬 상태나 목록 재정렬을 도입하면 안정적인 key도 함께 고려해야 한다. |
| `Scaffold`, `paddingValues` | 상단바 등 화면 틀을 구성하고 본문이 가려지지 않도록 제공된 여백을 Content에 전달한다. |
| `Modifier` 체인 | 부모 크기 채우기 → 스크롤 → 내부 여백을 적용한다. 전달받은 modifier에는 Scaffold 여백도 포함된다. |
| `rememberScrollState()` | 재구성 간 스크롤 상태를 보관한다. Column 전체를 verticalScroll로 스크롤한다. |
| Screen/Content 분리 | Screen은 환경·수집을 담당하고 Content는 받은 상태를 렌더링한다. Preview와 UI 테스트는 Repository 없이 Content만 호출한다. |

홈에 두 개의 알림만 보여 주는 것은 화면 표시 정책이므로 ViewModel에서 필터링한다. Repository는 다른 화면에서도 쓸 전체 알림을 제공한다. 모델에 정렬 가능한 시각 필드가 없으므로 “최근”은 현재 Repository의 목록 순서를 전제로 한다. 화면에서 `timeAgo` 문자열을 날짜처럼 정렬하지 않는다.

## 테스트를 읽을 때의 핵심

`StandardTestDispatcher`는 작업을 예약하므로, 구독 직후 `awaitItem()`은 stateIn의 초기 상태를 받는다. `runCurrent()`는 현재 가상 시각에 예약된 작업을 진행하고, 다음 `awaitItem()`은 결합된 상태를 검증한다. 실제 시간을 기다리는 sleep에 기대지 않는다.

`MutableAttendanceRepository`는 홈에서 사용하는 세 조회 메서드만 MutableStateFlow로 대체하고, 나머지 인터페이스 메서드는 기존 Fake에 위임한다. 운영 코드의 조회 계약을 바꾸지 않고 변화를 재현한다. 알림 변경은 실제 FakeNotificationRepository의 markAllAsRead를 사용한다.

여러 Flow를 한꺼번에 비울 때는 중간 방출 횟수를 고정하지 않고 `expectMostRecentItem()`으로 최종 상태를 검증한다. 반면 각 필드 변경 테스트에서는 한 번 변경한 뒤 수집을 진행하고 결과를 확인하여 무엇이 바뀌었는지 분리한다. `tearDown()`은 viewModelScope를 취소해 5초 대기 작업이 다른 테스트에 남지 않도록 한다.

UI 테스트는 화면 밖의 버튼·제목을 `performScrollTo()`로 보이게 만든 뒤 검사하거나 클릭한다. 두 “더보기” 링크는 현재 섹션 순서인 알림 → 기록을 기준으로 구분하고 개수도 확인한다. 화면 구조가 바뀌면 이 선택 기준도 함께 갱신해야 한다.

## 현재 한계

- API·영구 저장·자동 갱신 출석 데이터는 미구현이다. Navigation은 3-9에서 연결했으며 실행 결과는 별도로 확인한다.
- 빈 초기 상태와 실제 빈 데이터 상태를 별도 로딩 플래그로 구분하지 않는다. 가이드의 범위에 맞춘 상태다.
- upstream 예외를 사용자 오류 상태로 변환하는 로직은 없다. 실제 네트워크 연결 시 오류 모델·재시도 정책이 필요하다.
- Column은 모든 행을 구성한다. 현재 샘플 목록 규모에 맞췄으며 대량 목록에 대한 LazyColumn 최적화는 하지 않았다.
- Content UI 테스트는 Factory, 실제 Activity 진입, Navigation, 서버 통신을 검증하지 않는다.

## 이해를 위해 스스로 설명해 볼 질문

1. combine과 stateIn이 각각 어떤 문제를 해결하는가?
2. uiState.value를 읽는 것과 uiState를 구독하는 것은 왜 다른가?
3. WhileSubscribed의 5초가 지났을 때 수집과 마지막 값은 각각 어떻게 되는가?
4. 알림 두 건 제한을 Repository가 아닌 ViewModel에 둔 이유는 무엇인가?
5. 앱의 Fake 출석 데이터는 고정인데, 테스트에서 갱신을 검증할 수 있는 이유는 무엇인가?
6. HomeScreen과 HomeContent 중 어떤 쪽이 ViewModel을 알고, 왜 그렇게 분리했는가?
7. 현재 버튼 콜백과 실제 화면 이동·출석 인증 사이에 어떤 구현이 더 필요한가?
