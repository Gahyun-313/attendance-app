package com.example.attendance.feature.auth.presentation

import app.cash.turbine.test
import com.example.attendance.MainDispatcherRule
import com.example.attendance.core.data.repository.FakeAuthRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** 실제 네트워크 대신 Fake를 주입하여 입력과 요청 결과에 따른 상태를 검증한다. */
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        // 기본 테스트는 즉시 완료하고, 로딩 테스트에서만 지연을 설정한다.
        authRepository = FakeAuthRepository().apply { networkDelayMs = 0 }
        viewModel = LoginViewModel(authRepository)
    }

    @Test
    fun 학번_비밀번호_입력이_상태에_반영된다() {
        viewModel.onStudentIdChange("2021000000")
        viewModel.onPasswordChange("pw1234")

        assertThat(viewModel.uiState.value.studentId).isEqualTo("2021000000")
        assertThat(viewModel.uiState.value.password).isEqualTo("pw1234")
    }

    @Test
    fun 로그인_성공시_isLoginSuccess가_true가_된다() = runTest {
        viewModel.login()
        assertThat(viewModel.uiState.value.isLoginSuccess).isTrue()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }

    @Test
    fun 로그인_실패시_에러_메시지가_표시된다() = runTest {
        authRepository.shouldFail = true
        viewModel.login()
        assertThat(viewModel.uiState.value.errorMessage)
            .isEqualTo("아이디 또는 비밀번호가 올바르지 않습니다.")
        assertThat(viewModel.uiState.value.isLoginSuccess).isFalse()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun 로그인_중에는_로딩_상태를_거친다() = runTest {
        authRepository.networkDelayMs = 100
        // 요청 전에 구독하여 초기 상태부터 완료까지의 방출 순서를 확인한다.
        viewModel.uiState.test {
            assertThat(awaitItem().isLoading).isFalse()
            viewModel.login()
            assertThat(awaitItem().isLoading).isTrue()
            val completed = awaitItem()
            assertThat(completed.isLoading).isFalse()
            assertThat(completed.isLoginSuccess).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun 실패_후_재시도하면_이전_오류가_지워지고_성공한다() = runTest {
        authRepository.shouldFail = true
        viewModel.login()
        authRepository.shouldFail = false
        authRepository.networkDelayMs = 100
        viewModel.uiState.test {
            assertThat(awaitItem().errorMessage).isNotNull()
            viewModel.login()
            val loading = awaitItem()
            assertThat(loading.isLoading).isTrue()
            assertThat(loading.errorMessage).isNull()
            assertThat(awaitItem().isLoginSuccess).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

}
