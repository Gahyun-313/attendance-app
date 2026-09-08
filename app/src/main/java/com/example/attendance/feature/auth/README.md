# 로그인 기능 학습 노트 — STEP 3-2

이 문서는 `attendance-app`의 `feat/screen-login` 구현을 설명한다. 실제 소스에는 유지보수에 필요한 주석을 두고, 아래 연결된 문서에는 import를 제외한 코드의 모든 줄에 설명 주석을 붙였다. 설명 코드는 실제 코드와 동일한 순서·동작이며 별도 구현이 아니다.

## 읽는 순서

1. 이 문서에서 구조와 실행 흐름을 이해한다.
2. [ViewModel과 Repository 줄별 설명](LOGIN_STATE_NOTES.md)을 읽는다.
3. [Screen과 Content 줄별 설명](LOGIN_UI_NOTES.md)을 읽는다.
4. [단위·UI 테스트 줄별 설명](LOGIN_TEST_NOTES.md)을 읽고 검증 항목을 비교한다.

## 현재 구현의 경계

현재는 **수동 DI + Fake Repository** 단계다. `attendance-app-claude`의 이후 단계에 있는 Hilt, `AppResult`, `SessionRepository`를 이 코드의 현재 구조로 혼동하면 안 된다.

`MainActivity`는 아직 `HomePrototypeScreen()`을 표시한다. `LoginScreen`은 구현되어 있지만 앱 진입 경로에 연결되지 않았다. 성공 시 `onLoginSuccess()`를 호출할 뿐, 여기에는 NavController나 홈으로 이동하는 코드가 없다. 스플래시·로그인·홈 연결은 이후 Navigation 단계의 책임이다. Preview와 Content UI 테스트로 로그인 UI를 독립적으로 확인할 수 있다.

실제 서버 인증, 입력값 유효성 검사, 토큰 발급·저장, 자동 로그인은 구현하지 않았다. Fake는 입력에 관계없이 `shouldFail`에 따라 결과를 반환한다. Retrofit 의존성이 있다고 서버 연동이 완료된 것은 아니다.

## 파일의 책임과 관계

| 파일 | 책임 | 직접 사용하는 대상 |
| --- | --- | --- |
| `presentation/LoginScreen.kt`의 Screen | ViewModel 확보, 상태 수집, 성공 콜백 | Factory, StateFlow, Content |
| 같은 파일의 Content | UI 표시와 입력 전달 | AppTextField, LoginButton, 테마·이미지 리소스 |
| `presentation/LoginViewModel.kt` | 입력·로딩·결과 상태 관리 | AuthRepository 인터페이스 |
| `core/data/repository/AuthRepository.kt` | 로그인 계약과 Fake 동작 | Coroutine delay, Kotlin Result |
| `AttendanceApp.kt` | 앱 생명주기에 맞춘 DI 컨테이너 준비 | DefaultAppContainer |
| `core/di/AppContainer.kt` | Repository 구현체 제공 | FakeAuthRepository |
| `MainDispatcherRule.kt` | JVM 테스트용 Main 교체와 복구 | TestDispatcher, JUnit Rule |
| `LoginViewModelTest.kt` | 입력·결과·로딩 상태 검증 | Fake, Truth, Turbine |
| `LoginViewModelMockkTest.kt` | 인자·횟수·연속 클릭 검증 | MockK, StandardTestDispatcher |
| `LoginScreenTest.kt` | 렌더링·활성화·입력 콜백 검증 | Compose Test Rule, Content |

테스트 파일은 각각 `app/src/test/java/com/example/attendance/`와 `app/src/androidTest/java/com/example/attendance/` 아래에 있다. 운영 코드의 `feature/auth`가 테스트 코드를 호출하는 것은 아니다.

## 생성 흐름과 호출 흐름

```text
AndroidManifest.xml의 android:name=".AttendanceApp"
  → Android가 AttendanceApp 생성 및 onCreate 호출
  → DefaultAppContainer 생성
  → LoginScreen을 호출하는 화면이 생기면 viewModel(factory = ...) 실행
  → 해당 ViewModelStore에 인스턴스가 없을 때 Factory.initializer 실행
  → CreationExtras[APPLICATION_KEY]에서 AttendanceApp 조회
  → app.container.authRepository 최초 접근
  → lazy 블록에서 FakeAuthRepository 생성
  → LoginViewModel(authRepository) 생성
```

`viewModel()`은 매 재구성마다 새 ViewModel을 만드는 호출이 아니다. 현재 ViewModelStoreOwner의 저장소에서 기존 인스턴스를 재사용한다. Factory는 생성 방법을 제공하고, 저장·재사용·제거는 ViewModel 관련 프레임워크가 담당한다.

```text
사용자 입력
  → AppTextField.onValueChange(새 문자열)
  → LoginContent에 전달된 콜백
  → LoginViewModel.onStudentIdChange / onPasswordChange
  → _uiState.update { it.copy(...) }
  → StateFlow의 상태 변경
  → LoginScreen의 collectAsStateWithLifecycle
  → 새 uiState로 LoginContent 재구성

로그인 클릭
  → LoginButton.onClick → LoginViewModel.login()
  → isLoading 검사: 요청 중이면 return
  → 클릭 시점 상태를 지역 변수에 보관
  → 로딩 true, 이전 성공 false, 오류 null
  → viewModelScope.launch
  → AuthRepository.login(클릭 시점 학번, 비밀번호)
      → 실제 주입된 FakeAuthRepository.login
      → delay(networkDelayMs)
      → shouldFail에 따라 Result 성공 또는 실패
  → 성공: 로딩 false, 성공 true
      → Screen의 LaunchedEffect → 호출자가 제공한 onLoginSuccess()
  → 실패: 로딩 false, errorMessage 설정
      → Content의 조건부 Text 표시
```

화살표 중 Repository 호출은 직접 함수 호출이고, ViewModel에서 Screen으로 가는 경로는 구독한 상태의 전달이다. ViewModel이 Screen 함수를 직접 호출하지 않는다.

## 아키텍처를 설명하는 방법

**MVVM**: View는 Screen/Content, ViewModel은 로그인 화면 상태와 이벤트 처리, 데이터 측 계약은 AuthRepository다. 별도 UseCase 계층은 없다. 계층 수보다 UI가 인증 구현체를 직접 생성하지 않는 경계가 핵심이다.

**단방향 데이터 흐름(UDF)**: 상태는 ViewModel에서 UI로 내려가고, 사용자 이벤트는 콜백으로 ViewModel에 올라간다. Content가 `uiState.studentId`를 직접 수정하지 않는다. 값을 표시하는 `value`와 변경을 전달하는 `onValueChange`를 분리한 것이 상태 끌어올리기(state hoisting)다.

**의존성 주입(DI)**: ViewModel은 생성자로 AuthRepository를 받는다. 이 코드에서 DI는 Hilt라는 라이브러리가 아니라 외부에서 의존 객체를 전달하는 설계다. 앱에서는 Factory와 컨테이너가, 테스트에서는 테스트 함수가 의존성을 제공한다. Fake를 실제 구현으로 교체해도 같은 계약이면 ViewModel의 호출 코드는 유지된다.

**Screen/Content 분리**: Screen은 환경 의존적인 ViewModel과 생명주기를 다룬다. Content는 상태와 콜백만 받으므로 Preview에서 초기 상태를 만들거나 테스트에서 실패 상태를 바로 넣을 수 있다. Stateless는 상태를 전혀 읽지 않는다는 뜻이 아니라 상태를 직접 소유·변경하지 않는다는 뜻이다.

## Kotlin·Compose·Coroutine 핵심 문법

| 문법/API | 코드에서의 의미와 주의점 |
| --- | --- |
| `data class`, `val`, `copy` | 불변 상태 객체를 새 복사본으로 교체한다. val은 참조 재할당을 막으며 일반적으로 깊은 불변성을 보장하지는 않는다. 여기 필드는 String/Boolean이라 직접 변경할 내부 컬렉션이 없다. |
| `String?`, `?:` | 오류가 없으면 null. 예외 메시지가 null일 때 기본 문구를 선택한다. 빈 문자열에는 기본 문구가 적용되지 않는다. |
| `private`, `_uiState`, `asStateFlow()` | 변경 권한은 ViewModel 내부에 두고 외부에는 읽기 전용 타입으로 노출한다. 밑줄은 관례다. |
| `update { it.copy(...) }` | it은 현재 상태다. 원자적인 갱신이며 경합 시 람다가 재평가될 수 있어 내부에 네트워크 호출 같은 부수효과를 넣지 않는다. |
| `StateFlow<T>` | 초기값과 최신값 하나가 있는 hot stream. equals 기준 같은 값은 방출하지 않으며 느린 구독자는 중간 상태를 건너뛸 수 있다. 모든 이벤트를 저장하는 큐가 아니다. |
| `by` | Compose State의 값을 위임으로 읽는다. 이 상태를 읽은 컴포지션은 변경 시 재구성 대상이 된다. |
| `collectAsStateWithLifecycle()` | 기본 STARTED 이상에서 수집한다. 수집 중지는 ViewModel 코루틴 취소나 상태 삭제와 다르다. |
| `@Composable` | Compose UI를 구성할 수 있는 함수 표시다. 재구성 중 반복 실행될 수 있어 본문에서 곧바로 네트워크 요청을 하면 안 된다. |
| `LaunchedEffect(key)` | 컴포지션 진입 시 실행하고 key가 바뀌면 기존 작업 취소 후 재시작한다. 같은 key의 일반 재구성에서는 재시작하지 않는다. 앱 전체에서 정확히 한 번이라는 보장은 없다. |
| `(String) -> Unit`, `() -> Unit`, `::` | 문자열을 받는 콜백, 인자 없는 콜백, 함수 참조다. `viewModel::login`은 즉시 실행이 아니라 나중에 실행할 함수를 전달한다. |
| `suspend`, `launch`, `delay` | suspend는 중단 가능한 함수이며 자동 백그라운드 실행을 뜻하지 않는다. launch는 Job을 반환한다. delay는 스레드를 막지 않고 중단한다. |
| `viewModelScope` | ViewModel이 clear될 때 작업을 취소한다. 화면이 잠시 안 보인다고 즉시 취소되는 것은 아니다. |
| `Result<Unit>` | 성공 시 별도 데이터 없이 완료했다는 Unit, 실패 시 Throwable을 담는다. `onFailure`는 반환된 실패 Result를 처리하며 직접 throw된 예외를 자동으로 잡지 않는다. |
| `companion object`, `as`, DSL | 클래스 이름으로 Factory에 접근한다. Application을 AttendanceApp으로 캐스팅하고 initializer의 마지막 식을 생성 결과로 사용한다. |
| `Modifier`, `dp`, `sp` | Modifier 체인은 순서대로 크기·여백을 적용한다. dp는 레이아웃, sp는 사용자 글꼴 배율을 반영하는 글자 크기다. |
| `PasswordVisualTransformation` | 화면 표시만 가린다. ViewModel의 원문을 암호화하지 않는다. KeyboardType.Number 역시 입력값 검증을 대신하지 않는다. |
| `@get:Rule`, `lateinit`, `apply` | Rule을 getter에 붙인다. lateinit은 나중에 초기화할 프로퍼티이며 사용 전 초기화가 필요하다. apply의 수신 객체는 Fake이고 설정 후 같은 객체를 돌려준다. |

## 가이드에서 보완한 부분

가이드처럼 코루틴 안에서 로딩을 설정하면, 실행이 예약된 동안 연속 호출될 때 두 요청이 시작될 여지가 있다. 이번 코드는 `login()`에서 로딩을 먼저 설정한다. UI 이벤트가 Main에서 순차 전달되는 사용 방식에 맞춘 방어이며 임의의 여러 스레드에서 동시에 호출하는 API로 설계한 것은 아니다.

같은 시점의 학번·비밀번호를 지역 변수로 보관하여 요청 대기 중 입력 수정의 영향을 받지 않게 했다. 재시도 시작 시 성공 상태도 false로 초기화한다. 실제 소스의 변경과 동일하게 줄별 설명에도 반영했다.

가이드 UI 테스트가 Truth를 사용하므로 `androidTestImplementation(libs.truth)`도 추가했다. `testImplementation`만으로는 별도 소스 세트인 androidTest에 제공되지 않는다.

## 테스트 해석과 실행

Fake 테스트는 최종 상태와 방출 순서를 본다. MockK 테스트는 호출 인자와 횟수를 기록해서 본다. UI 테스트는 Content의 상태별 표시와 콜백을 본다. UI 테스트 통과만으로 Factory 생성, 실제 로그인 서버, Navigation 연결까지 검증되는 것은 아니다.

`UnconfinedTestDispatcher`를 쓰는 기본 Rule은 코루틴이 중단점까지 바로 진입하도록 돕는다. MockK 테스트는 `StandardTestDispatcher`로 실행을 예약한 뒤 `advanceUntilIdle()`로 가상 시간을 진행한다. 이 차이를 모르면 `login()` 직후 성공을 검사하는 테스트가 왜 어떤 환경에서는 실패하는지 설명하기 어렵다.

```powershell
# attendance-app 디렉터리에서 JVM 단위 테스트
.\gradlew.bat testDebugUnitTest
# 앱과 계측 테스트 APK의 컴파일·패키징 검증
.\gradlew.bat assembleDebug assembleDebugAndroidTest
# 기기/에뮬레이터 연결 후 계측 테스트 실행
.\gradlew.bat connectedDebugAndroidTest
```

APK 빌드 성공과 기기에서 UI 테스트를 실행한 성공은 다르다. 실제 이번 검증 결과는 [검증 기록](VALIDATION.md)에 남긴다.

## 이해를 위해 스스로 설명해 볼 질문

1. 왜 TextField가 ViewModel의 값을 직접 변경하지 않고 콜백을 호출하는가?
2. StateFlow 수집이 중지된 것과 ViewModel이 제거된 것은 어떻게 다른가?
3. Fake와 MockK 중 로딩 상태 전환과 Repository 호출 횟수 검증에 각각 무엇을 선택했는가?
4. viewModelScope를 사용해도 `suspend` 함수 내부의 블로킹 작업이 자동으로 안전해지지 않는 이유는?
5. 로그인 성공 상태가 true인 채 화면에 재진입하면 LaunchedEffect는 어떻게 동작하는가?
6. 실제 서버를 연결하려면 어디를 바꾸고 무엇을 더 구현해야 하는가?

답변의 기준은 위의 실제 흐름이다. 현재 성공 상태를 소비·초기화하는 Navigation 정책은 없으므로, 이후 연결 단계에서 로그인 화면 제거 또는 성공 처리 정책을 정해야 한다. 프로세스가 종료되면 UiState가 사라지며 영구 저장소는 아니다. 현재 Fake 인증을 운영 인증 경험으로 설명하지 않는다.
