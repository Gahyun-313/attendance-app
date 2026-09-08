package com.example.attendance.feature.attendance.presentation

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.attendance.core.model.AttendanceStatus
import com.example.attendance.ui.theme.AttendanceTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class AttendanceScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun 출석_시작_버튼_클릭시_콜백이_호출된다() {
        var started = false
        composeTestRule.setContent {
            AttendanceTheme {
                AttendanceContent(
                    uiState = AttendanceUiState(status = AttendanceStatus.NOT_YET),
                    onBack = {},
                    onStartAttendance = { started = true }
                )
            }
        }

        composeTestRule.onNodeWithText("출석 시작").performClick()
        assertThat(started).isTrue()
    }

    @Test
    fun 이미_출석했다면_출석_시작_버튼이_비활성화된다() {
        composeTestRule.setContent {
            AttendanceTheme {
                AttendanceContent(
                    uiState = AttendanceUiState(status = AttendanceStatus.PRESENT),
                    onBack = {},
                    onStartAttendance = {}
                )
            }
        }

        composeTestRule.onNodeWithText("출석 시작").assertIsNotEnabled()
    }
}
