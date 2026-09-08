package com.example.attendance.feature.history.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.attendance.AttendanceApp
import com.example.attendance.core.data.repository.AttendanceRepository
import com.example.attendance.core.model.AttendanceRecord
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 정정 대상, 입력값, 조회·제출 상태를 한 번에 전달하는 화면 상태다. */
data class CorrectionUiState(
    val record: AttendanceRecord? = null,
    val reason: String = "",
    val detail: String = "",
    val isLoading: Boolean = true,
    val isReasonError: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null
)

/**
 * SavedStateHandle의 기록 ID로 대상을 조회하고 입력 검증·제출을 담당한다.
 * Repository는 생성자로 주입해 실제 Navigation 없이도 상태 흐름을 테스트할 수 있다.
 */
class CorrectionViewModel(
    savedStateHandle: SavedStateHandle,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CorrectionUiState())
    val uiState = _uiState.asStateFlow()

    /** 생성 시 ID를 확인하고 대상을 조회한다. 없는 ID와 조회 실패는 화면 오류로 바꾼다. */
    init {
        val recordId = savedStateHandle.get<Long>(ARG_RECORD_ID)
        if (recordId == null || recordId <= 0L) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "정정 대상 기록을 찾을 수 없습니다.") }
        } else {
            viewModelScope.launch {
                try {
                    val record = attendanceRepository.getRecord(recordId)
                    _uiState.update {
                        it.copy(
                            record = record,
                            isLoading = false,
                            errorMessage = if (record == null) "정정 대상 기록을 찾을 수 없습니다." else null
                        )
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "기록을 불러오지 못했습니다. 다시 진입해주세요.") }
                }
            }
        }
    }

    /** 사유 입력을 반영하고 이전 필수 입력 오류를 해제한다. */
    fun onReasonChange(value: String) {
        _uiState.update { it.copy(reason = value, isReasonError = false) }
    }

    /** 선택 입력인 상세 내용을 반영한다. 제출은 클릭 시점의 입력값을 사용한다. */
    fun onDetailChange(value: String) {
        _uiState.update { it.copy(detail = value) }
    }

    /**
     * 제출 가능 상태와 필수 사유를 검사한 후 Repository에 요청한다.
     * 코루틴 시작 전에 제출 상태를 잠가 연속 클릭을 막고, 실패하면 재시도를 허용한다.
     */
    fun submit() {
        val state = _uiState.value
        if (state.isLoading || state.isSubmitting || state.isSubmitted) return
        val record = state.record ?: return
        if (state.reason.isBlank()) {
            _uiState.update { it.copy(isReasonError = true) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null, isReasonError = false) }
        viewModelScope.launch {
            try {
                val success = attendanceRepository.requestCorrection(record.id, state.reason, state.detail)
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        isSubmitted = success,
                        errorMessage = if (success) null else "정정 요청에 실패했습니다. 다시 시도해주세요."
                    )
                }
            } catch (cancelled: CancellationException) {
                _uiState.update { it.copy(isSubmitting = false) }
                throw cancelled
            } catch (_: Exception) {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = "정정 요청에 실패했습니다. 다시 시도해주세요.") }
            }
        }
    }

    companion object {
        // Navigation 연결 단계에서도 이 키와 Long 타입을 사용한다.
        const val ARG_RECORD_ID = "recordId"

        /** CreationExtras에서 저장 상태와 앱 컨테이너를 받아 수동 DI로 생성한다. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as AttendanceApp
                CorrectionViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    attendanceRepository = app.container.attendanceRepository
                )
            }
        }
    }
}
