package com.example.attendance.feature.auth.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.attendance.ui.theme.AttendanceTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

/** Content에 상태와 콜백을 직접 주입하여 UI 계약을 기기에서 검증한다. */
class LoginScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun 입력된_학번_값이_화면에_표시된다() {
        composeTestRule.setContent {
            AttendanceTheme {
                LoginContent(
                    uiState = LoginUiState(studentId = "2021000000", password = "pw1234"),
                    onStudentIdChange = {},
                    onPasswordChange = {},
                    onLoginClick = {}
                )
            }
        }
        composeTestRule.onNodeWithText("2021000000").assertIsDisplayed()
    }

    @Test
    fun 로딩_중에는_버튼_텍스트가_바뀌고_비활성화된다() {
        composeTestRule.setContent {
            AttendanceTheme {
                LoginContent(
                    uiState = LoginUiState(isLoading = true),
                    onStudentIdChange = {},
                    onPasswordChange = {},
                    onLoginClick = {}
                )
            }
        }
        composeTestRule.onNodeWithText("로그인 중...").assertIsDisplayed().assertIsNotEnabled()
    }

    @Test
    fun 에러_메시지가_표시되고_로그인_버튼_클릭시_콜백이_호출된다() {
        var clicked = false
        composeTestRule.setContent {
            AttendanceTheme {
                LoginContent(
                    uiState = LoginUiState(errorMessage = "로그인에 실패했습니다."),
                    onStudentIdChange = {},
                    onPasswordChange = {},
                    onLoginClick = { clicked = true }
                )
            }
        }
        composeTestRule.onNodeWithText("로그인에 실패했습니다.").assertIsDisplayed()
        composeTestRule.onNodeWithText("로그인").performClick()
        composeTestRule.runOnIdle { assertThat(clicked).isTrue() }
    }

    @Test
    fun 학번과_비밀번호_입력이_각_콜백에_전달된다() {
        // 실제 화면처럼 입력 결과를 다시 전달하여 포커스 이동 후에도 값을 유지한다.
        val uiState = mutableStateOf(LoginUiState())
        var studentId = ""
        var password = ""
        composeTestRule.setContent {
            AttendanceTheme {
                LoginContent(
                    uiState = uiState.value,
                    onStudentIdChange = {
                        studentId = it
                        uiState.value = uiState.value.copy(studentId = it)
                    },
                    onPasswordChange = {
                        password = it
                        uiState.value = uiState.value.copy(password = it)
                    },
                    onLoginClick = {}
                )
            }
        }
        composeTestRule.onNodeWithText("학번").performTextInput("2021000000")
        composeTestRule.onNodeWithText("비밀번호").performTextInput("pw1234")
        composeTestRule.onNodeWithText("2021000000").assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertThat(studentId).isEqualTo("2021000000")
            assertThat(password).isEqualTo("pw1234")
        }
    }
}
