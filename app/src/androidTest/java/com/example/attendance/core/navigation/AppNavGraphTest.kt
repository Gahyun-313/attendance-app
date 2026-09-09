package com.example.attendance.core.navigation

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.example.attendance.ui.theme.AttendanceTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

/** 실제 그래프·Factory·Fake Repository를 사용해 화면 연결 계약을 검증한다. */
class AppNavGraphTest {
    @get:Rule
    val rule = createComposeRule()
    private lateinit var controller: TestNavHostController

    /** Splash 목적지를 제거한 뒤 Login만 남는지 확인한다. */
    @Test
    fun 스플래시_종료_후_로그인으로_이동한다() {
        showGraph(ScreenRoute.Splash.route)
        rule.waitUntil(5_000) { rule.onAllNodesWithText("로그인").fetchSemanticsNodes().isNotEmpty() }
        rule.runOnIdle {
            assertThat(controller.currentDestination?.route).isEqualTo(ScreenRoute.Login.route)
            assertThat(controller.previousBackStackEntry).isNull()
        }
    }

    /** 로그인 성공 뒤 뒤로가기로 로그인 화면에 복귀하지 않도록 확인한다. */
    @Test
    fun 로그인_성공_후_홈으로_이동하고_로그인을_제거한다() {
        showGraph(ScreenRoute.Login.route)
        login()
        rule.runOnIdle {
            assertThat(controller.currentDestination?.route).isEqualTo(ScreenRoute.Home.route)
            assertThat(controller.previousBackStackEntry).isNull()
        }
    }

    /** 탭 재선택은 중복을 만들지 않고 저장한 기록 탭 상태는 다시 복원한다. */
    @Test
    fun 탭_재선택과_왕복에서_기록_상태를_유지한다() {
        showGraph(ScreenRoute.Main.route)
        clickTab("기록")
        rule.runOnIdle { controller.currentBackStackEntry!!.savedStateHandle["probe"] = "saved" }
        clickTab("기록")
        clickTab("마이")
        clickTab("기록")
        rule.runOnIdle {
            assertThat(controller.currentBackStackEntry!!.savedStateHandle.get<String>("probe")).isEqualTo("saved")
            assertThat(controller.previousBackStackEntry?.destination?.route).isEqualTo(ScreenRoute.Home.route)
        }
        rule.onNode(hasText("기록") and hasClickAction()).assertIsSelected()
    }

    /** 기록 버튼의 ID가 SavedStateHandle을 통해 조회되고 제출 성공 시 기록으로 복귀한다. */
    @Test
    fun 기록에서_정정_인자를_전달하고_제출_후_복귀한다() {
        showGraph(ScreenRoute.Main.route)
        clickTab("기록")
        rule.waitUntil(5_000) { rule.onAllNodesWithText("정정 요청").fetchSemanticsNodes().isNotEmpty() }
        rule.onAllNodesWithText("정정 요청")[0].performScrollTo().performClick()
        rule.runOnIdle { assertThat(controller.currentBackStackEntry!!.arguments!!.getLong(ScreenRoute.Correction.ARG_RECORD_ID)).isEqualTo(2L) }
        rule.onNodeWithText("알고리즘", substring = true).assertIsDisplayed()
        rule.onNodeWithText("예) NFC 오류").performTextInput("NFC 오류")
        rule.onNodeWithText("제출").performScrollTo().performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithText("출석 정정 요청").fetchSemanticsNodes().isEmpty() }
        rule.runOnIdle { assertThat(controller.currentDestination?.route).isEqualTo(ScreenRoute.History.route) }
    }

    /** 로그아웃 시 저장해둔 탭까지 제거되어 다음 로그인에서 복원되지 않아야 한다. */
    @Test
    fun 로그아웃_후_이전_화면과_저장된_탭을_복원하지_않는다() {
        showGraph(ScreenRoute.Main.route)
        clickTab("기록")
        rule.runOnIdle { controller.currentBackStackEntry!!.savedStateHandle["probe"] = "old-user" }
        clickTab("마이")
        rule.onNodeWithText("로그아웃").performScrollTo().performClick()
        rule.onNodeWithText("확인").performClick()
        rule.runOnIdle {
            assertThat(controller.currentDestination?.route).isEqualTo(ScreenRoute.Login.route)
            assertThat(controller.previousBackStackEntry).isNull()
        }
        login()
        clickTab("기록")
        rule.runOnIdle { assertThat(controller.currentBackStackEntry!!.savedStateHandle.get<String>("probe")).isNull() }
    }

    /** 홈의 출석 체크 버튼이 상세 화면으로 연결되고 뒤로가기로 홈에 복귀한다. */
    @Test
    fun 홈에서_출석_체크로_이동하고_뒤로_돌아온다() {
        showGraph(ScreenRoute.Main.route)
        rule.waitUntil(5_000) { rule.onAllNodesWithText("출석 체크").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("출석 체크").performScrollTo().performClick()
        rule.runOnIdle {
            assertThat(controller.currentDestination?.route).isEqualTo(ScreenRoute.Attendance.route)
            controller.popBackStack()
        }
        rule.onNode(hasText("홈") and hasClickAction()).assertIsSelected()
    }

    /** 테스트용 controller에 Compose 목적지 navigator를 설치하고 실제 그래프를 렌더링한다. */
    private fun showGraph(start: String) {
        rule.setContent {
            val context = LocalContext.current
            controller = remember { TestNavHostController(context).apply { navigatorProvider.addNavigator(ComposeNavigator()) } }
            AttendanceTheme { AppNavGraph(controller, startDestination = start) }
        }
    }

    /** 제목과 같은 문자열이 있어도 클릭 가능한 하단 탭만 선택한다. */
    private fun clickTab(label: String) {
        rule.onNode(hasText(label) and hasClickAction()).performClick()
    }

    /** 실제 로그인 폼을 입력하고 Fake 로그인 응답 후 홈 탭을 기다린다. */
    private fun login() {
        rule.onNodeWithText("학번").performTextInput("2021000000")
        rule.onNodeWithText("비밀번호").performTextInput("password")
        Espresso.closeSoftKeyboard()
        rule.onNodeWithText("로그인").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithText("홈").fetchSemanticsNodes().isNotEmpty() }
    }
}
