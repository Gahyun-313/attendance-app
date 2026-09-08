package com.example.attendance.feature.history.presentation

import androidx.lifecycle.SavedStateHandle
import com.example.attendance.MainDispatcherRule
import com.example.attendance.core.data.repository.AttendanceRepository
import com.example.attendance.core.data.repository.FakeAttendanceRepository
import com.example.attendance.core.model.AttendanceRecord
import com.example.attendance.core.model.SampleData
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CorrectionViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    /** 전달한 Long ID가 실제 조회 호출과 화면 기록에 반영되는지 확인한다. */
    @Test
    fun SavedStateHandle의_recordId로_기록을_조회한다() = runTest {
        val repository = RecordingRepository()
        val viewModel = createViewModel(repository)
        assertThat(viewModel.uiState.value.isLoading).isTrue()
        advanceUntilIdle()
        assertThat(repository.queriedIds).containsExactly(repository.record.id)
        assertThat(viewModel.uiState.value.record).isEqualTo(repository.record)
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    /** 인자가 누락되면 조회와 제출을 모두 차단하고 오류를 표시한다. */
    @Test
    fun 인자가_없으면_조회와_제출을_하지_않는다() = runTest {
        val repository = RecordingRepository()
        val viewModel = CorrectionViewModel(SavedStateHandle(), repository)
        viewModel.onReasonChange("NFC 오류")
        viewModel.submit()
        advanceUntilIdle()
        assertThat(repository.queriedIds).isEmpty()
        assertThat(repository.requests).isEmpty()
        assertThat(viewModel.uiState.value.errorMessage).isNotNull()
    }

    /** 양수 ID라도 Repository에 기록이 없으면 제출을 허용하지 않는다. */
    @Test
    fun 없는_기록이면_오류를_표시하고_제출하지_않는다() = runTest {
        val repository = RecordingRepository()
        val viewModel = CorrectionViewModel(SavedStateHandle(mapOf(CorrectionViewModel.ARG_RECORD_ID to Long.MAX_VALUE)), repository)
        advanceUntilIdle()
        viewModel.onReasonChange("NFC 오류")
        viewModel.submit()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.record).isNull()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.errorMessage).isNotNull()
        assertThat(repository.requests).isEmpty()
    }

    /** 공백 사유를 거절한 뒤 새 입력으로 오류가 해제되고 정확한 인자가 제출되는지 확인한다. */
    @Test
    fun 공백_사유는_차단하고_수정하면_입력값으로_제출한다() = runTest {
        val repository = RecordingRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        viewModel.onReasonChange("   ")
        viewModel.submit()
        assertThat(viewModel.uiState.value.isReasonError).isTrue()
        assertThat(repository.requests).isEmpty()
        viewModel.onReasonChange("NFC 오류")
        viewModel.onDetailChange("태그 인식이 되지 않았습니다.")
        assertThat(viewModel.uiState.value.isReasonError).isFalse()
        viewModel.submit()
        advanceUntilIdle()
        assertThat(repository.requests).containsExactly(Triple(repository.record.id, "NFC 오류", "태그 인식이 되지 않았습니다."))
        assertThat(viewModel.uiState.value.isSubmitted).isTrue()
        assertThat(viewModel.uiState.value.isSubmitting).isFalse()
    }

    /** launch 실행 전 연속 클릭과 성공 후 재제출을 차단하고 클릭 당시 입력값을 유지한다. */
    @Test
    fun 연속_제출과_성공_후_재제출을_막고_클릭_시점_입력을_사용한다() = runTest {
        val repository = RecordingRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        viewModel.onReasonChange("첫 사유")
        viewModel.submit()
        assertThat(viewModel.uiState.value.isSubmitting).isTrue()
        viewModel.submit()
        viewModel.onReasonChange("바뀐 사유")
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()
        assertThat(repository.requests).containsExactly(Triple(repository.record.id, "첫 사유", ""))
    }

    /** 서버가 false를 반환하면 오류를 남기고 다음 제출에서 오류를 지운 뒤 재시도한다. */
    @Test
    fun 제출_실패_후_다시_시도할_수_있다() = runTest {
        val repository = RecordingRepository().apply { succeeds = false }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        viewModel.onReasonChange("NFC 오류")
        viewModel.submit()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isSubmitted).isFalse()
        assertThat(viewModel.uiState.value.isSubmitting).isFalse()
        assertThat(viewModel.uiState.value.errorMessage).isNotNull()
        repository.succeeds = true
        viewModel.submit()
        assertThat(viewModel.uiState.value.errorMessage).isNull()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isSubmitted).isTrue()
        assertThat(repository.requests).hasSize(2)
    }

    /** 조회 예외를 화면 오류로 바꾸고 로딩을 종료하는지 확인한다. */
    @Test
    fun 조회_예외는_오류로_표시한다() = runTest {
        val repository = RecordingRepository().apply { lookupFailure = IllegalStateException("조회 실패") }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.errorMessage).isNotNull()
    }

    /** 제출 예외를 성공으로 처리하지 않고 입력을 보존해 재시도를 허용한다. */
    @Test
    fun 제출_예외는_오류로_표시하고_입력을_보존한다() = runTest {
        val repository = RecordingRepository().apply { submitFailure = IllegalStateException("제출 실패") }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        viewModel.onReasonChange("NFC 오류")
        viewModel.submit()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isSubmitting).isFalse()
        assertThat(viewModel.uiState.value.isSubmitted).isFalse()
        assertThat(viewModel.uiState.value.reason).isEqualTo("NFC 오류")
        assertThat(viewModel.uiState.value.errorMessage).isNotNull()
    }

    /** 코루틴 취소를 일반 실패나 성공으로 바꾸지 않는지 확인한다. */
    @Test
    fun 제출_취소는_실패_메시지로_변환하지_않는다() = runTest {
        val repository = RecordingRepository().apply { submitFailure = CancellationException("취소") }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        viewModel.onReasonChange("NFC 오류")
        viewModel.submit()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isSubmitting).isFalse()
        assertThat(viewModel.uiState.value.isSubmitted).isFalse()
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }

    /** 모든 정상 진입 테스트에서 같은 인자 계약으로 ViewModel을 만든다. */
    private fun createViewModel(repository: RecordingRepository): CorrectionViewModel =
        CorrectionViewModel(
            SavedStateHandle(mapOf(CorrectionViewModel.ARG_RECORD_ID to repository.record.id)),
            repository
        )

    /** 정정 관련 메서드만 제어하고 나머지 Repository 계약은 기존 Fake에 위임한다. */
    private class RecordingRepository : AttendanceRepository by FakeAttendanceRepository() {
        val record = SampleData.historyRecords[1]
        val queriedIds = mutableListOf<Long>()
        val requests = mutableListOf<Triple<Long, String, String>>()
        var succeeds = true
        var lookupFailure: Exception? = null
        var submitFailure: Exception? = null

        /** 조회 호출을 기록한 뒤 예외 또는 ID에 해당하는 기록을 반환한다. */
        override suspend fun getRecord(recordId: Long): AttendanceRecord? {
            queriedIds += recordId
            lookupFailure?.let { throw it }
            return record.takeIf { it.id == recordId }
        }

        /** 요청 인자를 기록하고 가상 시간 지연 후 설정한 응답을 돌려준다. */
        override suspend fun requestCorrection(recordId: Long, reason: String, detail: String): Boolean {
            requests += Triple(recordId, reason, detail)
            delay(500)
            submitFailure?.let { throw it }
            return succeeds
        }
    }
}
