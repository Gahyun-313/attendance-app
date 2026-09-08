# 알림 단위 테스트와 UI 테스트 줄별 해설

[알림 화면 구조·문법·호출 흐름으로 돌아가기](README.md)

package/import와 기존 주석은 생략했다. 아래 코드의 각 실행 줄에 상세 설명을 붙였으며, 동작은 실제 소스와 같다. 사용자가 직접 실행해 아래 4개 테스트 모두 통과를 확인했다(자세한 내용은 VALIDATION.md).

## NotificationViewModelTest.kt

원본: `app/src/test/java/com/example/attendance/feature/notification/presentation/NotificationViewModelTest.kt`

```kotlin
// class NotificationViewModelTest를 선언하고 관련 상태와 메서드를 묶는다.
class NotificationViewModelTest {

    // 프로퍼티 getter에 JUnit Rule을 붙여 테스트 시작·종료 때 Main 디스패처 교체를 수행하게 한다.
    @get:Rule
    // 인자 없이 생성하면 기본값인 UnconfinedTestDispatcher를 쓴다. launch한 코루틴을 첫 지연 지점까지 즉시 실행한다.
    val mainDispatcherRule = MainDispatcherRule()

    // 각 테스트의 setUp에서 초기화할 viewModel: NotificationViewModel 프로퍼티다. private으로 외부 접근을 막으며 초기화 전 사용하면 오류가 난다.
    private lateinit var viewModel: NotificationViewModel

    // 각 테스트 시작 전 다음 메서드를 실행한다.
    @Before
    /**
     * 책임: 테스트 간 데이터 격리. 흐름: 새 Fake Repository → 새 ViewModel에 생성자 주입. 각 테스트 전에 JUnit이 호출한다.
     */
    // setUp 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
    fun setUp() {
        // Factory나 AppContainer 없이 FakeNotificationRepository를 직접 생성자에 주입한다.
        viewModel = NotificationViewModel(FakeNotificationRepository())
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }

    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 초기 상태가 읽음/안읽음으로 올바르게 분류돼 있다. 흐름: 구독 → 첫 상태 확인 → 두 목록의 분류 조건 검증.
     */
    // 초기_상태는_읽음_안읽음으로_분류되어_있다 함수를 선언한다. runTest의 코루틴 테스트 범위에서 실행하며 테스트 디스패처의 가상 시각을 사용한다.
    fun 초기_상태는_읽음_안읽음으로_분류되어_있다() = runTest {
        // Turbine으로 uiState를 구독한다. 이 구독이 WhileSubscribed의 upstream 수집을 시작하게 한다.
        viewModel.uiState.test {
            // 첫 상태 방출(SampleData 기반 분류 결과)을 기다려 지역 변수에 보관한다.
            val state = awaitItem()
            // unread 목록의 모든 항목이 isRead=false인지 확인한다. 하나라도 읽은 항목이 섞여 있으면 실패한다.
            assertThat(state.unread.all { !it.isRead }).isTrue()
            // read 목록의 모든 항목이 isRead=true인지 확인한다.
            assertThat(state.read.all { it.isRead }).isTrue()
            // Turbine 구독을 취소하고 남은 이벤트는 이 검증 범위에서 제외한다.
            cancelAndIgnoreRemainingEvents()
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }

    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 모두 읽음 처리하면 unread가 비워진다. 흐름: 초기 상태 소비 → markAllAsRead 호출 → 다음 상태의 unread 확인.
     */
    // 모두_읽음_처리하면_unread가_비워진다 함수를 선언한다. runTest의 코루틴 테스트 범위에서 실행하며 테스트 디스패처의 가상 시각을 사용한다.
    fun 모두_읽음_처리하면_unread가_비워진다() = runTest {
        // Turbine으로 uiState를 구독한다.
        viewModel.uiState.test {
            // 초기 상태 (일부 unread 존재)
            // 첫 상태 방출을 소비한다. 아직 markAllAsRead를 호출하지 않은 시점의 분류 결과다.
            awaitItem()
            // 모두 읽음 처리를 호출한다. 내부적으로 Repository의 목록이 갱신되고 Flow가 새 값을 방출한다.
            viewModel.markAllAsRead()
            // 갱신을 반영한 다음 상태를 기다려 지역 변수에 보관한다.
            val after = awaitItem()
            // 모두 읽음 처리 후에는 읽지 않은 알림이 하나도 없어야 한다.
            assertThat(after.unread).isEmpty()
            // Turbine 구독을 취소하고 남은 이벤트는 이 검증 범위에서 제외한다.
            cancelAndIgnoreRemainingEvents()
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
// 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
}
```

두 번째 테스트는 `markAllAsRead()`가 ViewModel의 상태를 직접 바꾸는 게 아니라 Repository를 거쳐 Flow가 다시 방출되는 것을 확인한다는 점이 핵심이다. `awaitItem()`을 두 번 호출하는 순서(초기값 소비 → 이벤트 실행 → 갱신값 확인)를 바꾸면 어떤 상태를 비교하는지가 달라진다.

## NotificationScreenTest.kt

원본: `app/src/androidTest/java/com/example/attendance/feature/notification/presentation/NotificationScreenTest.kt`

```kotlin
// class NotificationScreenTest를 선언하고 관련 상태와 메서드를 묶는다.
class NotificationScreenTest {

    // 프로퍼티 getter에 JUnit Rule을 붙여 테스트 시작·종료 때 Main 디스패처 교체를 수행하게 한다.
    @get:Rule
    // Compose UI 구성·동기화·조작·단언을 수행할 JUnit Rule을 만든다.
    val composeTestRule = createComposeRule()

    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 알림이 없으면 빈 상태 문구가 표시된다. 흐름: 빈 NotificationUiState로 Content 구성 → 안내 문구 표시 검증.
     */
    // 알림이_없으면_빈_상태_문구가_표시된다 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
    fun 알림이_없으면_빈_상태_문구가_표시된다() {
        // 계측 테스트용 화면에 Compose UI를 설정한다. UI 조작은 이 구성 이후 수행한다.
        composeTestRule.setContent {
            // 앱의 Compose 테마 아래에서 UI를 구성하여 프로젝트 스타일을 적용한다.
            AttendanceTheme {
                // 기본값(빈 두 목록) 상태를 넣어 isEmpty가 true가 되는 조건을 구성한다.
                NotificationContent(uiState = NotificationUiState(), onMarkAllAsRead = {})
            // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
            }
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }

        // 표시 문자열 "알림이 없습니다"를 가진 의미 노드를 찾는다. 실제 표시 여부를 검증한다.
        composeTestRule.onNodeWithText("알림이 없습니다").assertIsDisplayed()
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }

    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 모두 읽음 처리 버튼 클릭 시 콜백이 호출된다. 흐름: 콜백 기록 변수 준비 → 읽지 않은 알림이 있는 Content 구성 → 버튼 클릭 → 기록 검증.
     */
    // 모두_읽음_처리_버튼_클릭시_콜백이_호출된다 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
    fun 모두_읽음_처리_버튼_클릭시_콜백이_호출된다() {
        // 클릭 콜백이 실행됐는지 기록할 지역 변수를 만든다.
        var clicked = false
        // 계측 테스트용 화면에 Compose UI를 설정한다.
        composeTestRule.setContent {
            // 앱의 Compose 테마 아래에서 UI를 구성하여 프로젝트 스타일을 적용한다.
            AttendanceTheme {
                // 상태와 콜백으로만 그리는 Content를 호출한다.
                NotificationContent(
                    // 읽지 않은 알림이 존재해야 버튼이 화면에 의미 있게 나타나므로 샘플 데이터에서 걸러 넣는다.
                    uiState = NotificationUiState(
                        unread = SampleData.notifications.filter { !it.isRead }
                    ),
                    // 버튼 클릭 콜백이 실행되면 기록 변수를 true로 바꾸도록 설정한다.
                    onMarkAllAsRead = { clicked = true }
                // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
                )
            // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
            }
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }

        // 표시 문자열 "모두 읽음 처리"를 가진 의미 노드를 찾아 클릭해 이벤트를 발생시킨다.
        composeTestRule.onNodeWithText("모두 읽음 처리").performClick()
        // 콜백 기록 값이 true인지 확인한다.
        assertThat(clicked).isTrue()
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
// 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
}
```

두 UI 테스트는 `NotificationContent`만 호출하고 `NotificationScreen`이나 `NotificationViewModel`, 실제 Repository는 구성하지 않는다. 그래서 Factory, `BackTopBar`의 실제 뒤로가기 동작, Repository 공유(홈 화면과의 동기화)는 이 테스트로 검증되지 않는다.
