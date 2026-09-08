# 알림 화면 이해 노트 — STEP 3-5

이번 단계는 Repository가 제공하는 알림 목록 하나를 읽음/안읽음 두 그룹으로 분류해 보여주고, '모두 읽음 처리'를 Repository에 위임하는 화면이다. 핵심은 이 화면과 홈 화면이 **같은 `NotificationRepository` 인스턴스**를 구독한다는 점이다. 여기서 모두 읽음 처리를 누르면 홈 화면의 "읽지 않은 알림" 섹션도 같이 사라진다.

## 읽는 순서

1. 이 문서의 책임·데이터 출처·실행 흐름을 읽는다.
2. [NotificationViewModel 줄별 해설](NOTIFICATION_STATE_NOTES.md)에서 map과 stateIn으로 분류하는 과정을 따라간다.
3. [NotificationScreen·NotificationContent 줄별 해설](NOTIFICATION_UI_NOTES.md)에서 섹션·빈 상태 분기를 확인한다.
4. [단위·UI 테스트 줄별 해설](NOTIFICATION_TEST_NOTES.md)에서 Turbine으로 Repository 변경 전파를 검증하는 방식을 확인한다.
5. [직접 검증한 결과](VALIDATION.md)에서 실행한 명령과 통과 결과를 확인한다.

실제 소스의 주석은 유지보수에 필요한 정도로 작성했다. 줄별 해설에는 package/import를 제외한 실행 코드를 모두 담고, 메서드 위에는 책임과 흐름을 묶어서 설명했다. 설명 코드에서 주석을 제거하면 해당 실제 코드와 같아야 한다.

## 현재 구현과 가이드의 차이

- 가이드는 `NotificationScreen`의 `Scaffold`에 `bottomBar = { BottomNavigationBar(...) }`를 쓰지만, `BottomNavigationBar`와 Navigation 그래프 자체가 이 저장소에 아직 없다(3-3 홈 때 확인한 것과 같은 제약). 3-4 출석 체크와 동일하게 `topBar = { BackTopBar(title = "알림", onBack = { navController.popBackStack() }) }`로 대체했다.
- 이 화면을 네비게이션 그래프에 등록하는 작업은 하지 않았다(STEP 3-9에서 처리). 지금은 `NotificationContentPreview`로만 배치를 확인한다.
- 단위 테스트 2건과 Compose UI 테스트 2건을 작성했고, 사용자가 직접 실행해 4건 모두 통과를 확인했다(자세한 내용은 VALIDATION.md).

## 화면 데이터의 출처

| UiState 필드 | Repository 호출 | 현재 앱 Fake의 방식 | UI에서의 사용 |
| --- | --- | --- | --- |
| `unread` | `NotificationRepository.getNotifications()` | `MutableStateFlow`로 목록 보관, `isRead=false`인 것만 필터 | "읽지 않은 알림" 섹션 |
| `read` | `NotificationRepository.getNotifications()` | 같은 Flow에서 `isRead=true`인 것만 필터 | "읽은 알림" 섹션 |
| `isEmpty` | 없음 (계산 프로퍼티) | `unread`와 `read`가 모두 빈 목록일 때 true | `EmptyState` 표시 여부 |

`unread`와 `read`는 서로 다른 두 번의 Repository 호출이 아니라, **같은 Flow 하나**를 `map`으로 두 목록에 나눠 담은 결과다. Repository 인터페이스에는 분류된 두 메서드가 없다 — 분류는 ViewModel의 책임이다.

## 파일별 책임과 의존 관계

```text
AttendanceApp.kt
  └─ DefaultAppContainer (core/di/AppContainer.kt)
       └─ notificationRepository → FakeNotificationRepository (by lazy, 앱 전역에서 하나만 생성)
                  ↓ 같은 인스턴스를 구독
       ┌──────────────────────────────┬──────────────────────────────┐
       ↓                              ↓
NotificationViewModel.kt         HomeViewModel.kt (3-3에서 이미 같은 Repository 사용)
  ├─ getNotifications()           └─ getNotifications()를 combine의 한 입력으로 사용
  │    .map { 읽음/안읽음 분류 }
  │    .stateIn(...)
  └─ markAllAsRead() → notificationRepository.markAllAsRead()
                  ↓ 구독
NotificationScreen.kt / NotificationScreen
  ├─ Factory로 현재 소유자의 ViewModel 확보
  ├─ collectAsStateWithLifecycle로 상태 수집
  ├─ BackTopBar(뒤로가기) — 가이드의 BottomNavigationBar 대체
  └─ NotificationContent에 상태·콜백·여백 전달
                  ↓ 표시
NotificationScreen.kt / NotificationContent
  ├─ 타이틀 + OutlinedButton("모두 읽음 처리")
  ├─ isEmpty → EmptyState
  └─ SectionHeader + NotificationRow 반복 (읽지 않은 알림 / 읽은 알림)
```

`FakeNotificationRepository`가 `AppContainer`의 `by lazy`로 앱 전역에 하나만 생성되므로, 알림 화면에서 `markAllAsRead()`를 호출하면 그 갱신이 같은 `MutableStateFlow`를 구독 중인 홈 화면의 `HomeViewModel`에도 그대로 전파된다. Repository를 여러 ViewModel이 공유할 때 얻는 이점이다.

## 실제 동작 순서

```text
NotificationScreen 진입
  → viewModel(factory = NotificationViewModel.Factory)
  → getNotifications() 구독 시작 (WhileSubscribed(5_000))
  → 최신 목록을 map으로 unread/read 두 그룹으로 분류
  → stateIn의 initialValue(빈 두 목록)를 지나 분류된 첫 상태 방출
  → uiState.isEmpty가 false면 섹션별로 NotificationRow 나열

'모두 읽음 처리' 클릭
  → onMarkAllAsRead → viewModel.markAllAsRead()
  → viewModelScope.launch에서 notificationRepository.markAllAsRead() 호출
  → FakeNotificationRepository가 목록의 모든 항목을 isRead=true로 copy
  → 같은 MutableStateFlow가 새 목록을 방출
  → NotificationViewModel의 map이 재계산 → unread=[], read=전체
  → 화면의 "읽지 않은 알림" 섹션이 사라지고 "읽은 알림" 섹션만 남음
  → 같은 Repository를 구독 중인 홈 화면의 unreadNotifications도 함께 빈 목록으로 갱신
```

## map과 stateIn, 그리고 combine과의 차이

3-3 홈의 `combine`은 **여러 Flow**의 최신 값을 하나로 합쳤다. 이 화면의 `map`은 **하나의 Flow**가 방출한 값의 모양만 바꾼다(원본 알림 리스트 → 분류된 UiState). 입력 Flow가 하나뿐이라 `combine`이 필요 없다.

`stateIn(scope, started, initialValue)`은 홈과 똑같은 역할이다. `map`이 계산한 결과를 ViewModel 범위에서 공유되는 StateFlow로 만들고, 첫 방출 전에도 `initialValue`(빈 두 목록)를 즉시 읽을 수 있게 한다.

`markAllAsRead()`는 Flow 연산자 체인 밖에 있는 **별도의 suspend 호출**이다. UI 이벤트가 상태를 직접 바꾸는 것이 아니라 Repository에 위임하고, Repository의 Flow가 새 값을 방출하면 그 결과가 `map` → `stateIn`을 다시 타고 내려와 화면에 반영된다.

## Kotlin·Compose 문법과 설계

| 문법·개념 | 이 코드에서 이해할 점 |
| --- | --- |
| `val isEmpty: Boolean get() = ...` | 데이터 클래스에 저장 프로퍼티가 아닌 계산 프로퍼티를 추가하는 문법이다. `unread`/`read`가 바뀔 때마다 매번 다시 계산되며 `copy()` 대상에는 포함되지 않는다. |
| `getNotifications().map { list -> ... }` | Flow의 각 방출값을 다른 모양으로 변환하는 중간 연산자다. upstream이 새 목록을 낼 때마다 `map` 람다가 다시 실행된다. |
| `list.filter { !it.isRead }` / `list.filter { it.isRead }` | 같은 원본 리스트를 조건만 바꿔 두 번 필터링해서 서로 겹치지 않는 두 목록을 만든다. |
| `viewModelScope.launch { notificationRepository.markAllAsRead() }` | UI 콜백(`markAllAsRead()`)은 동기 함수지만, 내부에서 suspend 함수를 코루틴으로 감싸 호출한다. |
| `EmptyState(message = "...")` | 조건(`uiState.isEmpty`)에 따라 목록 UI 대신 보여줄 안내 컴포넌트다. 목록이 비어 있을 때와 아직 로딩 중일 때를 구분하지 않는다(초기 `stateIn` 값도 빈 목록이라 로딩 중에도 잠깐 이 화면이 보일 수 있다). |
| `SectionHeader(title = "...")` (onMore 생략) | 홈 화면에서는 `onMore` 콜백을 넘겼지만, 이 화면 자체가 알림의 "더보기" 대상이라 여기서는 제목만 쓰는 기본값을 그대로 쓴다. |
| `NotificationRow(notification = it)` (showContent 생략) | 기본값 `showContent = true`를 그대로 써서 본문까지 보여준다. 홈에서는 `showContent = false`로 요약만 보여줬던 것과 같은 컴포넌트의 다른 사용법이다. |
| `BackTopBar(title = "알림", onBack = { navController.popBackStack() })` | 3-4와 같은 패턴으로, 아직 없는 하단 탭 대신 상단 뒤로가기로 화면을 나간다. |

## 현재 한계

- 이 화면을 네비게이션 그래프에 연결하는 작업은 하지 않았다(STEP 3-9에서 처리).
- 하단 탭(BottomNavigationBar)이 없어 다른 주요 화면으로의 직접 이동은 지원하지 않는다. 뒤로가기만 가능하다.
- 서버 연동, 알림 개별 읽음 처리(항목 단위), 알림 삭제는 다루지 않는다. `be-dependency-blockers`에 기록된 대로 "모두 읽음 처리" API는 BE에 아직 없어 Fake 단계에 머물러 있다.
- 초기 `stateIn` 빈 상태와 실제 빈 데이터 상태를 구분하는 로딩 플래그가 없다.

## 이해를 위해 스스로 설명해 볼 질문

1. `map`과 3-3의 `combine`은 어떤 상황에서 각각 선택하는가?
2. `markAllAsRead()`를 호출한 뒤 화면이 갱신되는 경로를 Repository부터 UI까지 순서대로 설명할 수 있는가?
3. 알림 화면과 홈 화면이 서로의 존재를 모르고도 같은 데이터를 동기화할 수 있는 이유는 무엇인가?
4. `isEmpty`를 저장 프로퍼티가 아니라 계산 프로퍼티로 둔 이유는 무엇인가?
5. `NotificationRow`의 `showContent` 기본값이 화면마다 다르게 쓰이는 것이 왜 문제가 되지 않는가?
