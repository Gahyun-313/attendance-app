# 로그인 테스트 줄별 해설

[구조·문법·호출 흐름으로 돌아가기](README.md)

package/import와 기존 설명 주석은 생략하고, 실행 코드는 원본 순서 그대로 유지했다. 닫는 괄호까지 각 줄 위에 설명을 붙였다. 실제 파일의 대체본이 아니라 함께 읽는 학습용 코드다.

## MainDispatcherRule.kt

원본: `app/src/test/java/com/example/attendance/MainDispatcherRule.kt`

```kotlin
// 이 파일 범위에서 사용하는 실험적 Coroutine 테스트 API에 명시적으로 동의한다.
@OptIn(ExperimentalCoroutinesApi::class)
// class MainDispatcherRule를 선언한다. 아래 프로퍼티와 메서드를 하나의 책임으로 묶는다.
class MainDispatcherRule(
    // 외부에서 디스패처를 바꿀 수 있고 생략하면 Unconfined 테스트 디스패처를 사용한다.
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
// JUnit의 테스트 시작·종료 훅을 제공하는 TestWatcher를 상속한다.
) : TestWatcher() {
    /**
     * 책임: 테스트 시작 환경 준비. JUnit이 호출 → Main 디스패처 교체 → 테스트가 viewModelScope 실행 가능.
     */
    // starting 메서드를 선언한다. 인자에 따른 처리 또는 검증은 아래 본문에서 수행한다.
    override fun starting(description: Description?) {
        // 전역 Main을 테스트 디스패처로 교체한다. JVM에 Android Main Looper가 없어도 ViewModel 코루틴을 실행할 수 있다.
        Dispatchers.setMain(testDispatcher)
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
    /**
     * 책임: 테스트 전역 설정 정리. JUnit이 종료 시 호출 → Main 교체 해제 → 다른 테스트에 영향 방지.
     */
    // finished 메서드를 선언한다. 인자에 따른 처리 또는 검증은 아래 본문에서 수행한다.
    override fun finished(description: Description?) {
        // 테스트 종료 시 Main 교체를 해제하여 다음 테스트로 설정이 새지 않게 한다.
        Dispatchers.resetMain()
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
// 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
}
```

## LoginViewModelTest.kt

원본: `app/src/test/java/com/example/attendance/feature/auth/presentation/LoginViewModelTest.kt`

```kotlin
// class LoginViewModelTest를 선언한다. 아래 프로퍼티와 메서드를 하나의 책임으로 묶는다.
class LoginViewModelTest {
    // 프로퍼티 getter에 JUnit Rule 어노테이션을 붙여 테스트 전후 처리를 등록한다.
    @get:Rule
    // 기본 UnconfinedTestDispatcher를 쓰는 Main 교체 Rule을 준비한다.
    val mainDispatcherRule = MainDispatcherRule()
    // 각 테스트 전에 초기화할 Fake 참조다. lateinit은 null 대신 초기화 책임을 코드에 부여한다.
    private lateinit var authRepository: FakeAuthRepository
    // 각 테스트 전에 새로 만들 ViewModel 참조다. 초기화 전에 읽으면 예외가 발생한다.
    private lateinit var viewModel: LoginViewModel
    // 각 테스트 실행 전에 다음 초기화 메서드를 호출하도록 JUnit에 지정한다.
    @Before
    /**
     * 책임: 테스트 간 상태 격리. 각 테스트 전에 새 Fake 생성 → 지연 제거 → 새 ViewModel에 주입.
     */
    // setUp 메서드를 선언한다. 인자에 따른 처리 또는 검증은 아래 본문에서 수행한다.
    fun setUp() {
        // 새 Fake를 만들고 apply의 수신 객체에 지연 0을 설정한다. apply는 같은 객체를 반환한다.
        authRepository = FakeAuthRepository().apply { networkDelayMs = 0 }
        // 준비한 Fake를 생성자로 직접 주입한다. Android Factory를 거치지 않는 단위 테스트다.
        viewModel = LoginViewModel(authRepository)
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 학번 비밀번호 입력이 상태에 반영된다 조건을 검증한다. 흐름: 테스트 상태/응답 준비 → 이벤트 또는 UI 조작 → 기대 상태·호출·표시 단언. 실패하면 해당 계약이 깨졌음을 알린다.
     */
    // 학번_비밀번호_입력이_상태에_반영된다 메서드를 선언한다. 인자에 따른 처리 또는 검증은 아래 본문에서 수행한다.
    fun 학번_비밀번호_입력이_상태에_반영된다() {
        // 사용자가 학번을 입력한 것처럼 새 전체 문자열을 ViewModel 이벤트에 전달한다.
        viewModel.onStudentIdChange("2021000000")
        // 사용자가 비밀번호를 입력한 것처럼 새 전체 문자열을 ViewModel 이벤트에 전달한다.
        viewModel.onPasswordChange("pw1234")
        // Truth로 viewModel.uiState.value.studentId).isEqualTo("2021000000") 식을 검증한다. isTrue/isFalse는 Boolean, isNull/isNotNull은 null 여부, isEqualTo는 값 일치를 확인한다.
        assertThat(viewModel.uiState.value.studentId).isEqualTo("2021000000")
        // Truth로 viewModel.uiState.value.password).isEqualTo("pw1234") 식을 검증한다. isTrue/isFalse는 Boolean, isNull/isNotNull은 null 여부, isEqualTo는 값 일치를 확인한다.
        assertThat(viewModel.uiState.value.password).isEqualTo("pw1234")
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 로그인 성공시 isLoginSuccess가 true가 된다 조건을 검증한다. 흐름: 테스트 상태/응답 준비 → 이벤트 또는 UI 조작 → 기대 상태·호출·표시 단언. 실패하면 해당 계약이 깨졌음을 알린다.
     */
    // 로그인_성공시_isLoginSuccess가_true가_된다 메서드를 선언한다. runTest는 테스트 코루틴 범위와 가상 시간 실행 환경을 제공한다.
    fun 로그인_성공시_isLoginSuccess가_true가_된다() = runTest {
        // 사용자의 로그인 클릭에 해당하는 이벤트를 ViewModel에 전달한다.
        viewModel.login()
        // Truth로 viewModel.uiState.value.isLoginSuccess).isTrue() 식을 검증한다. isTrue/isFalse는 Boolean, isNull/isNotNull은 null 여부, isEqualTo는 값 일치를 확인한다.
        assertThat(viewModel.uiState.value.isLoginSuccess).isTrue()
        // Truth로 viewModel.uiState.value.isLoading).isFalse() 식을 검증한다. isTrue/isFalse는 Boolean, isNull/isNotNull은 null 여부, isEqualTo는 값 일치를 확인한다.
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        // Truth로 viewModel.uiState.value.errorMessage).isNull() 식을 검증한다. isTrue/isFalse는 Boolean, isNull/isNotNull은 null 여부, isEqualTo는 값 일치를 확인한다.
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 로그인 실패시 에러 메시지가 표시된다 조건을 검증한다. 흐름: 테스트 상태/응답 준비 → 이벤트 또는 UI 조작 → 기대 상태·호출·표시 단언. 실패하면 해당 계약이 깨졌음을 알린다.
     */
    // 로그인_실패시_에러_메시지가_표시된다 메서드를 선언한다. runTest는 테스트 코루틴 범위와 가상 시간 실행 환경을 제공한다.
    fun 로그인_실패시_에러_메시지가_표시된다() = runTest {
        // 다음 로그인 결과가 실패가 되도록 Fake를 설정한다.
        authRepository.shouldFail = true
        // 사용자의 로그인 클릭에 해당하는 이벤트를 ViewModel에 전달한다.
        viewModel.login()
        // Truth로 viewModel.uiState.value.errorMessage) 식을 검증한다. isTrue/isFalse는 Boolean, isNull/isNotNull은 null 여부, isEqualTo는 값 일치를 확인한다.
        assertThat(viewModel.uiState.value.errorMessage)
            // 앞줄에서 선택한 실제 값이 이 기대 문자열과 정확히 같은지 검증한다.
            .isEqualTo("아이디 또는 비밀번호가 올바르지 않습니다.")
        // Truth로 viewModel.uiState.value.isLoginSuccess).isFalse() 식을 검증한다. isTrue/isFalse는 Boolean, isNull/isNotNull은 null 여부, isEqualTo는 값 일치를 확인한다.
        assertThat(viewModel.uiState.value.isLoginSuccess).isFalse()
        // Truth로 viewModel.uiState.value.isLoading).isFalse() 식을 검증한다. isTrue/isFalse는 Boolean, isNull/isNotNull은 null 여부, isEqualTo는 값 일치를 확인한다.
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 로그인 중에는 로딩 상태를 거친다 조건을 검증한다. 흐름: 테스트 상태/응답 준비 → 이벤트 또는 UI 조작 → 기대 상태·호출·표시 단언. 실패하면 해당 계약이 깨졌음을 알린다.
     */
    // 로그인_중에는_로딩_상태를_거친다 메서드를 선언한다. runTest는 테스트 코루틴 범위와 가상 시간 실행 환경을 제공한다.
    fun 로그인_중에는_로딩_상태를_거친다() = runTest {
        // 100ms 가상 지연을 설정해 로딩 상태를 관찰할 구간을 만든다.
        authRepository.networkDelayMs = 100
        // Turbine으로 StateFlow를 구독한다. 요청 전에 구독하여 초기 상태부터 순서대로 검증한다.
        viewModel.uiState.test {
            // Truth로 awaitItem().isLoading).isFalse() 식을 검증한다. awaitItem은 다음 방출을 기다린다. isTrue/isFalse는 Boolean, isNull/isNotNull은 null 여부, isEqualTo는 값 일치를 확인한다.
            assertThat(awaitItem().isLoading).isFalse()
            // 사용자의 로그인 클릭에 해당하는 이벤트를 ViewModel에 전달한다.
            viewModel.login()
            // Truth로 awaitItem().isLoading).isTrue() 식을 검증한다. awaitItem은 다음 방출을 기다린다. isTrue/isFalse는 Boolean, isNull/isNotNull은 null 여부, isEqualTo는 값 일치를 확인한다.
            assertThat(awaitItem().isLoading).isTrue()
            // 다음 상태 방출을 기다려 완료 상태로 보관한다. 기다리는 중 테스트 스케줄러가 코루틴 진행을 돕는다.
            val completed = awaitItem()
            // Truth로 completed.isLoading).isFalse() 식을 검증한다. isTrue/isFalse는 Boolean, isNull/isNotNull은 null 여부, isEqualTo는 값 일치를 확인한다.
            assertThat(completed.isLoading).isFalse()
            // Truth로 completed.isLoginSuccess).isTrue() 식을 검증한다. isTrue/isFalse는 Boolean, isNull/isNotNull은 null 여부, isEqualTo는 값 일치를 확인한다.
            assertThat(completed.isLoginSuccess).isTrue()
            // Turbine 구독을 취소하고 남은 이벤트는 이 검증의 범위에서 제외한다.
            cancelAndIgnoreRemainingEvents()
        // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
        }
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 실패 후 재시도하면 이전 오류가 지워지고 성공한다 조건을 검증한다. 흐름: 테스트 상태/응답 준비 → 이벤트 또는 UI 조작 → 기대 상태·호출·표시 단언. 실패하면 해당 계약이 깨졌음을 알린다.
     */
    // 실패_후_재시도하면_이전_오류가_지워지고_성공한다 메서드를 선언한다. runTest는 테스트 코루틴 범위와 가상 시간 실행 환경을 제공한다.
    fun 실패_후_재시도하면_이전_오류가_지워지고_성공한다() = runTest {
        // 다음 로그인 결과가 실패가 되도록 Fake를 설정한다.
        authRepository.shouldFail = true
        // 사용자의 로그인 클릭에 해당하는 이벤트를 ViewModel에 전달한다.
        viewModel.login()
        // 재시도에서 성공하도록 Fake의 실패 스위치를 끈다.
        authRepository.shouldFail = false
        // 100ms 가상 지연을 설정해 로딩 상태를 관찰할 구간을 만든다.
        authRepository.networkDelayMs = 100
        // Turbine으로 StateFlow를 구독한다. 요청 전에 구독하여 초기 상태부터 순서대로 검증한다.
        viewModel.uiState.test {
            // Truth로 awaitItem().errorMessage).isNotNull() 식을 검증한다. awaitItem은 다음 방출을 기다린다. isTrue/isFalse는 Boolean, isNull/isNotNull은 null 여부, isEqualTo는 값 일치를 확인한다.
            assertThat(awaitItem().errorMessage).isNotNull()
            // 사용자의 로그인 클릭에 해당하는 이벤트를 ViewModel에 전달한다.
            viewModel.login()
            // 다음 방출을 로딩 상태로 받아 여러 프로퍼티를 검사할 수 있도록 보관한다.
            val loading = awaitItem()
            // Truth로 loading.isLoading).isTrue() 식을 검증한다. isTrue/isFalse는 Boolean, isNull/isNotNull은 null 여부, isEqualTo는 값 일치를 확인한다.
            assertThat(loading.isLoading).isTrue()
            // Truth로 loading.errorMessage).isNull() 식을 검증한다. isTrue/isFalse는 Boolean, isNull/isNotNull은 null 여부, isEqualTo는 값 일치를 확인한다.
            assertThat(loading.errorMessage).isNull()
            // Truth로 awaitItem().isLoginSuccess).isTrue() 식을 검증한다. awaitItem은 다음 방출을 기다린다. isTrue/isFalse는 Boolean, isNull/isNotNull은 null 여부, isEqualTo는 값 일치를 확인한다.
            assertThat(awaitItem().isLoginSuccess).isTrue()
            // Turbine 구독을 취소하고 남은 이벤트는 이 검증의 범위에서 제외한다.
            cancelAndIgnoreRemainingEvents()
        // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
        }
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
// 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
}
```

## LoginViewModelMockkTest.kt

원본: `app/src/test/java/com/example/attendance/feature/auth/presentation/LoginViewModelMockkTest.kt`

```kotlin
// 이 파일 범위에서 사용하는 실험적 Coroutine 테스트 API에 명시적으로 동의한다.
@OptIn(ExperimentalCoroutinesApi::class)
// class LoginViewModelMockkTest를 선언한다. 아래 프로퍼티와 메서드를 하나의 책임으로 묶는다.
class LoginViewModelMockkTest {
    // 프로퍼티 getter에 JUnit Rule 어노테이션을 붙여 테스트 전후 처리를 등록한다.
    @get:Rule
    // 실행을 예약하는 StandardTestDispatcher로 Main을 교체한다. 코루틴 시작 전 연속 호출 상황을 만들 수 있다.
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: login 호출시 AuthRepository login이 입력값 그대로 정확히 1번 호출된다 조건을 검증한다. 흐름: 테스트 상태/응답 준비 → 이벤트 또는 UI 조작 → 기대 상태·호출·표시 단언. 실패하면 해당 계약이 깨졌음을 알린다.
     */
    // login_호출시_AuthRepository_login이_입력값_그대로_정확히_1번_호출된다 메서드를 선언한다. runTest는 테스트 코루틴 범위와 가상 시간 실행 환경을 제공한다.
    fun login_호출시_AuthRepository_login이_입력값_그대로_정확히_1번_호출된다() = runTest {
        // 인터페이스의 Mock 객체를 만든다. 호출 기록과 미리 정한 응답을 사용한다.
        val authRepository = mockk<AuthRepository>()
        // 어떤 문자열 인자로 로그인해도 성공 Result를 반환하도록 suspend 함수 응답을 설정한다.
        coEvery { authRepository.login(any(), any()) } returns Result.success(Unit)
        // Mock Repository를 생성자로 넣은 ViewModel을 만든다. 호출을 기록하는 대상을 실제로 주입하는 것이 핵심이다.
        val viewModel = LoginViewModel(authRepository)
        // 사용자가 학번을 입력한 것처럼 새 전체 문자열을 ViewModel 이벤트에 전달한다.
        viewModel.onStudentIdChange("2021000000")
        // 사용자가 비밀번호를 입력한 것처럼 새 전체 문자열을 ViewModel 이벤트에 전달한다.
        viewModel.onPasswordChange("pw1234")
        // 사용자의 로그인 클릭에 해당하는 이벤트를 ViewModel에 전달한다.
        viewModel.login()
        // 공유 테스트 스케줄러의 예약 작업과 가상 시간을 더 이상 작업이 없을 때까지 진행한다.
        advanceUntilIdle()
        // 정확한 학번과 비밀번호 조합으로 suspend login이 한 번 호출됐는지 검증한다.
        coVerify(exactly = 1) { authRepository.login("2021000000", "pw1234") }
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 연속 클릭해도 클릭 시점의 입력으로 한번만 요청한다 조건을 검증한다. 흐름: 테스트 상태/응답 준비 → 이벤트 또는 UI 조작 → 기대 상태·호출·표시 단언. 실패하면 해당 계약이 깨졌음을 알린다.
     */
    // 연속_클릭해도_클릭_시점의_입력으로_한번만_요청한다 메서드를 선언한다. runTest는 테스트 코루틴 범위와 가상 시간 실행 환경을 제공한다.
    fun 연속_클릭해도_클릭_시점의_입력으로_한번만_요청한다() = runTest {
        // 인터페이스의 Mock 객체를 만든다. 호출 기록과 미리 정한 응답을 사용한다.
        val authRepository = mockk<AuthRepository>()
        // 로그인 suspend 호출에 실행할 응답 블록을 등록한다. 지연을 포함하는 모의 응답이다.
        coEvery { authRepository.login(any(), any()) } coAnswers {
            // 모의 응답도 100ms 동안 중단시켜 요청이 진행 중인 상황을 만든다.
            delay(100)
            // 별도 응답 데이터 없이 성공했다는 Unit을 Result로 감싼다.
            Result.success(Unit)
        // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
        }
        // Mock Repository를 생성자로 넣은 ViewModel을 만든다. 호출을 기록하는 대상을 실제로 주입하는 것이 핵심이다.
        val viewModel = LoginViewModel(authRepository)
        // 사용자가 학번을 입력한 것처럼 새 전체 문자열을 ViewModel 이벤트에 전달한다.
        viewModel.onStudentIdChange("2021000000")
        // 사용자가 비밀번호를 입력한 것처럼 새 전체 문자열을 ViewModel 이벤트에 전달한다.
        viewModel.onPasswordChange("pw1234")
        // 사용자의 로그인 클릭에 해당하는 이벤트를 ViewModel에 전달한다.
        viewModel.login()
        // 사용자의 로그인 클릭에 해당하는 이벤트를 ViewModel에 전달한다.
        viewModel.login()
        // 사용자가 학번을 입력한 것처럼 새 전체 문자열을 ViewModel 이벤트에 전달한다.
        viewModel.onStudentIdChange("2022000000")
        // 공유 테스트 스케줄러의 예약 작업과 가상 시간을 더 이상 작업이 없을 때까지 진행한다.
        advanceUntilIdle()
        // 인자와 관계없이 전체 로그인 호출이 한 번뿐인지 검증한다. 다른 인자로 추가 호출된 경우도 잡는다.
        coVerify(exactly = 1) { authRepository.login(any(), any()) }
        // 정확한 학번과 비밀번호 조합으로 suspend login이 한 번 호출됐는지 검증한다.
        coVerify(exactly = 1) { authRepository.login("2021000000", "pw1234") }
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 실패 메시지가 null이면 기본 문구를 표시한다 조건을 검증한다. 흐름: 테스트 상태/응답 준비 → 이벤트 또는 UI 조작 → 기대 상태·호출·표시 단언. 실패하면 해당 계약이 깨졌음을 알린다.
     */
    // 실패_메시지가_null이면_기본_문구를_표시한다 메서드를 선언한다. runTest는 테스트 코루틴 범위와 가상 시간 실행 환경을 제공한다.
    fun 실패_메시지가_null이면_기본_문구를_표시한다() = runTest {
        // 인터페이스의 Mock 객체를 만든다. 호출 기록과 미리 정한 응답을 사용한다.
        val authRepository = mockk<AuthRepository>()
        // 메시지가 null인 예외를 실패 Result로 반환해 기본 안내 문구 분기를 검증한다.
        coEvery { authRepository.login(any(), any()) } returns Result.failure(Exception())
        // Mock Repository를 생성자로 넣은 ViewModel을 만든다. 호출을 기록하는 대상을 실제로 주입하는 것이 핵심이다.
        val viewModel = LoginViewModel(authRepository)
        // 사용자의 로그인 클릭에 해당하는 이벤트를 ViewModel에 전달한다.
        viewModel.login()
        // 공유 테스트 스케줄러의 예약 작업과 가상 시간을 더 이상 작업이 없을 때까지 진행한다.
        advanceUntilIdle()
        // Truth로 viewModel.uiState.value.errorMessage).isEqualTo("로그인에 실패했습니다.") 식을 검증한다. isTrue/isFalse는 Boolean, isNull/isNotNull은 null 여부, isEqualTo는 값 일치를 확인한다.
        assertThat(viewModel.uiState.value.errorMessage).isEqualTo("로그인에 실패했습니다.")
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
// 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
}
```

## LoginScreenTest.kt

원본: `app/src/androidTest/java/com/example/attendance/feature/auth/presentation/LoginScreenTest.kt`

```kotlin
// class LoginScreenTest를 선언한다. 아래 프로퍼티와 메서드를 하나의 책임으로 묶는다.
class LoginScreenTest {
    // 프로퍼티 getter에 JUnit Rule 어노테이션을 붙여 테스트 전후 처리를 등록한다.
    @get:Rule
    // Compose UI를 설정하고 동기화된 UI 조작·검증을 수행할 JUnit Rule을 만든다.
    val composeTestRule = createComposeRule()
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 입력된 학번 값이 화면에 표시된다 조건을 검증한다. 흐름: 테스트 상태/응답 준비 → 이벤트 또는 UI 조작 → 기대 상태·호출·표시 단언. 실패하면 해당 계약이 깨졌음을 알린다.
     */
    // 입력된_학번_값이_화면에_표시된다 메서드를 선언한다. 인자에 따른 처리 또는 검증은 아래 본문에서 수행한다.
    fun 입력된_학번_값이_화면에_표시된다() {
        // 계측 테스트용 Activity에 이 테스트가 검증할 Compose UI를 구성한다.
        composeTestRule.setContent {
            // 앱의 Compose 테마 아래에서 UI를 구성한다. 색상·타이포그래피 환경을 제공한다.
            AttendanceTheme {
                // 순수 표시를 담당하는 Content를 호출한다. 아래 인자로 상태와 이벤트 콜백을 전달한다.
                LoginContent(
                    // 입력값이 이미 채워진 상태를 직접 주입한다. 입력 이벤트를 거치지 않고 렌더링만 검증한다.
                    uiState = LoginUiState(studentId = "2021000000", password = "pw1234"),
                    // 이 시나리오에서는 학번 변경을 처리하지 않는 빈 람다를 전달한다.
                    onStudentIdChange = {},
                    // 이 시나리오에서는 비밀번호 변경을 처리하지 않는 빈 람다를 전달한다.
                    onPasswordChange = {},
                    // 로그인 클릭에 부수효과가 없는 빈 람다를 전달한다.
                    onLoginClick = {}
                // 앞에서 시작한 인자 목록 또는 주 생성자의 소괄호를 닫는다.
                )
            // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
            }
        // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
        }
        // 텍스트 의미 정보를 가진 UI 노드를 찾는다. 화면 표시 여부를 검사한다.
        composeTestRule.onNodeWithText("2021000000").assertIsDisplayed()
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 로딩 중에는 버튼 텍스트가 바뀌고 비활성화된다 조건을 검증한다. 흐름: 테스트 상태/응답 준비 → 이벤트 또는 UI 조작 → 기대 상태·호출·표시 단언. 실패하면 해당 계약이 깨졌음을 알린다.
     */
    // 로딩_중에는_버튼_텍스트가_바뀌고_비활성화된다 메서드를 선언한다. 인자에 따른 처리 또는 검증은 아래 본문에서 수행한다.
    fun 로딩_중에는_버튼_텍스트가_바뀌고_비활성화된다() {
        // 계측 테스트용 Activity에 이 테스트가 검증할 Compose UI를 구성한다.
        composeTestRule.setContent {
            // 앱의 Compose 테마 아래에서 UI를 구성한다. 색상·타이포그래피 환경을 제공한다.
            AttendanceTheme {
                // 순수 표시를 담당하는 Content를 호출한다. 아래 인자로 상태와 이벤트 콜백을 전달한다.
                LoginContent(
                    // 요청 중 상태를 직접 주입한다. 실제 네트워크 지연 없이 로딩 UI를 확인한다.
                    uiState = LoginUiState(isLoading = true),
                    // 이 시나리오에서는 학번 변경을 처리하지 않는 빈 람다를 전달한다.
                    onStudentIdChange = {},
                    // 이 시나리오에서는 비밀번호 변경을 처리하지 않는 빈 람다를 전달한다.
                    onPasswordChange = {},
                    // 로그인 클릭에 부수효과가 없는 빈 람다를 전달한다.
                    onLoginClick = {}
                // 앞에서 시작한 인자 목록 또는 주 생성자의 소괄호를 닫는다.
                )
            // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
            }
        // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
        }
        // 텍스트 의미 정보를 가진 UI 노드를 찾는다. 화면 표시 여부를 검사한다. 추가로 비활성 상태도 검사한다.
        composeTestRule.onNodeWithText("로그인 중...").assertIsDisplayed().assertIsNotEnabled()
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 에러 메시지가 표시되고 로그인 버튼 클릭시 콜백이 호출된다 조건을 검증한다. 흐름: 테스트 상태/응답 준비 → 이벤트 또는 UI 조작 → 기대 상태·호출·표시 단언. 실패하면 해당 계약이 깨졌음을 알린다.
     */
    // 에러_메시지가_표시되고_로그인_버튼_클릭시_콜백이_호출된다 메서드를 선언한다. 인자에 따른 처리 또는 검증은 아래 본문에서 수행한다.
    fun 에러_메시지가_표시되고_로그인_버튼_클릭시_콜백이_호출된다() {
        // 버튼 콜백이 실행됐는지 기록할 테스트 지역 변수다.
        var clicked = false
        // 계측 테스트용 Activity에 이 테스트가 검증할 Compose UI를 구성한다.
        composeTestRule.setContent {
            // 앱의 Compose 테마 아래에서 UI를 구성한다. 색상·타이포그래피 환경을 제공한다.
            AttendanceTheme {
                // 순수 표시를 담당하는 Content를 호출한다. 아래 인자로 상태와 이벤트 콜백을 전달한다.
                LoginContent(
                    // 실패 메시지가 있는 상태를 직접 주입한다. Content의 오류 표시 조건을 검증한다.
                    uiState = LoginUiState(errorMessage = "로그인에 실패했습니다."),
                    // 이 시나리오에서는 학번 변경을 처리하지 않는 빈 람다를 전달한다.
                    onStudentIdChange = {},
                    // 이 시나리오에서는 비밀번호 변경을 처리하지 않는 빈 람다를 전달한다.
                    onPasswordChange = {},
                    // 로그인 이벤트가 오면 기록 변수를 true로 바꾸는 람다를 주입한다.
                    onLoginClick = { clicked = true }
                // 앞에서 시작한 인자 목록 또는 주 생성자의 소괄호를 닫는다.
                )
            // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
            }
        // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
        }
        // 텍스트 의미 정보를 가진 UI 노드를 찾는다. 화면 표시 여부를 검사한다.
        composeTestRule.onNodeWithText("로그인에 실패했습니다.").assertIsDisplayed()
        // 텍스트 의미 정보를 가진 UI 노드를 찾는다. 클릭을 수행해 연결된 콜백을 실행한다.
        composeTestRule.onNodeWithText("로그인").performClick()
        // UI가 유휴 상태일 때 UI 스레드에서 콜백 실행 기록을 검사한다.
        composeTestRule.runOnIdle { assertThat(clicked).isTrue() }
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
    // 다음 메서드를 독립적인 JUnit 테스트로 등록한다.
    @Test
    /**
     * 책임: 두 입력의 콜백 전달과 포커스 이동 후 학번 유지를 검증한다.
     * 흐름: 관찰 가능한 상태 준비 → 입력 콜백 기록 및 상태 갱신 → 재구성 → 다음 입력 → 표시와 콜백 값 검증.
     * ViewModel 대신 테스트가 상태 소유자가 되어 Content에 최신 값을 다시 전달한다.
     */
    // 학번과_비밀번호_입력이_각_콜백에_전달된다 메서드를 선언한다. 인자에 따른 처리 또는 검증은 아래 본문에서 수행한다.
    fun 학번과_비밀번호_입력이_각_콜백에_전달된다() {
        // Compose가 변경을 관찰할 상태를 테스트 범위에 만든다. val은 상태 객체의 참조를 고정하며 value는 갱신할 수 있다.
        val uiState = mutableStateOf(LoginUiState())
        // 학번 변경 콜백으로 받은 문자열을 기록할 변수를 준비한다.
        var studentId = ""
        // 비밀번호 변경 콜백으로 받은 문자열을 기록할 변수를 준비한다.
        var password = ""
        // 계측 테스트용 Activity에 이 테스트가 검증할 Compose UI를 구성한다.
        composeTestRule.setContent {
            // 앱의 Compose 테마 아래에서 UI를 구성한다. 색상·타이포그래피 환경을 제공한다.
            AttendanceTheme {
                // 순수 표시를 담당하는 Content를 호출한다. 아래 인자로 상태와 이벤트 콜백을 전달한다.
                LoginContent(
                    // 관찰 가능한 상태의 최신 값을 읽어 전달한다. value 변경 시 이를 읽은 UI가 재구성된다.
                    uiState = uiState.value,
                    // 학번 입력 콜백을 연다. it은 새 전체 학번 문자열이다.
                    onStudentIdChange = {
                        // 콜백이 실제로 전달한 값을 별도로 기록하여 마지막에 검증한다.
                        studentId = it
                        // 비밀번호 등 다른 필드는 유지하면서 학번을 교체한다. 새 상태를 UI에 돌려주는 상태 끌어올리기 흐름이다.
                        uiState.value = uiState.value.copy(studentId = it)
                    // 학번 콜백을 닫고 다음 인자로 구분한다.
                    },
                    // 비밀번호 입력 콜백을 연다. it은 화면에서 가려지기 전 원문이다.
                    onPasswordChange = {
                        // 비밀번호 콜백이 받은 값을 별도로 기록한다.
                        password = it
                        // 학번을 유지한 복사본으로 비밀번호를 갱신하여 Content에 최신 입력값을 다시 전달한다.
                        uiState.value = uiState.value.copy(password = it)
                    // 비밀번호 콜백을 닫고 다음 인자로 구분한다.
                    },
                    // 로그인 클릭에 부수효과가 없는 빈 람다를 전달한다.
                    onLoginClick = {}
                // 앞에서 시작한 인자 목록 또는 주 생성자의 소괄호를 닫는다.
                )
            // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
            }
        // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
        }
        // 텍스트 의미 정보를 가진 UI 노드를 찾는다. 입력을 수행하여 onValueChange 콜백을 발생시킨다.
        composeTestRule.onNodeWithText("학번").performTextInput("2021000000")
        // 텍스트 의미 정보를 가진 UI 노드를 찾는다. 입력을 수행하여 onValueChange 콜백을 발생시킨다.
        composeTestRule.onNodeWithText("비밀번호").performTextInput("pw1234")
        // 비밀번호 입력창으로 포커스를 옮긴 뒤에도 학번이 화면에 남아 있는지 확인한다.
        composeTestRule.onNodeWithText("2021000000").assertIsDisplayed()
        // Compose 작업이 안정된 시점의 UI 스레드에서 다음 검증을 실행한다.
        composeTestRule.runOnIdle {
            // Truth로 studentId).isEqualTo("2021000000") 식을 검증한다. isTrue/isFalse는 Boolean, isNull/isNotNull은 null 여부, isEqualTo는 값 일치를 확인한다.
            assertThat(studentId).isEqualTo("2021000000")
            // Truth로 password).isEqualTo("pw1234") 식을 검증한다. isTrue/isFalse는 Boolean, isNull/isNotNull은 null 여부, isEqualTo는 값 일치를 확인한다.
            assertThat(password).isEqualTo("pw1234")
        // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
        }
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
// 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
}
```
