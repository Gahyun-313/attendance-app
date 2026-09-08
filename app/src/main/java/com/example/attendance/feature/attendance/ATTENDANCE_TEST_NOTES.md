# 출석 체크 단위 테스트와 UI 테스트 줄별 해설

[출석 체크 구조·문법·호출 흐름으로 돌아가기](README.md)

package/import와 기존 주석은 생략했다. 아래 코드의 각 실행 줄에 상세 설명을 붙였으며, 동작은 실제 소스와 같다. 사용자가 직접 실행해 아래 4개 테스트 모두 통과를 확인했다(자세한 내용은 VALIDATION.md).

## AttendanceViewModelTest.kt

원본: `app/src/test/java/com/example/attendance/feature/attendance/presentation/AttendanceViewModelTest.kt`

```kotlin
// class AttendanceViewModelTest를 선언하고 관련 상태와 메서드를 묶는다.
class AttendanceViewModelTest {

    // 프로퍼티 getter에 JUnit Rule을 붙여 테스트 시작·종료 때 Main 디스패처 교체를 수행하게 한다.
    @get:Rule
    // 인자 없이 생성하면 기본값인 UnconfinedTestDispatcher를 쓴다. 이 디스패처는 launch한 코루틴을 첫 지연(delay) 지점까지 즉시 실행하므로, 이 테스트에서는 runCurrent() 없이도 startAttendance() 호출 직후의 상태를 바로 확인할 수 있다.
    val mainDispatcherRule = MainDispatcherRule()

    // 각 테스트의 setUp에서 초기화할 viewModel: AttendanceViewModel 프로퍼티다. private으로 외부 접근을 막으며 초기화 전 사용하면 오류가 난다.
    private lateinit var viewModel: AttendanceViewModel

    // 각 테스트 시작 전 다음 메서드를 실행한다.
    @Before
    /**
     * 책임: 테스트 간 데이터 격리. 흐름: 새 Fake Repository → 새 ViewModel에 생성자 주입. 각 테스트 전에 JUnit이 호출한다.
     */
    // setUp 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
    fun setUp() {
        // Factory나 AppContainer 없이 FakeAttendanceRepository를 직접 생성자에 주입한다. Android 컴포넌트가 필요 없는 단위 테스트다.
        viewModel = AttendanceViewModel(FakeAttendanceRepository())
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }

    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 모달을 닫으면 진행 중인 스캔이 취소되고 결과가 반영되지 않는다. 흐름: 스캔 시작 → 스캔 중 상태 확인 → 취소 → 취소 후 상태 확인.
     */
    // 모달을_닫으면_진행중이던_스캔이_취소되고_결과가_반영되지_않는다 함수를 선언한다. runTest의 코루틴 테스트 범위에서 실행하며 테스트 디스패처의 가상 시각을 사용한다.
    fun 모달을_닫으면_진행중이던_스캔이_취소되고_결과가_반영되지_않는다() = runTest {
        // 스캔을 시작한다. UnconfinedTestDispatcher라 delay(2_000) 지점에서 멈춘 채로 이 호출이 반환된다.
        viewModel.startAttendance()
        // 스캔이 시작되어 모달이 떠 있는 상태인지 확인한다.
        assertThat(viewModel.uiState.value.isNfcScanning).isTrue()

        // NFC 모달을 닫는 동작을 호출한다. 내부에서 scanJob.cancel()이 실행되어 delay 중이던 코루틴이 취소된다.
        viewModel.cancelScan()

        // 취소로 인해 스캔 중 플래그가 false로 돌아왔는지 확인한다.
        assertThat(viewModel.uiState.value.isNfcScanning).isFalse()
        // checkAttendance 호출이 취소되어 결과 갱신 코드에 도달하지 못했으므로 result가 여전히 null인지 확인한다.
        assertThat(viewModel.uiState.value.result).isNull()
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }

    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 스캔이 끝까지 진행되면 결과가 출석으로 반영된다. 흐름: 스캔 시작 → 가상 시간을 끝까지 흘려보냄 → 완료 후 상태 확인.
     */
    // 스캔이_끝까지_진행되면_결과가_출석으로_반영된다 함수를 선언한다. runTest의 코루틴 테스트 범위에서 실행하며 테스트 디스패처의 가상 시각을 사용한다.
    fun 스캔이_끝까지_진행되면_결과가_출석으로_반영된다() = runTest {
        // 스캔을 시작한다. 이 시점에서는 아직 delay(2_000) 중이라 결과가 반영되지 않은 상태다.
        viewModel.startAttendance()
        // FakeAttendanceRepository의 delay(2_000)을 가상 시간으로 흘려보낸다
        // 대기 중인 모든 코루틴의 가상 시간을 끝까지 흘려보내 delay(2_000)을 통과시킨다. cancelScan()을 호출하지 않았으므로 취소되지 않고 끝까지 진행된다.
        advanceUntilIdle()

        // 스캔이 끝났으므로 모달이 닫혀 있는지(isNfcScanning=false) 확인한다.
        assertThat(viewModel.uiState.value.isNfcScanning).isFalse()
        // FakeAttendanceRepository.checkAttendance()가 항상 PRESENT를 반환하므로 result가 PRESENT인지 확인한다.
        assertThat(viewModel.uiState.value.result).isEqualTo(AttendanceStatus.PRESENT)
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
// 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
}
```

두 테스트는 같은 `startAttendance()` 호출 뒤에 서로 다른 조작(취소 vs 가상 시간 진행)을 해서 하나의 `scanJob`이 두 가지 경로로 끝날 수 있음을 보여준다. `cancelScan()`을 호출하는 순간과 `advanceUntilIdle()`을 호출하는 순간의 순서를 바꾸면 두 테스트의 의미가 뒤바뀐다.

## AttendanceScreenTest.kt

원본: `app/src/androidTest/java/com/example/attendance/feature/attendance/presentation/AttendanceScreenTest.kt`

```kotlin
// class AttendanceScreenTest를 선언하고 관련 상태와 메서드를 묶는다.
class AttendanceScreenTest {

    // 프로퍼티 getter에 JUnit Rule을 붙여 테스트 시작·종료 때 Main 디스패처 교체를 수행하게 한다.
    @get:Rule
    // Compose UI 구성·동기화·조작·단언을 수행할 JUnit Rule을 만든다.
    val composeTestRule = createComposeRule()

    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 출석 시작 버튼 클릭 시 콜백이 호출된다. 흐름: 콜백 기록 변수 준비 → Content 구성 → 버튼 클릭 → 기록 검증.
     */
    // 출석_시작_버튼_클릭시_콜백이_호출된다 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
    fun 출석_시작_버튼_클릭시_콜백이_호출된다() {
        // 클릭 콜백이 실행됐는지 기록할 지역 변수를 만든다.
        var started = false
        // 계측 테스트용 화면에 Compose UI를 설정한다. UI 조작은 이 구성 이후 수행한다.
        composeTestRule.setContent {
            // 앱의 Compose 테마 아래에서 UI를 구성하여 프로젝트 스타일을 적용한다.
            AttendanceTheme {
                // 상태와 콜백으로만 그리는 Content를 호출한다. ViewModel이나 Repository는 만들지 않는다.
                AttendanceContent(
                    // 미출석 상태를 직접 넣어 버튼이 활성화된 화면을 구성한다.
                    uiState = AttendanceUiState(status = AttendanceStatus.NOT_YET),
                    // 이 테스트에서는 뒤로가기를 검증하지 않으므로 빈 람다를 전달한다.
                    onBack = {},
                    // 버튼 클릭 콜백이 실행되면 기록 변수를 true로 바꾸도록 설정한다.
                    onStartAttendance = { started = true }
                // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
                )
            // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
            }
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }

        // 표시 문자열 "출석 시작"을 가진 의미 노드를 찾아 클릭해 이벤트를 발생시킨다.
        composeTestRule.onNodeWithText("출석 시작").performClick()
        // 콜백 기록 값이 true인지 확인한다.
        assertThat(started).isTrue()
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }

    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 이미 출석했다면 출석 시작 버튼이 비활성화된다. 흐름: PRESENT 상태로 Content 구성 → 버튼 비활성 상태 검증.
     */
    // 이미_출석했다면_출석_시작_버튼이_비활성화된다 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
    fun 이미_출석했다면_출석_시작_버튼이_비활성화된다() {
        // 계측 테스트용 화면에 Compose UI를 설정한다. UI 조작은 이 구성 이후 수행한다.
        composeTestRule.setContent {
            // 앱의 Compose 테마 아래에서 UI를 구성하여 프로젝트 스타일을 적용한다.
            AttendanceTheme {
                // 상태와 콜백으로만 그리는 Content를 호출한다.
                AttendanceContent(
                    // 이미 출석(PRESENT) 상태를 넣어 canStart가 false가 되는 조건을 구성한다.
                    uiState = AttendanceUiState(status = AttendanceStatus.PRESENT),
                    // 이 테스트에서는 뒤로가기를 검증하지 않으므로 빈 람다를 전달한다.
                    onBack = {},
                    // 비활성화된 버튼이라 클릭되지 않을 것을 검증하는 테스트이므로 콜백 내용은 비워 둔다.
                    onStartAttendance = {}
                // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
                )
            // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
            }
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }

        // 표시 문자열 "출석 시작"을 가진 의미 노드를 찾아 비활성화 상태인지 확인한다.
        composeTestRule.onNodeWithText("출석 시작").assertIsNotEnabled()
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
// 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
}
```

두 UI 테스트는 `AttendanceContent`만 호출하고 `AttendanceScreen`이나 `AttendanceViewModel`, 다이얼로그는 구성하지 않는다. 그래서 NFC 모달·결과 모달이 뜨는 경로나 Factory·NavHostController는 이 테스트로 검증되지 않는다.
