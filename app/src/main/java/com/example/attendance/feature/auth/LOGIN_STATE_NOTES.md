# ViewModel·인증 Repository 줄별 해설

[구조·문법·호출 흐름으로 돌아가기](README.md)

package/import와 기존 설명 주석은 생략하고, 실행 코드는 원본 순서 그대로 유지했다. 닫는 괄호까지 각 줄 위에 설명을 붙였다. 실제 파일의 대체본이 아니라 함께 읽는 학습용 코드다.

## LoginViewModel.kt

원본: `app/src/main/java/com/example/attendance/feature/auth/presentation/LoginViewModel.kt`

```kotlin
// 로그인 화면이 필요로 하는 값들을 하나의 데이터 클래스로 선언한다. copy와 값 비교 기능이 생성된다.
data class LoginUiState(
    // 학번을 빈 문자열로 초기화하는 읽기 전용 상태 프로퍼티다.
    val studentId: String = "",
    // 비밀번호 원문을 보관하는 읽기 전용 상태 프로퍼티다. 초기 입력은 비어 있다.
    val password: String = "",
    // 초기 false인 로딩 플래그다. true이면 버튼 비활성화와 로딩 문구에 사용한다.
    val isLoading: Boolean = false,
    // 초기 false인 성공 플래그다. true이면 Screen에서 성공 콜백을 실행할 수 있다.
    val isLoginSuccess: Boolean = false,
    // null을 허용하는 오류 문자열이다. 초기 null이면 오류를 표시하지 않는다.
    val errorMessage: String? = null
// 앞에서 시작한 인자 목록 또는 주 생성자의 소괄호를 닫는다.
)
// class LoginViewModel를 선언한다. 생성자로 받은 Repository에 의존하고 AndroidX ViewModel을 상속한다.
class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    // 기본 LoginUiState를 초기값으로 갖는 변경 가능한 상태 스트림을 만든다. private으로 변경 권한을 내부에 제한한다.
    private val _uiState = MutableStateFlow(LoginUiState())
    // 같은 상태를 읽기 전용 StateFlow로 공개한다. UI는 이 값을 수집하지만 직접 대입하지 못한다.
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    /**
     * 책임: 학번 상태 변경. 흐름: 새 문자열 수신 → 현재 상태 copy → studentId만 교체 → 구독 UI에 반영. 반환은 Unit이다.
     */
    // onStudentIdChange 메서드를 선언한다. 인자에 따른 처리 또는 검증은 아래 본문에서 수행한다.
    fun onStudentIdChange(value: String) {
        // 현재 상태 it을 복사하여 학번만 교체한다. 나머지 필드는 유지하고 새 상태를 원자적으로 저장한다.
        _uiState.update { it.copy(studentId = value) }
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
    /**
     * 책임: 비밀번호 상태 변경. 흐름: 새 원문 수신 → password만 교체한 copy → 구독 UI에 반영. 마스킹은 UI 책임이다.
     */
    // onPasswordChange 메서드를 선언한다. 인자에 따른 처리 또는 검증은 아래 본문에서 수행한다.
    fun onPasswordChange(value: String) {
        // 현재 상태의 비밀번호만 새 입력으로 바꾼 복사본을 저장한다. 마스킹은 이 로직이 아닌 UI의 책임이다.
        _uiState.update { it.copy(password = value) }
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
    /**
     * 책임: 로그인 계약 또는 구현. ViewModel은 중복 검사 → 입력 스냅샷 → 로딩 → 요청 → 상태 반영, Fake는 지연 → 설정에 따른 Result 반환 순서다.
     */
    // login 메서드를 선언한다. 인자에 따른 처리 또는 검증은 아래 본문에서 수행한다.
    fun login() {
        // 이미 요청 중이면 login 메서드를 즉시 종료한다. 첫 요청이 끝나기 전 추가 요청을 막는다.
        if (_uiState.value.isLoading) return
        // 이 시점의 불변 상태 객체를 지역 변수에 보관한다. 이후 UI 입력이 바뀌어도 요청에는 이 학번과 비밀번호를 사용한다.
        val state = _uiState.value
        // 코루틴이 예약되기 전에 요청 중으로 바꾸고 이전 성공과 오류를 초기화한다. 연속 클릭의 중복 진입을 막는다.
        _uiState.update { it.copy(isLoading = true, isLoginSuccess = false, errorMessage = null) }
        // ViewModel의 수명에 연결된 코루틴을 시작한다. clear되면 취소되며 launch 자체는 Job을 반환한다.
        viewModelScope.launch {
            // 주입받은 인터페이스를 통해 클릭 시점의 인증 정보를 전달한다. 실제 실행 대상은 컨테이너가 제공한 Fake다.
            authRepository.login(state.studentId, state.password)
                // 반환된 Result가 성공인 경우에만 다음 람다를 실행한다. 성공 값 Unit은 사용하지 않는다.
                .onSuccess {
                    // 요청 완료와 성공을 상태에 반영한다. Screen은 이 성공 상태를 보고 이동 콜백을 실행할 수 있다.
                    _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
                // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
                }
                // Result가 실패이면 보관된 예외를 throwable로 받아 처리한다. Repository가 직접 throw한 예외를 잡는 catch는 아니다.
                .onFailure { throwable ->
                    // 현재 상태를 받아 새 상태를 계산하는 원자적 갱신 람다를 연다. 네트워크 부수효과는 이 안에 넣지 않는다.
                    _uiState.update {
                        // 현재 상태의 복사본을 만든다. 아래에서 지정하지 않은 프로퍼티는 기존 값을 유지한다.
                        it.copy(
                            // 요청 진행 표시를 해제한다. Content가 다시 로그인 버튼을 활성화할 수 있다.
                            isLoading = false,
                            // 예외 메시지가 null이면 Elvis 연산자로 기본 안내 문구를 사용한다. 빈 문자열은 null과 다르다.
                            errorMessage = throwable.message ?: "로그인에 실패했습니다."
                        // 앞에서 시작한 인자 목록 또는 주 생성자의 소괄호를 닫는다.
                        )
                    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
                    }
                // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
                }
        // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
        }
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
    // 인스턴스 없이 LoginViewModel.Factory로 접근할 수 있는 동반 객체 영역을 연다.
    companion object {
        // Repository가 필요한 ViewModel의 생성 방법을 AndroidX Factory DSL로 등록한다.
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            // 저장소에 ViewModel이 없어 새로 생성할 때 실행할 초기화 블록이다.
            initializer {
                // CreationExtras에서 Application을 조회하고 AttendanceApp으로 캐스팅한다. Manifest의 Application 등록과 타입이 맞아야 한다.
                val app = this[APPLICATION_KEY] as AttendanceApp
                // 수동 DI 컨테이너의 Repository를 생성자로 주입한다. 이 블록의 마지막 식이 생성할 ViewModel이다.
                LoginViewModel(authRepository = app.container.authRepository)
            // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
            }
        // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
        }
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
// 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
}
```

## AuthRepository.kt

원본: `app/src/main/java/com/example/attendance/core/data/repository/AuthRepository.kt`

```kotlin
// 인증 요청의 계약을 선언한다. 호출자는 구체적인 통신 구현 대신 이 타입에 의존한다.
interface AuthRepository {
    /**
     * 책임: 로그인 계약 또는 구현. ViewModel은 중복 검사 → 입력 스냅샷 → 로딩 → 요청 → 상태 반영, Fake는 지연 → 설정에 따른 Result 반환 순서다.
     */
    // 학번과 비밀번호를 받고 중단 가능하게 실행되는 계약이다. 성공은 Unit, 실패는 예외를 Result에 담는다.
    suspend fun login(studentId: String, password: String): Result<Unit>
    /**
     * 책임: 로그아웃 계약 또는 Fake 구현. Fake는 200ms 지연 후 Unit으로 끝나며 실제 세션 제거는 없다.
     */
    // 중단 가능한 로그아웃 계약이다. 명시적 반환 타입이 없으므로 Unit이다.
    suspend fun logout()
// 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
}
// 인증 인터페이스를 구현하는 가짜 저장소다. 실제 서버를 호출하지 않는다.
class FakeAuthRepository : AuthRepository {
    // 테스트가 변경 가능한 실패 스위치다. 기본 false이면 성공하도록 구성된다.
    var shouldFail: Boolean = false
    // 응답까지 중단할 시간을 밀리초 단위 Long 값으로 보관한다. 기본은 500ms다.
    var networkDelayMs: Long = 500
    /**
     * 책임: 로그인 계약 또는 구현. ViewModel은 중복 검사 → 입력 스냅샷 → 로딩 → 요청 → 상태 반영, Fake는 지연 → 설정에 따른 Result 반환 순서다.
     */
    // login 메서드를 선언한다. 인자에 따른 처리 또는 검증은 아래 본문에서 수행한다.
    override suspend fun login(studentId: String, password: String): Result<Unit> {
        // 설정된 시간 동안 코루틴을 중단한다. Thread.sleep처럼 실행 스레드를 점유하며 기다리지 않는다.
        delay(networkDelayMs)
        // if 식이 만든 Result를 호출자에게 반환한다. 실패 설정이면 첫 번째 분기로 들어간다.
        return if (shouldFail) {
            // 테스트용 예외를 실패 Result에 담는다. 여기서는 예외를 throw하지 않는다.
            Result.failure(Exception("아이디 또는 비밀번호가 올바르지 않습니다."))
        // 앞 조건이 false일 때의 성공 분기를 시작한다.
        } else {
            // 별도 응답 데이터 없이 성공했다는 Unit을 Result로 감싼다.
            Result.success(Unit)
        // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
        }
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
    /**
     * 책임: 로그아웃 계약 또는 Fake 구현. Fake는 200ms 지연 후 Unit으로 끝나며 실제 세션 제거는 없다.
     */
    // logout 메서드를 선언한다. 인자에 따른 처리 또는 검증은 아래 본문에서 수행한다.
    override suspend fun logout() {
        // 로그아웃에 걸리는 시간만 흉내 낸다. 현재 Fake에는 삭제할 세션이 없다.
        delay(200)
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
// 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
}
```
