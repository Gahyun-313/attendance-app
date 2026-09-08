package com.example.attendance.feature.notification.presentation

import app.cash.turbine.test
import com.example.attendance.MainDispatcherRule
import com.example.attendance.core.data.repository.FakeNotificationRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NotificationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: NotificationViewModel

    @Before
    fun setUp() {
        viewModel = NotificationViewModel(FakeNotificationRepository())
    }

    @Test
    fun 초기_상태는_읽음_안읽음으로_분류되어_있다() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.unread.all { !it.isRead }).isTrue()
            assertThat(state.read.all { it.isRead }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun 모두_읽음_처리하면_unread가_비워진다() = runTest {
        viewModel.uiState.test {
            awaitItem() // 초기 상태 (일부 unread 존재)
            viewModel.markAllAsRead()
            val after = awaitItem()
            assertThat(after.unread).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
