package com.example.attendance.feature.history.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.attendance.core.model.AttendanceRecord
import com.example.attendance.core.model.AttendanceStatus
import com.example.attendance.core.model.SemesterStats
import com.example.attendance.ui.theme.AttendanceTheme
import org.junit.Rule
import org.junit.Test

class AttendanceHistoryScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun 통계와_달력이_표시된다() {
        composeTestRule.setContent {
            AttendanceTheme {
                HistoryContent(
                    uiState = HistoryUiState(
                        stats = SemesterStats(22, 3, 1, 88),
                        records = listOf(AttendanceRecord(1, "자료구조", "1교시", AttendanceStatus.PRESENT, dayOfMonth = 3))
                    ),
                    onPrevMonth = {}, onNextMonth = {}, onDayClick = {}
                )
            }
        }
        composeTestRule.onNodeWithText("내 출석 기록").assertIsDisplayed()
        composeTestRule.onNodeWithText("2025년 6월").assertIsDisplayed()
        composeTestRule.onNodeWithText("자료구조 1교시").assertIsDisplayed()
    }

    @Test
    fun 다음_달_버튼_클릭시_콜백이_호출된다() {
        var clicked = false
        composeTestRule.setContent {
            AttendanceTheme {
                HistoryContent(
                    uiState = HistoryUiState(),
                    onPrevMonth = {}, onNextMonth = { clicked = true }, onDayClick = {}
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("다음 달").performClick()
        assert(clicked)
    }
}
