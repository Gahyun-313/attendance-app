package com.example.attendance.feature.home.presentation

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.example.attendance.MainDispatcherRule
import com.example.attendance.core.data.repository.AttendanceRepository
import com.example.attendance.core.data.repository.FakeAttendanceRepository
import com.example.attendance.core.data.repository.FakeNotificationRepository
import com.example.attendance.core.model.SampleData
import com.example.attendance.core.model.WeeklyStats
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** 구독 전 초기 상태, 네 Flow의 결합 결과와 이후 데이터 변경을 검증한다. */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private lateinit var attendanceRepository: MutableAttendanceRepository
    private lateinit var notificationRepository: FakeNotificationRepository
    private lateinit var viewModel: HomeViewModel

    /** 테스트마다 독립된 데이터와 ViewModel을 만들어 상태가 공유되지 않게 한다. */
    @Before
    fun setUp() {
        attendanceRepository = MutableAttendanceRepository()
        notificationRepository = FakeNotificationRepository()
        viewModel = HomeViewModel(attendanceRepository, notificationRepository)
    }

    /** WhileSubscribed의 대기 작업까지 정리한 뒤 MainDispatcherRule이 Main을 복구한다. */
    @After
    fun tearDown() {
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun 구독_전에는_빈_초기_상태를_유지한다() = runTest {
        runCurrent()
        assertThat(viewModel.uiState.value).isEqualTo(HomeUiState())
    }

    @Test
    fun 구독하면_초기값_이후_네_Flow를_합친_상태가_전달된다() = runTest {
        viewModel.uiState.test {
            // stateIn의 초기값을 Repository 결과로 오인하지 않도록 먼저 소비한다.
            assertThat(awaitItem()).isEqualTo(HomeUiState())
            runCurrent()
            val state = awaitItem()
            assertThat(state.weeklyStats).isEqualTo(WeeklyStats(3, 10, 55, 2))
            assertThat(state.todaySessions).isEqualTo(SampleData.todaySessions)
            assertThat(state.recentRecords).isEqualTo(SampleData.recentRecords)
            assertThat(state.unreadNotifications.map { it.id }).containsExactly(1L, 2L).inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun 모두_읽음_처리하면_홈의_알림이_사라지고_다른_데이터는_유지된다() = runTest {
        viewModel.uiState.test {
            awaitItem()
            runCurrent()
            val before = awaitItem()
            notificationRepository.markAllAsRead()
            runCurrent()
            val after = awaitItem()
            assertThat(after).isEqualTo(before.copy(unreadNotifications = emptyList()))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun 통계_세션_기록이_각각_변경되면_최신_상태로_갱신된다() = runTest {
        viewModel.uiState.test {
            awaitItem()
            runCurrent()
            var expected = awaitItem()

            attendanceRepository.stats.value = WeeklyStats(4, 10, 60, 1)
            runCurrent()
            expected = expected.copy(weeklyStats = attendanceRepository.stats.value)
            assertThat(awaitItem()).isEqualTo(expected)

            attendanceRepository.sessions.value = SampleData.todaySessions.take(1)
            runCurrent()
            expected = expected.copy(todaySessions = attendanceRepository.sessions.value)
            assertThat(awaitItem()).isEqualTo(expected)

            attendanceRepository.records.value = SampleData.recentRecords.take(1)
            runCurrent()
            expected = expected.copy(recentRecords = attendanceRepository.records.value)
            assertThat(awaitItem()).isEqualTo(expected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun 목록이_비어도_통계를_유지한_빈_목록_상태를_전달한다() = runTest {
        viewModel.uiState.test {
            awaitItem()
            runCurrent()
            awaitItem()
            attendanceRepository.sessions.value = emptyList()
            attendanceRepository.records.value = emptyList()
            notificationRepository.markAllAsRead()
            runCurrent()
            // 동시에 바뀐 Flow의 중간 방출 개수 대신 최종 상태의 계약을 검증한다.
            assertThat(expectMostRecentItem()).isEqualTo(
                HomeUiState(weeklyStats = WeeklyStats(3, 10, 55, 2))
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    /** 앱의 Fake는 홈 출석 데이터를 한 번만 방출하므로 테스트에서 변경 가능한 Flow로 대체한다. */
    private class MutableAttendanceRepository : AttendanceRepository by FakeAttendanceRepository() {
        val stats = MutableStateFlow(WeeklyStats(3, 10, 55, 2))
        val sessions = MutableStateFlow(SampleData.todaySessions)
        val records = MutableStateFlow(SampleData.recentRecords)

        override fun getWeeklyStats() = stats
        override fun getTodaySessions() = sessions
        override fun getRecentRecords() = records
    }
}
