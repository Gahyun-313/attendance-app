package com.example.attendance.feature.attendance.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.attendance.AttendanceApp
import com.example.attendance.core.data.repository.AttendanceRepository
import com.example.attendance.core.model.AttendanceStatus
import com.example.attendance.core.model.ClassSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 출석 체크 화면 UI 상태
 *
 * @property session       출석 체크 대상 세션 (로딩 전 null)
 * @property status        현재 출석 상태 (미출석 → 체크 후 출석/지각)
 * @property isNfcScanning NFC 인증 모달 표시 여부
 * @property result        출석 체크 결과 (null이면 결과 모달 미표시)
 * @property checkedAt     출석 처리 시각 문자열
 */
data class AttendanceUiState(
    val session: ClassSession? = null,
    val status: AttendanceStatus = AttendanceStatus.NOT_YET,
    val isNfcScanning: Boolean = false,
    val result: AttendanceStatus? = null,
    val checkedAt: String = ""
)

/**
 * 출석 체크 화면 ViewModel
 *
 * '출석 시작 → NFC 스캔 → 결과 표시' 플로우의 상태를 관리한다.
 * 실제 NFC 태깅은 Repository에서 시뮬레이션 중이며,
 * 추후 NfcAdapter 콜백을 Repository로 전달하는 구조로 확장하면 된다.
 */
class AttendanceViewModel(
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    // 진행 중인 스캔 작업 — 모달을 닫으면 취소한다
    private var scanJob: Job? = null

    init {
        // 출석 체크 대상(현재 진행 중인 세션) 로드
        viewModelScope.launch {
            val sessions = attendanceRepository.getTodaySessions().first()
            _uiState.update { state ->
                state.copy(session = sessions.find { it.isActive })
            }
        }
    }

    /** '출석 시작' 클릭 — NFC 스캔 모달을 띄우고 태깅을 기다린다 */
    fun startAttendance() {
        val session = _uiState.value.session ?: return
        if (_uiState.value.isNfcScanning) return // 중복 실행 방지

        _uiState.update { it.copy(isNfcScanning = true) }

        scanJob = viewModelScope.launch {
            // NFC 태깅 + 서버 처리 (현재는 Fake 구현이 지연 후 결과 반환)
            // TODO: 실제 NfcAdapter 콜백에서 읽은 태그 UID로 교체 — 지금은 시뮬레이션이라 임시 UID를 사용한다.
            // 어떤 세션인지는 서버가 태그 UID로 역추적하므로 여기서 session.id를 넘기지 않는다.
            val resultStatus = attendanceRepository.checkAttendance(nfcTagUid = "FAKE-NFC-TAG-UID")
            _uiState.update {
                it.copy(
                    isNfcScanning = false,
                    status = resultStatus,
                    result = resultStatus,
                    checkedAt = currentTimestamp()
                )
            }
        }
    }

    /** NFC 모달 닫기 — 진행 중인 스캔 취소 */
    fun cancelScan() {
        scanJob?.cancel()
        _uiState.update { it.copy(isNfcScanning = false) }
    }

    /** 결과 모달 닫기 */
    fun dismissResult() {
        _uiState.update { it.copy(result = null) }
    }

    /** 현재 시각을 "yyyy-MM-dd HH:mm:ss" 형식으로 반환 */
    private fun currentTimestamp(): String =
        java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    companion object {
        /** AppContainer에서 Repository를 꺼내 ViewModel을 생성하는 Factory */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as AttendanceApp
                AttendanceViewModel(attendanceRepository = app.container.attendanceRepository)
            }
        }
    }
}
