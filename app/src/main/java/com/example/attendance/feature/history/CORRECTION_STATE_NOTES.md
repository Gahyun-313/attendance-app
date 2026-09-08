# 정정 요청 상태와 ViewModel 줄별 해설

## 책임과 실행 흐름

SavedStateHandle → ID 검증 → getRecord → CorrectionUiState → 입력 검증 → requestCorrection → 성공 또는 실패 상태

실제 파일: `app/src/main/java/com/example/attendance/feature/history/presentation/CorrectionViewModel.kt`. package/import는 생략하고 나머지 코드의 실행 순서와 동작을 유지했다. 실제 소스의 주석은 주요 책임 중심이며, 아래는 학습을 위한 줄별 해설이다.

```kotlin
/** 정정 대상, 입력값, 조회·제출 상태를 한 번에 전달하는 화면 상태다. */
// data class는 값 비교와 copy를 제공한다. 화면에 필요한 값을 하나의 불변 상태로 묶는다.
data class CorrectionUiState(
    // 조회 전이거나 대상이 없으면 null이다. nullable 타입으로 기록 부재를 표현한다.
    val record: AttendanceRecord? = null,
    // 필수 사유의 현재 입력값이다. SavedStateHandle에 저장하는 초안은 아니다.
    val reason: String = "",
    // 선택 입력인 상세 내용이다. 빈 문자열도 제출할 수 있다.
    val detail: String = "",
    // 기록 조회가 끝나기 전임을 표시해 없는 기록과 로딩을 구분한다.
    val isLoading: Boolean = true,
    // 사유가 공백뿐일 때 입력 필드와 오류 문구에 반영한다.
    val isReasonError: Boolean = false,
    // 제출 중 버튼을 잠그고 중복 호출을 차단한다.
    val isSubmitting: Boolean = false,
    // 성공 후 Screen이 완료 콜백을 호출하는 상태다.
    val isSubmitted: Boolean = false,
    // 조회 실패·없는 대상·제출 실패를 표시하며 정상 상태는 null이다.
    val errorMessage: String? = null
// 앞에서 지정한 생성자 또는 함수 호출의 인자 목록을 닫는다.
)

/**
 * SavedStateHandle의 기록 ID로 대상을 조회하고 입력 검증·제출을 담당한다.
 * Repository는 생성자로 주입해 실제 Navigation 없이도 상태 흐름을 테스트할 수 있다.
 */
// ViewModel은 화면의 재구성과 분리해 상태와 비동기 작업을 소유한다.
class CorrectionViewModel(
    // Navigation 연결 후에는 해당 back stack entry의 인자·복원 상태가 이 객체에 들어온다.
    savedStateHandle: SavedStateHandle,
    // 인터페이스에 의존하며 현재 앱에서는 수동 DI가 Fake 구현을 제공한다.
    private val attendanceRepository: AttendanceRepository
// 상속한 ViewModel이 정리되면 viewModelScope의 자식 코루틴도 취소된다.
) : ViewModel() {
    // 쓰기 가능한 상태는 ViewModel 안에만 둔다.
    private val _uiState = MutableStateFlow(CorrectionUiState())
    // 외부에는 읽기 전용 StateFlow를 공개한다.
    val uiState = _uiState.asStateFlow()

    /** 생성 시 ID를 확인하고 대상을 조회한다. 없는 ID와 조회 실패는 화면 오류로 바꾼다. */
    // 생성 직후 한 번 실행한다. Compose의 재구성마다 실행되는 코드는 아니다.
    init {
        // Long 타입 인자를 읽는다. 후속 Navigation도 같은 키와 Long 타입을 사용해야 한다.
        val recordId = savedStateHandle.get<Long>(ARG_RECORD_ID)
        // 누락·0·음수는 조회 전에 거절하며 0을 정상 ID로 대체하지 않는다.
        if (recordId == null || recordId <= 0L) {
            // 로딩을 끝내고 대상 부재 오류를 표시한다.
            _uiState.update { it.copy(isLoading = false, errorMessage = "정정 대상 기록을 찾을 수 없습니다.") }
        // 유효한 형태의 ID일 때만 Repository 조회를 시작한다.
        } else {
            // suspend 조회를 화면 수명에 속한 코루틴에서 실행한다.
            viewModelScope.launch {
                // 조회 도중의 일반 예외를 화면 상태로 전환할 경계다.
                try {
                    // ID에 해당하는 기록 한 건 또는 null을 기다린다.
                    val record = attendanceRepository.getRecord(recordId)
                    // 최신 입력값 등 나머지 필드를 유지하며 조회 결과만 바꾼다.
                    _uiState.update {
                        // 기존 객체를 수정하지 않고 새 상태 객체를 만든다.
                        it.copy(
                            // null이면 대상이 없으므로 제출을 허용하지 않는다.
                            record = record,
                            // 기록 존재 여부와 관계없이 조회 대기는 끝났다.
                            isLoading = false,
                            // 존재 여부를 사용자에게 설명하는 오류로 변환한다.
                            errorMessage = if (record == null) "정정 대상 기록을 찾을 수 없습니다." else null
                        // 앞에서 지정한 생성자 또는 함수 호출의 인자 목록을 닫는다.
                        )
                    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
                    }
                // 화면 종료 등으로 발생한 코루틴 취소는 일반 실패가 아니다.
                } catch (cancelled: CancellationException) {
                    // 취소 신호를 다시 던져 구조적 동시성의 취소 전파를 유지한다.
                    throw cancelled
                // Repository의 일반 조회 예외는 화면 오류로 처리한다.
                } catch (_: Exception) {
                    // 무한 로딩을 막고 재진입 안내를 남긴다.
                    _uiState.update { it.copy(isLoading = false, errorMessage = "기록을 불러오지 못했습니다. 다시 진입해주세요.") }
                // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
                }
            // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
            }
        // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
        }
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }

    /** 사유 입력을 반영하고 이전 필수 입력 오류를 해제한다. */
    // AppTextField의 입력 콜백이 새 문자열을 전달한다.
    fun onReasonChange(value: String) {
        // 입력값과 사유 오류만 바꾼다. 조회 오류는 지우지 않는다.
        _uiState.update { it.copy(reason = value, isReasonError = false) }
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }

    /** 선택 입력인 상세 내용을 반영한다. 제출은 클릭 시점의 입력값을 사용한다. */
    // 상세 필드의 새 문자열을 받는다.
    fun onDetailChange(value: String) {
        // 나머지 상태를 보존하며 상세 값만 교체한다.
        _uiState.update { it.copy(detail = value) }
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }

    /**
     * 제출 가능 상태와 필수 사유를 검사한 후 Repository에 요청한다.
     * 코루틴 시작 전에 제출 상태를 잠가 연속 클릭을 막고, 실패하면 재시도를 허용한다.
     */
    // UI의 제출 콜백에서 호출한다. 버튼 비활성화와 별도로 내부 검증도 수행한다.
    fun submit() {
        // 클릭 시점의 기록·입력값을 한 번 읽어 요청 인자로 고정한다.
        val state = _uiState.value
        // 조회 중·제출 중·이미 성공한 경우 추가 제출을 막는다.
        if (state.isLoading || state.isSubmitting || state.isSubmitted) return
        // 대상이 없으면 Repository에 요청하지 않는다.
        val record = state.record ?: return
        // 빈 문자열뿐 아니라 공백·줄바꿈만 있는 사유도 거절한다.
        if (state.reason.isBlank()) {
            // 필수 입력 오류를 표시한다.
            _uiState.update { it.copy(isReasonError = true) }
            // 입력 검증 실패 시 아래 비동기 요청까지 내려가지 않는다.
            return
        // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
        }

        // launch 전에 즉시 잠그고 이전 제출 오류를 초기화한다.
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null, isReasonError = false) }
        // 요청 중에도 메인 스레드를 막지 않고 suspend 결과를 기다린다.
        viewModelScope.launch {
            // Boolean 실패와 예외 실패를 모두 UI에서 처리한다.
            try {
                // 제출 후 입력이 바뀌어도 이 요청에는 클릭 당시의 값이 전달된다.
                val success = attendanceRepository.requestCorrection(record.id, state.reason, state.detail)
                // 응답을 받으면 현재 입력값을 보존하며 제출 상태를 갱신한다.
                _uiState.update {
                    // 화면에 새 불변 상태를 방출한다.
                    it.copy(
                        // 요청이 끝났으므로 진행 상태를 해제한다.
                        isSubmitting = false,
                        // 성공일 때만 완료 콜백의 조건이 된다.
                        isSubmitted = success,
                        // false이면 재시도 가능한 오류를 표시한다.
                        errorMessage = if (success) null else "정정 요청에 실패했습니다. 다시 시도해주세요."
                    // 앞에서 지정한 생성자 또는 함수 호출의 인자 목록을 닫는다.
                    )
                // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
                }
            // 취소는 접수 실패 메시지로 바꾸지 않는다.
            } catch (cancelled: CancellationException) {
                // 취소된 작업의 진행 표시를 정리한다.
                _uiState.update { it.copy(isSubmitting = false) }
                // 부모 코루틴에 취소를 전파한다.
                throw cancelled
            // 일반 요청 예외도 제출 중 상태에 갇히지 않도록 처리한다.
            } catch (_: Exception) {
                // 성공으로 처리하지 않고 오류와 재시도 가능 상태를 남긴다.
                _uiState.update { it.copy(isSubmitting = false, errorMessage = "정정 요청에 실패했습니다. 다시 시도해주세요.") }
            // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
            }
        // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
        }
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }

    // 인스턴스 없이 접근할 인자 계약과 Factory를 묶는다.
    companion object {
        // Navigation 연결 단계에서도 이 키와 Long 타입을 사용한다.
        // 아직 없는 ScreenRoute에 의존하지 않고 현재 기능에서 인자 키를 정의한다.
        const val ARG_RECORD_ID = "recordId"

        /** CreationExtras에서 저장 상태와 앱 컨테이너를 받아 수동 DI로 생성한다. */
        // Compose viewModel(factory = ...)에 제공하는 생성 규칙이다.
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            // ViewModelStore에 인스턴스가 없을 때 이 초기화 코드가 사용된다.
            initializer {
                // CreationExtras의 Application을 앱 타입으로 읽는다.
                val app = this[APPLICATION_KEY] as AttendanceApp
                // 생성자 주입으로 Android 프레임워크와 상태 로직을 분리한다.
                CorrectionViewModel(
                    // 소유자의 저장 상태 및 기본 인자로 handle을 만든다. 인자 없는 Activity에서는 대상 누락 오류가 된다.
                    savedStateHandle = createSavedStateHandle(),
                    // 기존 컨테이너의 공용 Repository를 재사용한다.
                    attendanceRepository = app.container.attendanceRepository
                // 앞에서 지정한 생성자 또는 함수 호출의 인자 목록을 닫는다.
                )
            // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
            }
        // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
        }
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }
// 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
}
```
