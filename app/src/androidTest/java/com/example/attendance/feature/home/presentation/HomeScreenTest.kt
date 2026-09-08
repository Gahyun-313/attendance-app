package com.example.attendance.feature.home.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.attendance.core.model.SampleData
import com.example.attendance.core.model.WeeklyStats
import com.example.attendance.ui.theme.AttendanceTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

/** ViewModel 대신 상태를 직접 주입하여 표시와 사용자 이벤트 전달을 검증한다. */
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun 이번_주_출석_현황이_표시된다() {
        showHome(HomeUiState(weeklyStats = WeeklyStats(3, 10, 55, 2)))
        composeTestRule.onNodeWithText("이번 주 출석 현황").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 / 10").assertIsDisplayed()
        composeTestRule.onNodeWithText("55%").assertIsDisplayed()
        composeTestRule.onNodeWithText("2회").assertIsDisplayed()
    }

    @Test
    fun 빈_목록에서도_각_섹션_제목이_표시된다() {
        showHome(HomeUiState())
        composeTestRule.onNodeWithText("0 / 0").assertIsDisplayed()
        composeTestRule.onNodeWithText("오늘 수업 세션").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("읽지 않은 알림").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("최근 출석 기록").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun 진행중_세션의_출석_체크를_누르면_콜백이_호출된다() {
        var clicked = false
        showHome(
            state = HomeUiState(todaySessions = SampleData.todaySessions),
            onCheckAttendance = { clicked = true }
        )
        composeTestRule.onNodeWithText("출석 체크").performScrollTo().performClick()
        composeTestRule.runOnIdle { assertThat(clicked).isTrue() }
    }

    @Test
    fun 알림과_기록의_더보기는_각각의_콜백을_호출한다() {
        var notificationsClicked = false
        var recordsClicked = false
        showHome(
            state = HomeUiState(),
            onMoreNotifications = { notificationsClicked = true },
            onMoreRecords = { recordsClicked = true }
        )
        // Content의 섹션 순서(알림 → 기록)에 따라 두 링크의 이벤트를 따로 검증한다.
        val moreLinks = composeTestRule.onAllNodesWithText("더보기")
        moreLinks.assertCountEquals(2)
        moreLinks[0].performScrollTo().performClick()
        composeTestRule.runOnIdle {
            assertThat(notificationsClicked).isTrue()
            assertThat(recordsClicked).isFalse()
        }
        moreLinks[1].performScrollTo().performClick()
        composeTestRule.runOnIdle { assertThat(recordsClicked).isTrue() }
    }

    /** 테스트마다 필요한 상태와 이벤트만 지정하여 Content를 앱 테마 안에 구성한다. */
    private fun showHome(
        state: HomeUiState,
        onCheckAttendance: () -> Unit = {},
        onMoreNotifications: () -> Unit = {},
        onMoreRecords: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            AttendanceTheme {
                HomeContent(
                    uiState = state,
                    onCheckAttendance = onCheckAttendance,
                    onMoreNotifications = onMoreNotifications,
                    onMoreRecords = onMoreRecords
                )
            }
        }
    }
}
