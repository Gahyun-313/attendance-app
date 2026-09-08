package com.example.attendance.feature.attendance.presentation

import com.example.attendance.MainDispatcherRule
import com.example.attendance.core.data.repository.FakeAttendanceRepository
import com.example.attendance.core.model.AttendanceStatus
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AttendanceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AttendanceViewModel

    @Before
    fun setUp() {
        viewModel = AttendanceViewModel(FakeAttendanceRepository())
    }

    @Test
    fun 모달을_닫으면_진행중이던_스캔이_취소되고_결과가_반영되지_않는다() = runTest {
        viewModel.startAttendance()
        assertThat(viewModel.uiState.value.isNfcScanning).isTrue()

        viewModel.cancelScan()

        assertThat(viewModel.uiState.value.isNfcScanning).isFalse()
        assertThat(viewModel.uiState.value.result).isNull()
    }

    @Test
    fun 스캔이_끝까지_진행되면_결과가_출석으로_반영된다() = runTest {
        viewModel.startAttendance()
        advanceUntilIdle() // FakeAttendanceRepository의 delay(2_000)을 가상 시간으로 흘려보낸다

        assertThat(viewModel.uiState.value.isNfcScanning).isFalse()
        assertThat(viewModel.uiState.value.result).isEqualTo(AttendanceStatus.PRESENT)
    }
}
