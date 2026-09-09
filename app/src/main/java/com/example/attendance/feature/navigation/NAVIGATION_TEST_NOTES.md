# AppNavGraphTest.kt 줄별 해설

실제 파일: `app/src/androidTest/java/com/example/attendance/core/navigation/AppNavGraphTest.kt`. package/import는 생략했다. 함수 위의 설명은 책임을, 각 줄 위의 주석은 실행 순서와 문법의 의미를 설명한다.

```kotlin
/** 실제 그래프·Factory·Fake Repository를 사용해 화면 연결 계약을 검증한다. */
// 서버 연동 테스트가 아니라 Compose Navigation 통합 테스트다.
class AppNavGraphTest {
    // Kotlin 프로퍼티의 getter에 JUnit Rule을 적용한다.
    @get:Rule
    // UI 렌더링과 입력 처리를 테스트와 동기화한다.
    val rule = createComposeRule()
    // 실제 그래프의 목적지와 백스택을 관찰할 controller다.
    private lateinit var controller: TestNavHostController

    /** Splash 목적지를 제거한 뒤 Login만 남는지 확인한다. */
    // JUnit이 독립적으로 실행할 테스트 메서드임을 표시한다.
    @Test
    // 실제 Splash 타이머와 그래프 콜백 연결을 확인한다.
    fun 스플래시_종료_후_로그인으로_이동한다() {
        // 기본 앱과 같은 시작 화면이다.
        showGraph(ScreenRoute.Splash.route)
        // suspend 타이머 이후 로그인 화면이 나타날 때까지 기다린다.
        rule.waitUntil(5_000) { rule.onAllNodesWithText("로그인").fetchSemanticsNodes().isNotEmpty() }
        // Navigation 상태는 UI 스레드가 안정된 시점에 읽는다.
        rule.runOnIdle {
            // 로그인에 도착했다.
            assertThat(controller.currentDestination?.route).isEqualTo(ScreenRoute.Login.route)
            // 스플래시로 돌아갈 화면이 남아 있지 않다.
            assertThat(controller.previousBackStackEntry).isNull()
        // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
        }
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }

    /** 로그인 성공 뒤 뒤로가기로 로그인 화면에 복귀하지 않도록 확인한다. */
    // JUnit이 독립적으로 실행할 테스트 메서드임을 표시한다.
    @Test
    // 실제 Fake 로그인과 성공 효과를 통과한다.
    fun 로그인_성공_후_홈으로_이동하고_로그인을_제거한다() {
        // 스플래시 대기 없이 로그인에서 시작한다.
        showGraph(ScreenRoute.Login.route)
        // 입력·클릭·Fake 응답을 거쳐 홈까지 이동한다.
        login()
        // 현재와 이전 목적지를 확인한다.
        rule.runOnIdle {
            // Main 그래프의 시작 화면은 홈이다.
            assertThat(controller.currentDestination?.route).isEqualTo(ScreenRoute.Home.route)
            // 로그인 목적지가 제거되어야 한다.
            assertThat(controller.previousBackStackEntry).isNull()
        // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
        }
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }

    /** 탭 재선택은 중복을 만들지 않고 저장한 기록 탭 상태는 다시 복원한다. */
    // JUnit이 독립적으로 실행할 테스트 메서드임을 표시한다.
    @Test
    // saveState·restoreState·launchSingleTop의 조합을 검증한다.
    fun 탭_재선택과_왕복에서_기록_상태를_유지한다() {
        // 로그인 이후 그래프부터 시작한다.
        showGraph(ScreenRoute.Main.route)
        // 실제 하단 버튼으로 기록 탭에 진입한다.
        clickTab("기록")
        // 저장 여부를 관찰할 테스트 전용 값을 기록한다.
        rule.runOnIdle { controller.currentBackStackEntry!!.savedStateHandle["probe"] = "saved" }
        // 같은 탭을 다시 눌러 중복 방지를 확인한다.
        clickTab("기록")
        // 기록 탭을 저장하고 떠난다.
        clickTab("마이")
        // 저장된 기록 탭을 복원한다.
        clickTab("기록")
        // 복원 값과 백스택의 바로 이전 화면을 확인한다.
        rule.runOnIdle {
            // 새 기록 화면으로 대체되지 않았다.
            assertThat(controller.currentBackStackEntry!!.savedStateHandle.get<String>("probe")).isEqualTo("saved")
            // 탭들이 계속 누적되지 않고 홈 위에 하나만 남는다.
            assertThat(controller.previousBackStackEntry?.destination?.route).isEqualTo(ScreenRoute.Home.route)
        // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
        }
        // 현재 탭 선택 표시도 일치해야 한다.
        rule.onNode(hasText("기록") and hasClickAction()).assertIsSelected()
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }

    /** 기록 버튼의 ID가 SavedStateHandle을 통해 조회되고 제출 성공 시 기록으로 복귀한다. */
    // JUnit이 독립적으로 실행할 테스트 메서드임을 표시한다.
    @Test
    // 행 콜백·route·Factory·완료 콜백을 함께 검증한다.
    fun 기록에서_정정_인자를_전달하고_제출_후_복귀한다() {
        // 홈에서 시작한다.
        showGraph(ScreenRoute.Main.route)
        // 실제 기록 탭으로 이동한다.
        clickTab("기록")
        // Repository Flow가 기록을 표시할 때까지 기다린다.
        rule.waitUntil(5_000) { rule.onAllNodesWithText("정정 요청").fetchSemanticsNodes().isNotEmpty() }
        // 샘플의 첫 정정 대상은 ID 2 알고리즘 기록이다.
        rule.onAllNodesWithText("정정 요청")[0].performScrollTo().performClick()
        // Long 타입 인자 전달을 확인한다.
        rule.runOnIdle { assertThat(controller.currentBackStackEntry!!.arguments!!.getLong(ScreenRoute.Correction.ARG_RECORD_ID)).isEqualTo(2L) }
        // ViewModel이 실제 인자로 조회한 대상이 화면에 보인다.
        rule.onNodeWithText("알고리즘", substring = true).assertIsDisplayed()
        // 필수 사유를 입력한다.
        rule.onNodeWithText("예) NFC 오류").performTextInput("NFC 오류")
        // Fake 접수를 요청한다.
        rule.onNodeWithText("제출").performScrollTo().performClick()
        // 성공 효과가 화면을 제거할 때까지 기다린다.
        rule.waitUntil(5_000) { rule.onAllNodesWithText("출석 정정 요청").fetchSemanticsNodes().isEmpty() }
        // 기록 탭으로 돌아왔다.
        rule.runOnIdle { assertThat(controller.currentDestination?.route).isEqualTo(ScreenRoute.History.route) }
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }

    /** 로그아웃 시 저장해둔 탭까지 제거되어 다음 로그인에서 복원되지 않아야 한다. */
    // JUnit이 독립적으로 실행할 테스트 메서드임을 표시한다.
    @Test
    // Repository 로그아웃 연습 과제와 별개인 화면 스택 정리 테스트다.
    fun 로그아웃_후_이전_화면과_저장된_탭을_복원하지_않는다() {
        // 메인 화면에서 시작한다.
        showGraph(ScreenRoute.Main.route)
        // 저장할 탭을 연다.
        clickTab("기록")
        // 이전 화면 상태를 구분하는 표식을 넣는다.
        rule.runOnIdle { controller.currentBackStackEntry!!.savedStateHandle["probe"] = "old-user" }
        // 기록 탭을 저장하고 마이페이지로 이동한다.
        clickTab("마이")
        // 확인창을 연다.
        rule.onNodeWithText("로그아웃").performScrollTo().performClick()
        // 실제 확인 콜백으로 로그아웃 이동을 실행한다.
        rule.onNodeWithText("확인").performClick()
        // 현재 화면과 남은 이전 화면을 확인한다.
        rule.runOnIdle {
            // 로그인으로 돌아간다.
            assertThat(controller.currentDestination?.route).isEqualTo(ScreenRoute.Login.route)
            // 뒤로가기로 메인에 재진입할 수 없다.
            assertThat(controller.previousBackStackEntry).isNull()
        // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
        }
        // 새 로그인 흐름을 실행한다.
        login()
        // 이전과 같은 탭에 진입한다.
        clickTab("기록")
        // 저장했던 이전 탭 상태가 복원되면 실패한다.
        rule.runOnIdle { assertThat(controller.currentBackStackEntry!!.savedStateHandle.get<String>("probe")).isNull() }
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }

    /** 홈의 출석 체크 버튼이 상세 화면으로 연결되고 뒤로가기로 홈에 복귀한다. */
    // JUnit이 독립적으로 실행할 테스트 메서드임을 표시한다.
    @Test
    // 상세 화면과 홈 사이 백스택 연결을 확인한다.
    fun 홈에서_출석_체크로_이동하고_뒤로_돌아온다() {
        // 메인 화면을 연다.
        showGraph(ScreenRoute.Main.route)
        // 홈 세션 Flow의 표시를 기다린다.
        rule.waitUntil(5_000) { rule.onAllNodesWithText("출석 체크").fetchSemanticsNodes().isNotEmpty() }
        // 홈의 활성 세션 버튼을 누른다.
        rule.onNodeWithText("출석 체크").performScrollTo().performClick()
        // 상세 경로로 이동했는지 확인한다.
        rule.runOnIdle {
            // 출석 체크 목적지다.
            assertThat(controller.currentDestination?.route).isEqualTo(ScreenRoute.Attendance.route)
            // Navigation의 뒤로가기 동작을 실행한다.
            controller.popBackStack()
        // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
        }
        // 복귀 후 홈 탭 선택을 확인한다.
        rule.onNode(hasText("홈") and hasClickAction()).assertIsSelected()
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }

    /** 테스트용 controller에 Compose 목적지 navigator를 설치하고 실제 그래프를 렌더링한다. */
    // 테스트별 시작점만 바꾸며 실제 화면 구성은 재사용한다.
    private fun showGraph(start: String) {
        // 테스트 Activity에 UI를 설치한다.
        rule.setContent {
            // 앱 Application을 찾을 수 있는 Activity 문맥이다.
            val context = LocalContext.current
            // NavHost가 Compose 목적지를 실행할 수 있게 한다.
            controller = remember { TestNavHostController(context).apply { navigatorProvider.addNavigator(ComposeNavigator()) } }
            // 실제 Factory와 화면을 쓰는 통합 검증이다.
            AttendanceTheme { AppNavGraph(controller, startDestination = start) }
        // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
        }
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }

    /** 제목과 같은 문자열이 있어도 클릭 가능한 하단 탭만 선택한다. */
    // 알림 화면의 제목과 탭을 혼동하지 않도록 matcher를 결합한다.
    private fun clickTab(label: String) {
        // 실제 탭 UI를 누른다.
        rule.onNode(hasText(label) and hasClickAction()).performClick()
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }

    /** 실제 로그인 폼을 입력하고 Fake 로그인 응답 후 홈 탭을 기다린다. */
    // 두 테스트에서 동일한 사용자 로그인 과정을 재사용한다.
    private fun login() {
        // 실제 로그인 필드에 학번을 입력한다.
        rule.onNodeWithText("학번").performTextInput("2021000000")
        // 비밀번호 필드에 값을 입력한다.
        rule.onNodeWithText("비밀번호").performTextInput("password")
        // 입력 키보드가 로그인 버튼을 가리지 않도록 닫는다.
        Espresso.closeSoftKeyboard()
        // LoginViewModel의 비동기 로그인을 시작한다.
        rule.onNodeWithText("로그인").performClick()
        // Fake 지연 이후 홈 탭이 표시될 때까지 기다린다.
        rule.waitUntil(5_000) { rule.onAllNodesWithText("홈").fetchSemanticsNodes().isNotEmpty() }
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }
// 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
}
```

# BottomNavigationBarTest.kt 줄별 해설

실제 파일: `app/src/androidTest/java/com/example/attendance/core/navigation/BottomNavigationBarTest.kt`. package/import는 생략했다. 함수 위의 설명은 책임을, 각 줄 위의 주석은 실행 순서와 문법의 의미를 설명한다.

```kotlin
/** 가이드의 하단 탭 렌더링 스모크 테스트다. */
// 그래프 이동은 AppNavGraphTest에서 별도로 검증한다.
class BottomNavigationBarTest {
    // Kotlin 프로퍼티의 getter에 JUnit Rule을 적용한다.
    @get:Rule
    // Compose 테스트 화면을 준비한다.
    val rule = createComposeRule()

    /** 네 개 탭이 모두 사용자에게 표시되는지 확인한다. */
    // JUnit이 독립적으로 실행할 테스트 메서드임을 표시한다.
    @Test
    // 표시 이름과 개수를 검증한다.
    fun 하단_탭_4개가_표시된다() {
        // 테스트 Activity에 하단 바를 렌더링한다.
        rule.setContent {
            // 이동하지 않는 표시 테스트이므로 목적지 그래프는 필요 없다.
            AttendanceTheme { BottomNavigationBar(rememberNavController()) }
        // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
        }
        // 요구된 네 라벨을 순서대로 확인한다.
        listOf("홈", "알림", "기록", "마이").forEach { label ->
            // 각 라벨이 화면에 보여야 한다.
            rule.onNodeWithText(label).assertIsDisplayed()
        // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
        }
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }
// 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
}
```
