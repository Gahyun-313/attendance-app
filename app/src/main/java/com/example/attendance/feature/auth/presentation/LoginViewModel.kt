package com.example.attendance.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.attendance.AttendanceApp
import com.example.attendance.core.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 로그인 화면 UI 상태를 하나로 모은 불변 데이터 클래스
 *
 * 모든 상태 프로퍼티가 val이므로 생성된 객체 내부의 값을 직접 수정할 수 없다.
 * -> 상태 변경시 기존 상태를 copy()한 채 새 객체를 stateFlow에 넣는다
 */
data class LoginUiState(
    val studentId: String = "",         // 학번 입력 값
    val password: String = "",          // 비밀번호 입력 값
    val isLoading: Boolean = false,     // 로그인 요청 진행 중 여부 (버튼 비활성화용)
    val isLoginSuccess: Boolean = false,    // 로그인 성공 여부 - true 시 홈으로 이동
    val errorMessage: String? = null    // 로그인 실패 시 안내 문구 (null이면 에러 없음)
)

/**
 * 로그인 화면 viewModel
 *
 * 입력 값과 로그인 요청 상태를 관리(MVVM + 단방향 데이터 흐름)
 * View -> 이벤트 함수 호출 -> 상태 갱신 -> View는 uiState를 구독해 화면에 표시
 */
class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    // 내부에서만 변경 가능한 상태 / 외부에는 읽기 전용으로 노출
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** 새 학번을 상태에 반영한다. 다른 필드는 copy의 기본값으로 유지한다. */
    fun onStudentIdChange(value: String) {
        _uiState.update { it.copy(studentId = value) }
    }

    /** 비밀번호 입력을 상태에 반영한다. 화면에서의 마스킹은 UI가 담당한다. */
    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    /** 중복 요청을 차단하고, 현재 입력으로 로그인한 결과를 화면 상태에 반영한다. */
    fun login() {
        // 중복 요청 방지
        if (_uiState.value.isLoading) return

        // 코루틴이 예약된 동안 다시 호출되어도 요청이 겹치지 않도록 먼저 갱신한다.
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, isLoginSuccess = false, errorMessage = null) }

        // ViewModel이 제거되면 취소된다. 요청에는 버튼을 누른 시점의 입력을 사용한다.
        viewModelScope.launch {
            authRepository.login(state.studentId, state.password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "로그인에 실패했습니다."
                        )
                    }
                }
        }
    }

    companion object {
        /** Application의 수동 DI 컨테이너에서 Repository를 가져와 ViewModel을 생성한다. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as AttendanceApp
                LoginViewModel(authRepository = app.container.authRepository)
            }
        }
    }
}
