# 홈 단위 테스트와 UI 테스트 줄별 해설

[홈 구조·문법·호출 흐름으로 돌아가기](README.md)

package/import와 기존 주석은 생략했다. 아래 코드의 각 실행 줄에 상세 설명을 붙였으며, 동작은 실제 소스와 같다. 테스트 코드는 작성 상태이며 이번 작업에서는 실행하지 않았다.

## HomeViewModelTest.kt

원본: `app/src/test/java/com/example/attendance/feature/home/presentation/HomeViewModelTest.kt`

```kotlin
// 이 테스트에서 실험적 코루틴 테스트 API를 사용한다는 것을 명시한다.
@OptIn(ExperimentalCoroutinesApi::class)
// class HomeViewModelTest를 선언하고 관련 상태와 메서드를 묶는다.
class HomeViewModelTest {
    // 프로퍼티 getter에 JUnit Rule을 붙여 테스트 시작·종료 때 Main 디스패처 교체를 수행하게 한다.
    @get:Rule
    // 작업을 즉시 실행하지 않고 예약하는 테스트 디스패처로 Main을 교체한다. 초기 상태와 수집 결과를 분리해 관찰한다.
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())
    // 각 테스트의 setUp에서 초기화할 attendanceRepository: MutableAttendanceRepository 프로퍼티다. private으로 외부 접근을 막으며 초기화 전 사용하면 오류가 난다.
    private lateinit var attendanceRepository: MutableAttendanceRepository
    // 각 테스트의 setUp에서 초기화할 notificationRepository: FakeNotificationRepository 프로퍼티다. private으로 외부 접근을 막으며 초기화 전 사용하면 오류가 난다.
    private lateinit var notificationRepository: FakeNotificationRepository
    // 각 테스트의 setUp에서 초기화할 viewModel: HomeViewModel 프로퍼티다. private으로 외부 접근을 막으며 초기화 전 사용하면 오류가 난다.
    private lateinit var viewModel: HomeViewModel
    // 각 테스트 시작 전 다음 메서드를 실행한다.
    @Before
    /**
     * 책임: 테스트 간 데이터 격리. 흐름: 새 출석 테스트 Repository → 새 알림 Fake → 새 ViewModel에 생성자 주입. 각 테스트 전에 JUnit이 호출한다.
     */
    // setUp 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
    fun setUp() {
        // 테스트에서 통계·세션·기록 값을 바꿀 수 있는 Repository를 새로 만든다.
        attendanceRepository = MutableAttendanceRepository()
        // 실제 프로젝트의 알림 Fake를 새로 만든다. markAllAsRead의 상태 변경을 그대로 검증한다.
        notificationRepository = FakeNotificationRepository()
        // 두 Repository를 ViewModel 생성자에 직접 주입한다. Factory나 Activity가 필요 없는 단위 테스트다.
        viewModel = HomeViewModel(attendanceRepository, notificationRepository)
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
    // 각 테스트 종료 후 다음 정리 메서드를 실행한다.
    @After
    /**
     * 책임: 테스트 코루틴 정리. 흐름: ViewModel 범위 취소 → 수집·대기 작업 종료. 이후 Rule에서 Main 디스패처를 복구한다.
     */
    // tearDown 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
    fun tearDown() {
        // 테스트가 만든 ViewModel의 공유 수집과 구독 종료 대기 코루틴을 취소한다. 전역 Main 복구 전에 정리한다.
        viewModel.viewModelScope.cancel()
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 구독 전에는 빈 초기 상태를 유지한다. 흐름: 데이터 또는 화면 준비 → 구독·이벤트 실행 → 기대 결과 단언. 반환값보다 상태·표시·콜백 계약을 검증한다.
     */
    // 구독_전에는_빈_초기_상태를_유지한다 함수를 선언한다. runTest의 코루틴 테스트 범위에서 실행하며 테스트 디스패처의 가상 시각을 사용한다.
    fun 구독_전에는_빈_초기_상태를_유지한다() = runTest {
        // 현재 가상 시각에 예약된 코루틴을 실행한다. 실제 시간을 기다리거나 5초 뒤 작업까지 무조건 진행하지 않는다.
        runCurrent()
        // 현재 또는 다음 상태가 기본 HomeUiState와 같은지 검증한다. Repository 결합 결과와 초기값을 구분한다.
        assertThat(viewModel.uiState.value).isEqualTo(HomeUiState())
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 구독하면 초기값 이후 네 Flow를 합친 상태가 전달된다. 흐름: 데이터 또는 화면 준비 → 구독·이벤트 실행 → 기대 결과 단언. 반환값보다 상태·표시·콜백 계약을 검증한다.
     */
    // 구독하면_초기값_이후_네_Flow를_합친_상태가_전달된다 함수를 선언한다. runTest의 코루틴 테스트 범위에서 실행하며 테스트 디스패처의 가상 시각을 사용한다.
    fun 구독하면_초기값_이후_네_Flow를_합친_상태가_전달된다() = runTest {
        // Turbine으로 uiState를 구독한다. 이 구독이 WhileSubscribed의 upstream 수집을 시작하게 한다.
        viewModel.uiState.test {
            // 현재 또는 다음 상태가 기본 HomeUiState와 같은지 검증한다. Repository 결합 결과와 초기값을 구분한다.
            assertThat(awaitItem()).isEqualTo(HomeUiState())
            // 현재 가상 시각에 예약된 코루틴을 실행한다. 실제 시간을 기다리거나 5초 뒤 작업까지 무조건 진행하지 않는다.
            runCurrent()
            // 다음 상태 방출을 기다려 지역 변수에 보관한다. 이 위치에서는 초기 상태 이후의 결합 결과다.
            val state = awaitItem()
            // Truth로 state.weeklyStats 값을 준비한 기대 데이터와 비교한다. 데이터 클래스와 목록은 값 기준으로 비교한다.
            assertThat(state.weeklyStats).isEqualTo(WeeklyStats(3, 10, 55, 2))
            // Truth로 state.todaySessions 값을 준비한 기대 데이터와 비교한다. 데이터 클래스와 목록은 값 기준으로 비교한다.
            assertThat(state.todaySessions).isEqualTo(SampleData.todaySessions)
            // Truth로 state.recentRecords 값을 준비한 기대 데이터와 비교한다. 데이터 클래스와 목록은 값 기준으로 비교한다.
            assertThat(state.recentRecords).isEqualTo(SampleData.recentRecords)
            // 선택된 알림의 ID가 정확히 1, 2이고 순서도 같은지 검증한다. 빈 목록이나 두 건 초과가 통과하지 못한다.
            assertThat(state.unreadNotifications.map { it.id }).containsExactly(1L, 2L).inOrder()
            // Turbine 구독을 취소하고 남은 이벤트는 이 검증 범위에서 제외한다. ViewModel 범위 자체는 tearDown에서 정리한다.
            cancelAndIgnoreRemainingEvents()
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 모두 읽음 처리하면 홈의 알림이 사라지고 다른 데이터는 유지된다. 흐름: 데이터 또는 화면 준비 → 구독·이벤트 실행 → 기대 결과 단언. 반환값보다 상태·표시·콜백 계약을 검증한다.
     */
    // 모두_읽음_처리하면_홈의_알림이_사라지고_다른_데이터는_유지된다 함수를 선언한다. runTest의 코루틴 테스트 범위에서 실행하며 테스트 디스패처의 가상 시각을 사용한다.
    fun 모두_읽음_처리하면_홈의_알림이_사라지고_다른_데이터는_유지된다() = runTest {
        // Turbine으로 uiState를 구독한다. 이 구독이 WhileSubscribed의 upstream 수집을 시작하게 한다.
        viewModel.uiState.test {
            // 다음 상태 방출 하나를 소비한다. 이 위치의 초기값 또는 준비 상태를 넘겨 이후 변경 결과를 검사한다.
            awaitItem()
            // 현재 가상 시각에 예약된 코루틴을 실행한다. 실제 시간을 기다리거나 5초 뒤 작업까지 무조건 진행하지 않는다.
            runCurrent()
            // 변경 전의 결합 상태를 보관한다. 알림 외의 데이터가 유지되는지 비교하는 기준이다.
            val before = awaitItem()
            // Fake의 모든 알림을 읽음 처리한다. 내부 MutableStateFlow 변경으로 combine이 새 상태를 계산할 수 있다.
            notificationRepository.markAllAsRead()
            // 현재 가상 시각에 예약된 코루틴을 실행한다. 실제 시간을 기다리거나 5초 뒤 작업까지 무조건 진행하지 않는다.
            runCurrent()
            // 알림 변경을 반영한 다음 상태를 기다려 보관한다.
            val after = awaitItem()
            // 변경 전 상태에서 알림만 빈 목록으로 바꾼 결과와 비교한다. 다른 데이터가 변하면 실패한다.
            assertThat(after).isEqualTo(before.copy(unreadNotifications = emptyList()))
            // Turbine 구독을 취소하고 남은 이벤트는 이 검증 범위에서 제외한다. ViewModel 범위 자체는 tearDown에서 정리한다.
            cancelAndIgnoreRemainingEvents()
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 통계 세션 기록이 각각 변경되면 최신 상태로 갱신된다. 흐름: 데이터 또는 화면 준비 → 구독·이벤트 실행 → 기대 결과 단언. 반환값보다 상태·표시·콜백 계약을 검증한다.
     */
    // 통계_세션_기록이_각각_변경되면_최신_상태로_갱신된다 함수를 선언한다. runTest의 코루틴 테스트 범위에서 실행하며 테스트 디스패처의 가상 시각을 사용한다.
    fun 통계_세션_기록이_각각_변경되면_최신_상태로_갱신된다() = runTest {
        // Turbine으로 uiState를 구독한다. 이 구독이 WhileSubscribed의 upstream 수집을 시작하게 한다.
        viewModel.uiState.test {
            // 다음 상태 방출 하나를 소비한다. 이 위치의 초기값 또는 준비 상태를 넘겨 이후 변경 결과를 검사한다.
            awaitItem()
            // 현재 가상 시각에 예약된 코루틴을 실행한다. 실제 시간을 기다리거나 5초 뒤 작업까지 무조건 진행하지 않는다.
            runCurrent()
            // 결합된 초기 결과를 가변 지역 변수로 보관한다. 이후 한 필드씩 바뀐 기대 상태를 만든다.
            var expected = awaitItem()
            // 통계 Flow만 새 값으로 갱신한다. 나머지 upstream은 그대로 두고 통계 변경 전파를 검사한다.
            attendanceRepository.stats.value = WeeklyStats(4, 10, 60, 1)
            // 현재 가상 시각에 예약된 코루틴을 실행한다. 실제 시간을 기다리거나 5초 뒤 작업까지 무조건 진행하지 않는다.
            runCurrent()
            // 기대 상태에서 통계만 새 값으로 교체한다. 다른 필드가 뜻하지 않게 바뀌면 아래 동등성 검증이 실패한다.
            expected = expected.copy(weeklyStats = attendanceRepository.stats.value)
            // 다음 상태를 받아 기대 상태와 값 전체를 비교한다. 갱신 대상 외 필드도 함께 검증한다.
            assertThat(awaitItem()).isEqualTo(expected)
            // 세션 Flow를 샘플 앞의 한 건으로 바꾼다. take는 원본 목록을 직접 수정하지 않는다.
            attendanceRepository.sessions.value = SampleData.todaySessions.take(1)
            // 현재 가상 시각에 예약된 코루틴을 실행한다. 실제 시간을 기다리거나 5초 뒤 작업까지 무조건 진행하지 않는다.
            runCurrent()
            // 기대 상태에서 세션 목록만 교체한다.
            expected = expected.copy(todaySessions = attendanceRepository.sessions.value)
            // 다음 상태를 받아 기대 상태와 값 전체를 비교한다. 갱신 대상 외 필드도 함께 검증한다.
            assertThat(awaitItem()).isEqualTo(expected)
            // 최근 기록 Flow를 샘플 앞의 한 건으로 바꾼다.
            attendanceRepository.records.value = SampleData.recentRecords.take(1)
            // 현재 가상 시각에 예약된 코루틴을 실행한다. 실제 시간을 기다리거나 5초 뒤 작업까지 무조건 진행하지 않는다.
            runCurrent()
            // 기대 상태에서 최근 기록 목록만 교체한다.
            expected = expected.copy(recentRecords = attendanceRepository.records.value)
            // 다음 상태를 받아 기대 상태와 값 전체를 비교한다. 갱신 대상 외 필드도 함께 검증한다.
            assertThat(awaitItem()).isEqualTo(expected)
            // Turbine 구독을 취소하고 남은 이벤트는 이 검증 범위에서 제외한다. ViewModel 범위 자체는 tearDown에서 정리한다.
            cancelAndIgnoreRemainingEvents()
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 목록이 비어도 통계를 유지한 빈 목록 상태를 전달한다. 흐름: 데이터 또는 화면 준비 → 구독·이벤트 실행 → 기대 결과 단언. 반환값보다 상태·표시·콜백 계약을 검증한다.
     */
    // 목록이_비어도_통계를_유지한_빈_목록_상태를_전달한다 함수를 선언한다. runTest의 코루틴 테스트 범위에서 실행하며 테스트 디스패처의 가상 시각을 사용한다.
    fun 목록이_비어도_통계를_유지한_빈_목록_상태를_전달한다() = runTest {
        // Turbine으로 uiState를 구독한다. 이 구독이 WhileSubscribed의 upstream 수집을 시작하게 한다.
        viewModel.uiState.test {
            // 다음 상태 방출 하나를 소비한다. 이 위치의 초기값 또는 준비 상태를 넘겨 이후 변경 결과를 검사한다.
            awaitItem()
            // 현재 가상 시각에 예약된 코루틴을 실행한다. 실제 시간을 기다리거나 5초 뒤 작업까지 무조건 진행하지 않는다.
            runCurrent()
            // 다음 상태 방출 하나를 소비한다. 이 위치의 초기값 또는 준비 상태를 넘겨 이후 변경 결과를 검사한다.
            awaitItem()
            // 세션 목록을 비워 데이터가 없는 경우를 재현한다.
            attendanceRepository.sessions.value = emptyList()
            // 최근 기록 목록을 비워 빈 목록 처리를 검증한다.
            attendanceRepository.records.value = emptyList()
            // Fake의 모든 알림을 읽음 처리한다. 내부 MutableStateFlow 변경으로 combine이 새 상태를 계산할 수 있다.
            notificationRepository.markAllAsRead()
            // 현재 가상 시각에 예약된 코루틴을 실행한다. 실제 시간을 기다리거나 5초 뒤 작업까지 무조건 진행하지 않는다.
            runCurrent()
            // Turbine이 받은 가장 최근 상태를 기대 객체와 비교한다. 여러 Flow 변경 사이의 중간 방출 개수는 고정하지 않는다.
            assertThat(expectMostRecentItem()).isEqualTo(
                // 통계는 기존 값이고 나머지 목록은 기본 빈 목록인 기대 상태를 만든다.
                HomeUiState(weeklyStats = WeeklyStats(3, 10, 55, 2))
            // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
            )
            // Turbine 구독을 취소하고 남은 이벤트는 이 검증 범위에서 제외한다. ViewModel 범위 자체는 tearDown에서 정리한다.
            cancelAndIgnoreRemainingEvents()
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
    // 인터페이스 구현을 Fake에 위임하면서 홈의 세 조회만 아래에서 재정의한다. 운영 코드가 아닌 테스트 전용 대체 객체다.
    private class MutableAttendanceRepository : AttendanceRepository by FakeAttendanceRepository() {
        // 변경 가능한 통계 Flow를 샘플 값으로 초기화한다. 테스트가 value를 교체할 수 있다.
        val stats = MutableStateFlow(WeeklyStats(3, 10, 55, 2))
        // 변경 가능한 오늘 세션 목록 Flow를 만든다.
        val sessions = MutableStateFlow(SampleData.todaySessions)
        // 변경 가능한 최근 기록 Flow를 만든다.
        val records = MutableStateFlow(SampleData.recentRecords)
        /**
         * 책임: 테스트용 통계 Flow 제공. 호출되면 stats 객체를 반환하여 ViewModel이 변경을 구독할 수 있게 한다.
         */
        // 통계 조회 계약을 재정의하여 고정 flowOf 대신 테스트의 변경 가능한 Flow를 반환한다.
        override fun getWeeklyStats() = stats
        /**
         * 책임: 테스트용 세션 Flow 제공. 호출되면 sessions 객체를 반환한다. 원본 Fake의 고정 조회를 대체한다.
         */
        // 오늘 세션 조회 계약을 재정의하여 sessions Flow를 반환한다.
        override fun getTodaySessions() = sessions
        /**
         * 책임: 테스트용 기록 Flow 제공. 호출되면 records 객체를 반환한다. 조회 시 새 Flow를 만들지 않는다.
         */
        // 최근 기록 조회 계약을 재정의하여 records Flow를 반환한다.
        override fun getRecentRecords() = records
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
// 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
}
```

## HomeScreenTest.kt

원본: `app/src/androidTest/java/com/example/attendance/feature/home/presentation/HomeScreenTest.kt`

```kotlin
// class HomeScreenTest를 선언하고 관련 상태와 메서드를 묶는다.
class HomeScreenTest {
    // 프로퍼티 getter에 JUnit Rule을 붙여 테스트 시작·종료 때 Main 디스패처 교체를 수행하게 한다.
    @get:Rule
    // Compose UI 구성·동기화·조작·단언을 수행할 JUnit Rule을 만든다.
    val composeTestRule = createComposeRule()
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 이번 주 출석 현황이 표시된다. 흐름: 데이터 또는 화면 준비 → 구독·이벤트 실행 → 기대 결과 단언. 반환값보다 상태·표시·콜백 계약을 검증한다.
     */
    // 이번_주_출석_현황이_표시된다 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
    fun 이번_주_출석_현황이_표시된다() {
        // 통계만 지정한 상태로 홈을 구성한다. 나머지 목록은 기본 빈 목록이다.
        showHome(HomeUiState(weeklyStats = WeeklyStats(3, 10, 55, 2)))
        // 표시 문자열 이번 주 출석 현황를 가진 의미 노드를 찾는다. 실제 표시 여부를 검증한다.
        composeTestRule.onNodeWithText("이번 주 출석 현황").assertIsDisplayed()
        // 표시 문자열 3 / 10를 가진 의미 노드를 찾는다. 실제 표시 여부를 검증한다.
        composeTestRule.onNodeWithText("3 / 10").assertIsDisplayed()
        // 표시 문자열 55%를 가진 의미 노드를 찾는다. 실제 표시 여부를 검증한다.
        composeTestRule.onNodeWithText("55%").assertIsDisplayed()
        // 표시 문자열 2회를 가진 의미 노드를 찾는다. 실제 표시 여부를 검증한다.
        composeTestRule.onNodeWithText("2회").assertIsDisplayed()
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 빈 목록에서도 각 섹션 제목이 표시된다. 흐름: 데이터 또는 화면 준비 → 구독·이벤트 실행 → 기대 결과 단언. 반환값보다 상태·표시·콜백 계약을 검증한다.
     */
    // 빈_목록에서도_각_섹션_제목이_표시된다 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
    fun 빈_목록에서도_각_섹션_제목이_표시된다() {
        // 기본 빈 상태를 주입하여 데이터가 없어도 섹션 UI가 구성되는지 검사한다.
        showHome(HomeUiState())
        // 표시 문자열 0 / 0를 가진 의미 노드를 찾는다. 실제 표시 여부를 검증한다.
        composeTestRule.onNodeWithText("0 / 0").assertIsDisplayed()
        // 표시 문자열 오늘 수업 세션를 가진 의미 노드를 찾는다. 스크롤로 화면 안에 노출한 뒤 실제 표시 여부를 검증한다.
        composeTestRule.onNodeWithText("오늘 수업 세션").performScrollTo().assertIsDisplayed()
        // 표시 문자열 읽지 않은 알림를 가진 의미 노드를 찾는다. 스크롤로 화면 안에 노출한 뒤 실제 표시 여부를 검증한다.
        composeTestRule.onNodeWithText("읽지 않은 알림").performScrollTo().assertIsDisplayed()
        // 표시 문자열 최근 출석 기록를 가진 의미 노드를 찾는다. 스크롤로 화면 안에 노출한 뒤 실제 표시 여부를 검증한다.
        composeTestRule.onNodeWithText("최근 출석 기록").performScrollTo().assertIsDisplayed()
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 진행중 세션의 출석 체크를 누르면 콜백이 호출된다. 흐름: 데이터 또는 화면 준비 → 구독·이벤트 실행 → 기대 결과 단언. 반환값보다 상태·표시·콜백 계약을 검증한다.
     */
    // 진행중_세션의_출석_체크를_누르면_콜백이_호출된다 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
    fun 진행중_세션의_출석_체크를_누르면_콜백이_호출된다() {
        // 출석 체크 콜백이 실행됐는지 기록할 지역 변수를 만든다.
        var clicked = false
        // 아래 상태와 콜백을 테스트용 UI 구성 함수에 전달한다.
        showHome(
            // 활성 세션 한 건을 포함한 샘플 목록을 넣어 출석 체크 버튼을 구성한다.
            state = HomeUiState(todaySessions = SampleData.todaySessions),
            // 출석 체크 콜백이 실행되면 기록 변수를 true로 바꾸도록 설정한다.
            onCheckAttendance = { clicked = true }
        // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
        )
        // 표시 문자열 출석 체크를 가진 의미 노드를 찾는다. 스크롤로 화면 안에 노출한 뒤 클릭해 이벤트를 발생시킨다.
        composeTestRule.onNodeWithText("출석 체크").performScrollTo().performClick()
        // Compose가 유휴 상태일 때 UI 스레드에서 콜백 기록이 true인지 단언한다.
        composeTestRule.runOnIdle { assertThat(clicked).isTrue() }
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 알림과 기록의 더보기는 각각의 콜백을 호출한다. 흐름: 데이터 또는 화면 준비 → 구독·이벤트 실행 → 기대 결과 단언. 반환값보다 상태·표시·콜백 계약을 검증한다.
     */
    // 알림과_기록의_더보기는_각각의_콜백을_호출한다 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
    fun 알림과_기록의_더보기는_각각의_콜백을_호출한다() {
        // 알림 더보기 콜백의 실행 여부를 기록한다.
        var notificationsClicked = false
        // 기록 더보기 콜백의 실행 여부를 별도로 기록한다.
        var recordsClicked = false
        // 아래 상태와 콜백을 테스트용 UI 구성 함수에 전달한다.
        showHome(
            // 목록이 없어도 두 섹션 제목과 더보기 링크가 존재하는 기본 UI를 구성한다.
            state = HomeUiState(),
            // 알림 더보기만 눌렀을 때 이 변수가 true가 되도록 콜백을 주입한다.
            onMoreNotifications = { notificationsClicked = true },
            // 기록 더보기의 콜백을 별도 변수에 연결한다. 두 이벤트가 뒤바뀌면 테스트가 실패한다.
            onMoreRecords = { recordsClicked = true }
        // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
        )
        // 같은 문구의 두 링크를 의미 트리에서 찾는다. 현재 UI 구성 순서는 알림 다음 기록이다.
        val moreLinks = composeTestRule.onAllNodesWithText("더보기")
        // 링크가 정확히 두 개인지 검사해 잘못된 UI 구성이나 선택 대상을 먼저 확인한다.
        moreLinks.assertCountEquals(2)
        // 첫 번째인 알림 더보기를 화면 안으로 스크롤한 후 클릭한다.
        moreLinks[0].performScrollTo().performClick()
        // Compose 작업이 안정된 시점에 UI 스레드에서 콜백 기록을 검증한다.
        composeTestRule.runOnIdle {
            // 콜백 기록 값이 true인지 확인한다. 두 더보기 이벤트의 연결이 바뀌거나 호출이 누락되면 실패한다.
            assertThat(notificationsClicked).isTrue()
            // 콜백 기록 값이 false인지 확인한다. 두 더보기 이벤트의 연결이 바뀌거나 호출이 누락되면 실패한다.
            assertThat(recordsClicked).isFalse()
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
        // 두 번째인 기록 더보기를 화면 안으로 스크롤한 후 클릭한다.
        moreLinks[1].performScrollTo().performClick()
        // Compose가 유휴 상태일 때 UI 스레드에서 콜백 기록이 true인지 단언한다.
        composeTestRule.runOnIdle { assertThat(recordsClicked).isTrue() }
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
    /**
     * 책임: 테스트 UI 구성 중복 제거. 흐름: 상태와 선택적 콜백 수신 → setContent → 테마 → HomeContent. 생략된 콜백은 빈 람다로 처리한다.
     */
    // showHome 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
    private fun showHome(
        // 테스트에서 구성할 홈 상태를 필수 인자로 받는다.
        state: HomeUiState,
        // onCheckAttendance는 인자 없이 실행하고 Unit을 반환하는 콜백 타입이다. 생략하면 아무 동작도 하지 않는 빈 람다를 쓴다.
        onCheckAttendance: () -> Unit = {},
        // onMoreNotifications는 인자 없이 실행하고 Unit을 반환하는 콜백 타입이다. 생략하면 아무 동작도 하지 않는 빈 람다를 쓴다.
        onMoreNotifications: () -> Unit = {},
        // onMoreRecords는 인자 없이 실행하고 Unit을 반환하는 콜백 타입이다. 생략하면 아무 동작도 하지 않는 빈 람다를 쓴다.
        onMoreRecords: () -> Unit = {}
    // 인자 선언을 닫고 함수 본문 또는 마지막 람다를 연다.
    ) {
        // 계측 테스트용 화면에 Compose UI를 설정한다. UI 조작은 이 구성 이후 수행한다.
        composeTestRule.setContent {
            // 앱의 Compose 테마 아래에서 UI를 구성하여 프로젝트 스타일을 적용한다.
            AttendanceTheme {
                // 상태와 콜백으로만 그리는 Content를 호출한다. 아래 인자는 데이터와 이벤트 계약이다.
                HomeContent(
                    // 테스트가 준비한 상태를 Content에 직접 전달한다. Repository 수집은 하지 않는다.
                    uiState = state,
                    // 전달받은 onCheckAttendance 함수를 하위 UI 콜백에 연결한다. 지금 실행하는 것이 아니라 클릭 때 실행된다.
                    onCheckAttendance = onCheckAttendance,
                    // 전달받은 onMoreNotifications 함수를 하위 UI 콜백에 연결한다. 지금 실행하는 것이 아니라 클릭 때 실행된다.
                    onMoreNotifications = onMoreNotifications,
                    // 전달받은 onMoreRecords 함수를 하위 UI 콜백에 연결한다. 지금 실행하는 것이 아니라 클릭 때 실행된다.
                    onMoreRecords = onMoreRecords
                // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
                )
            // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
            }
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
// 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
}
```
