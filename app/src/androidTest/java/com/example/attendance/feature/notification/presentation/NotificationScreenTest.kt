package com.example.attendance.feature.notification.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.attendance.core.model.SampleData
import com.example.attendance.ui.theme.AttendanceTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class NotificationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun 알림이_없으면_빈_상태_문구가_표시된다() {
        composeTestRule.setContent {
            AttendanceTheme {
                NotificationContent(uiState = NotificationUiState(), onMarkAllAsRead = {})
            }
        }

        composeTestRule.onNodeWithText("알림이 없습니다").assertIsDisplayed()
    }

    @Test
    fun 모두_읽음_처리_버튼_클릭시_콜백이_호출된다() {
        var clicked = false
        composeTestRule.setContent {
            AttendanceTheme {
                NotificationContent(
                    uiState = NotificationUiState(
                        unread = SampleData.notifications.filter { !it.isRead }
                    ),
                    onMarkAllAsRead = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("모두 읽음 처리").performClick()
        assertThat(clicked).isTrue()
    }
}
