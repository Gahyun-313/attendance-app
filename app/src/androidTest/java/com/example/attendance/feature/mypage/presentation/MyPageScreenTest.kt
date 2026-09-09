package com.example.attendance.feature.mypage.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.attendance.ui.theme.AttendanceTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

/** ViewModel 연습 과제와 분리해서 기본 UI와 확인창 동작만 검증한다. */
class MyPageScreenTest {
    @get:Rule
    val rule = createComposeRule()

    /** 기본 프로필과 계정 메뉴가 표시되는지 확인한다. */
    @Test
    fun 기본_정보와_메뉴가_표시된다() {
        showPage()
        rule.onNodeWithText("마이페이지").assertIsDisplayed()
        rule.onNodeWithText("학생 이름").assertIsDisplayed()
        rule.onNodeWithText("로그아웃").performScrollTo().assertIsDisplayed()
    }

    /** 취소하면 이동하지 않고, 다시 열어 확인했을 때만 로그아웃 이벤트를 보낸다. */
    @Test
    fun 로그아웃_확인시에만_콜백을_호출한다() {
        var count = 0
        showPage(onLogout = { count++ })
        rule.onNodeWithText("로그아웃").performScrollTo().performClick()
        rule.onNodeWithText("로그아웃 하시겠어요?").assertIsDisplayed()
        rule.onNodeWithText("취소").performClick()
        rule.runOnIdle { assertThat(count).isEqualTo(0) }
        rule.onNodeWithText("로그아웃").performScrollTo().performClick()
        rule.onNodeWithText("확인").performClick()
        rule.runOnIdle { assertThat(count).isEqualTo(1) }
        rule.onNodeWithText("로그아웃 하시겠어요?").assertDoesNotExist()
    }

    /** 아직 없는 설정 기능은 준비 중 안내를 보여주고 닫을 수 있어야 한다. */
    @Test
    fun 설정_메뉴는_준비중_안내를_표시한다() {
        showPage()
        rule.onNodeWithText("프로필 관리").performScrollTo().performClick()
        rule.onNodeWithText("준비 중인 기능입니다.").assertIsDisplayed()
        rule.onNodeWithText("닫기").performClick()
        rule.onNodeWithText("알림 설정").performScrollTo().performClick()
        rule.onNodeWithText("준비 중인 기능입니다.").assertIsDisplayed()
    }

    /** 테마와 외부 콜백을 준비해 실제 화면을 렌더링한다. */
    private fun showPage(onLogout: () -> Unit = {}) {
        rule.setContent { AttendanceTheme { MyPageScreen(onLogout = onLogout) } }
    }
}
