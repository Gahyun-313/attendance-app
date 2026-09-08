package com.example.attendance.feature.history.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.attendance.core.model.SampleData
import com.example.attendance.ui.theme.AttendanceTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class CorrectionRequestScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private val loadedState = CorrectionUiState(record = SampleData.historyRecords[1], isLoading = false)

    /** 작은 기기에서도 제출 영역으로 스크롤한 뒤 콜백을 검증한다. */
    @Test
    fun 제출_버튼이_콜백을_호출한다() {
        var submitCount = 0
        showContent(onSubmit = { submitCount++ })
        composeTestRule.onNodeWithText("제출").performScrollTo().performClick()
        composeTestRule.runOnIdle { assertThat(submitCount).isEqualTo(1) }
    }

    /** 제출 진행 중의 문구와 비활성 상태를 함께 검증한다. */
    @Test
    fun 제출_중에는_버튼이_비활성화된다() {
        showContent(state = { loadedState.copy(isSubmitting = true) })
        composeTestRule.onNodeWithText("제출 중...").performScrollTo().assertIsDisplayed().assertIsNotEnabled()
    }

    /** 입력 콜백의 값을 상태로 돌려주어 controlled TextField의 실제 사용 방식을 재현한다. */
    @Test
    fun 사유와_상세_입력이_각_콜백으로_전달된다() {
        var state by mutableStateOf(loadedState)
        showContent(
            state = { state },
            onReasonChange = { state = state.copy(reason = it) },
            onDetailChange = { state = state.copy(detail = it) }
        )
        composeTestRule.onAllNodes(hasSetTextAction())[0].performScrollTo().performTextInput("NFC 오류")
        composeTestRule.onAllNodes(hasSetTextAction())[1].performScrollTo().performTextInput("태그 인식 실패")
        composeTestRule.onAllNodes(hasSetTextAction())[0].assertTextContains("NFC 오류")
        composeTestRule.runOnIdle {
            assertThat(state.reason).isEqualTo("NFC 오류")
            assertThat(state.detail).isEqualTo("태그 인식 실패")
        }
    }

    /** 입력 검증 오류와 서버 실패 안내가 화면에 나타나는지 확인한다. */
    @Test
    fun 사유_오류와_제출_오류가_표시된다() {
        showContent(state = { loadedState.copy(isReasonError = true, errorMessage = "정정 요청에 실패했습니다. 다시 시도해주세요.") })
        composeTestRule.onNodeWithText("정정 사유를 입력해주세요.").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("정정 요청에 실패했습니다. 다시 시도해주세요.").performScrollTo().assertIsDisplayed()
    }

    /** 대상이 없는 상태에서는 제출을 비활성화한다. */
    @Test
    fun 대상이_없으면_제출할_수_없다() {
        showContent(state = { CorrectionUiState(isLoading = false, errorMessage = "정정 대상 기록을 찾을 수 없습니다.") })
        composeTestRule.onNodeWithText("정정 대상 기록을 찾을 수 없습니다.").assertIsDisplayed()
        composeTestRule.onNodeWithText("제출").performScrollTo().assertIsNotEnabled()
    }

    /** 취소는 외부에서 정한 뒤로가기 동작을 실행한다. */
    @Test
    fun 취소_버튼이_뒤로가기_콜백을_호출한다() {
        var backCount = 0
        showContent(onBack = { backCount++ })
        composeTestRule.onNodeWithText("취소").performScrollTo().performClick()
        composeTestRule.runOnIdle { assertThat(backCount).isEqualTo(1) }
    }

    /** 앱 테마와 스낵바 상태를 준비하고 테스트별 상태·콜백을 Content에 전달한다. */
    private fun showContent(
        state: () -> CorrectionUiState = { loadedState },
        onBack: () -> Unit = {},
        onReasonChange: (String) -> Unit = {},
        onDetailChange: (String) -> Unit = {},
        onSubmit: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            AttendanceTheme {
                CorrectionContent(
                    uiState = state(),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = onBack, onReasonChange = onReasonChange,
                    onDetailChange = onDetailChange, onSubmit = onSubmit
                )
            }
        }
    }
}
