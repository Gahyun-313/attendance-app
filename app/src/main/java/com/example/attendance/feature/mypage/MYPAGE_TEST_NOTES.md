# MyPageScreenTest.kt 줄별 해설

실제 파일: `app/src/androidTest/java/com/example/attendance/feature/mypage/presentation/MyPageScreenTest.kt`. package/import는 생략했다. 함수 위의 설명은 책임을, 각 줄 위의 주석은 실행 순서와 문법의 의미를 설명한다.

```kotlin
/** ViewModel 연습 과제와 분리해서 기본 UI와 확인창 동작만 검증한다. */
// MyPageViewModelTest를 대신하는 도메인 테스트가 아니다.
class MyPageScreenTest {
    // Kotlin 프로퍼티의 getter에 JUnit Rule을 적용한다.
    @get:Rule
    // Compose UI 테스트 규칙이다.
    val rule = createComposeRule()

    /** 기본 프로필과 계정 메뉴가 표시되는지 확인한다. */
    // JUnit이 독립적으로 실행할 테스트 메서드임을 표시한다.
    @Test
    // 가이드의 마이페이지 렌더링 검증이다.
    fun 기본_정보와_메뉴가_표시된다() {
        // 실제 마이페이지를 표시한다.
        showPage()
        // 화면 제목을 확인한다.
        rule.onNodeWithText("마이페이지").assertIsDisplayed()
        // 정적 프로필을 확인한다.
        rule.onNodeWithText("학생 이름").assertIsDisplayed()
        // 작은 기기에서도 메뉴를 찾아 확인한다.
        rule.onNodeWithText("로그아웃").performScrollTo().assertIsDisplayed()
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }

    /** 취소하면 이동하지 않고, 다시 열어 확인했을 때만 로그아웃 이벤트를 보낸다. */
    // JUnit이 독립적으로 실행할 테스트 메서드임을 표시한다.
    @Test
    // 확인창 표시와 취소·확인 경로를 한 흐름으로 검증한다.
    fun 로그아웃_확인시에만_콜백을_호출한다() {
        // 이동 요청 횟수다.
        var count = 0
        // 외부 콜백을 관찰한다.
        showPage(onLogout = { count++ })
        // 확인창을 연다.
        rule.onNodeWithText("로그아웃").performScrollTo().performClick()
        // 확인창 제목을 확인한다.
        rule.onNodeWithText("로그아웃 하시겠어요?").assertIsDisplayed()
        // 취소는 화면 이동을 요청하지 않아야 한다.
        rule.onNodeWithText("취소").performClick()
        // 취소 시 호출이 없음을 확인한다.
        rule.runOnIdle { assertThat(count).isEqualTo(0) }
        // 다시 확인창을 연다.
        rule.onNodeWithText("로그아웃").performScrollTo().performClick()
        // 로그아웃 이동을 승인한다.
        rule.onNodeWithText("확인").performClick()
        // 정확히 한 번 호출된다.
        rule.runOnIdle { assertThat(count).isEqualTo(1) }
        // 확인창 상태도 해제되었다.
        rule.onNodeWithText("로그아웃 하시겠어요?").assertDoesNotExist()
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }

    /** 아직 없는 설정 기능은 준비 중 안내를 보여주고 닫을 수 있어야 한다. */
    // JUnit이 독립적으로 실행할 테스트 메서드임을 표시한다.
    @Test
    // 무반응 메뉴가 생기지 않도록 안내 연결을 확인한다.
    fun 설정_메뉴는_준비중_안내를_표시한다() {
        // 기본 마이페이지를 연다.
        showPage()
        // 프로필 메뉴를 누른다.
        rule.onNodeWithText("프로필 관리").performScrollTo().performClick()
        // 기능 범위를 정확히 안내한다.
        rule.onNodeWithText("준비 중인 기능입니다.").assertIsDisplayed()
        // 안내를 닫는다.
        rule.onNodeWithText("닫기").performClick()
        // 알림 설정도 안내를 제공한다.
        rule.onNodeWithText("알림 설정").performScrollTo().performClick()
        // 두 메뉴 모두 연결되어 있다.
        rule.onNodeWithText("준비 중인 기능입니다.").assertIsDisplayed()
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }

    /** 테마와 외부 콜백을 준비해 실제 화면을 렌더링한다. */
    // 테스트별로 필요한 이벤트만 주입한다.
    private fun showPage(onLogout: () -> Unit = {}) {
        // ViewModel·NavController 없이 화면 자체를 검증한다.
        rule.setContent { AttendanceTheme { MyPageScreen(onLogout = onLogout) } }
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }
// 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
}
```
