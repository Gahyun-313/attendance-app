package com.example.attendance.feature.auth.presentation

import com.example.attendance.MainDispatcherRule
import com.example.attendance.core.data.repository.AuthRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/** Fake의 상태 테스트와 별도로 Repository에 전달한 인자와 호출 횟수를 검증한다. */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelMockkTest {
    // 즉시 실행하지 않는 디스패처로 코루틴 시작 전 연속 클릭도 재현한다.
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun login_호출시_AuthRepository_login이_입력값_그대로_정확히_1번_호출된다() = runTest {
        val authRepository = mockk<AuthRepository>()
        coEvery { authRepository.login(any(), any()) } returns Result.success(Unit)
        val viewModel = LoginViewModel(authRepository)
        viewModel.onStudentIdChange("2021000000")
        viewModel.onPasswordChange("pw1234")
        viewModel.login()
        advanceUntilIdle()
        coVerify(exactly = 1) { authRepository.login("2021000000", "pw1234") }
    }

    @Test
    fun 연속_클릭해도_클릭_시점의_입력으로_한번만_요청한다() = runTest {
        val authRepository = mockk<AuthRepository>()
        coEvery { authRepository.login(any(), any()) } coAnswers {
            delay(100)
            Result.success(Unit)
        }
        val viewModel = LoginViewModel(authRepository)
        viewModel.onStudentIdChange("2021000000")
        viewModel.onPasswordChange("pw1234")
        viewModel.login()
        viewModel.login()
        // 대기 중 입력 변경이 이미 시작한 요청의 인증 정보를 바꾸면 안 된다.
        viewModel.onStudentIdChange("2022000000")
        advanceUntilIdle()
        coVerify(exactly = 1) { authRepository.login(any(), any()) }
        coVerify(exactly = 1) { authRepository.login("2021000000", "pw1234") }
    }

    @Test
    fun 실패_메시지가_null이면_기본_문구를_표시한다() = runTest {
        val authRepository = mockk<AuthRepository>()
        coEvery { authRepository.login(any(), any()) } returns Result.failure(Exception())
        val viewModel = LoginViewModel(authRepository)
        viewModel.login()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.errorMessage).isEqualTo("로그인에 실패했습니다.")
    }
}
