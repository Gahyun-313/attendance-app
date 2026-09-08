package com.example.attendance

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * ViewModelScope가 쓰는 Main 디스패처를 테스트용 디스패처로 교체하는 Rule
 * ViewModel 단위 테스트에서 재사용한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    /** Android Main Looper가 없는 JVM에서도 viewModelScope를 실행하도록 교체한다. */
    override fun starting(description: Description?) {
        Dispatchers.setMain(testDispatcher)
    }

    /** 전역 Main 설정이 다음 테스트에 남지 않도록 테스트 종료 시 복구한다. */
    override fun finished(description: Description?) {
        Dispatchers.resetMain()
    }
}
