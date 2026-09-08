# AttendanceViewModel 줄별 해설

[출석 체크 구조·문법·호출 흐름으로 돌아가기](README.md)

package/import와 기존 주석은 생략했다. 아래 코드의 각 실행 줄에 상세 설명을 붙였으며, 동작은 실제 소스와 같다. 테스트는 사용자가 직접 실행해 통과를 확인했다(VALIDATION.md 참고).

## AttendanceViewModel.kt

원본: `app/src/main/java/com/example/attendance/feature/attendance/presentation/AttendanceViewModel.kt`

```kotlin
// 출석 체크 화면의 데이터를 한 객체로 전달할 데이터 클래스를 선언한다. copy와 equals 등이 자동 생성된다.
data class AttendanceUiState(
    // 출석 체크 대상 세션이다. 로딩 전이거나 활성 세션이 없으면 null이다.
    val session: ClassSession? = null,
    // 현재 출석 상태다. 기본값은 미출석이며 스캔이 끝나면 결과로 갱신된다.
    val status: AttendanceStatus = AttendanceStatus.NOT_YET,
    // NFC 인증 모달을 띄울지 여부다. 스캔 시작부터 종료(성공/취소)까지만 true다.
    val isNfcScanning: Boolean = false,
    // 출석 체크 결과다. null이면 결과 모달을 띄우지 않는다. status와 별도로 두어 모달 표시 여부를 독립적으로 제어한다.
    val result: AttendanceStatus? = null,
    // 출석 처리 시각 문자열이다. 스캔이 끝나기 전에는 빈 문자열이다.
    val checkedAt: String = ""
// 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
)
// class AttendanceViewModel를 선언하고 관련 상태와 메서드를 묶는다.
class AttendanceViewModel(
    // 출석 관련 조회·처리 계약을 생성자 인자로 받는다. private val이라 프로퍼티로도 보관되어 이후 메서드에서 재사용한다.
    private val attendanceRepository: AttendanceRepository
// 생성자 인자 목록을 닫고 AndroidX ViewModel을 상속한다. viewModelScope가 이 객체의 수명에 연결된다.
) : ViewModel() {

    // 내부에서만 바꿀 수 있는 가변 상태 보관소를 초기값 AttendanceUiState()로 만든다.
    private val _uiState = MutableStateFlow(AttendanceUiState())
    // 외부에는 읽기 전용 StateFlow만 노출한다. asStateFlow()는 내부 MutableStateFlow를 감싸 쓰기를 막는다.
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    // 진행 중인 스캔 작업을 담을 프로퍼티다. var로 선언해 startAttendance마다 새 Job으로 교체할 수 있다. 화면 상태(AttendanceUiState)가 아니라 취소를 위한 내부 핸들이다.
    private var scanJob: Job? = null

    // ViewModel 인스턴스가 생성될 때 한 번 실행되는 초기화 블록이다.
    init {
        // ViewModel의 생명주기에 묶인 코루틴을 시작한다. ViewModel이 제거되면 이 작업도 취소된다.
        viewModelScope.launch {
            // 오늘 세션 목록 Flow에서 첫 방출 값만 수집하고 수집을 끝낸다. 이후 upstream이 바뀌어도 다시 조회하지 않는다.
            val sessions = attendanceRepository.getTodaySessions().first()
            // 현재 상태를 읽어 일부 필드만 바꾼 새 값으로 원자적으로 교체한다.
            _uiState.update { state ->
                // sessions 목록에서 isActive가 true인 첫 세션을 찾아 session 필드에 저장한다. 없으면 null이다.
                state.copy(session = sessions.find { it.isActive })
            // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
            }
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }

    /**
     * 책임: '출석 시작' 클릭 처리. 흐름: 세션·중복 실행 확인 → 스캔 모달 표시 → checkAttendance 호출 → 결과 반영. UI에서 버튼 클릭 시 호출한다.
     */
    // startAttendance 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
    fun startAttendance() {
        // 대상 세션이 없으면(session == null) 엘비스 연산자로 즉시 함수를 반환해 이후 로직을 실행하지 않는다.
        val session = _uiState.value.session ?: return
        // 이미 스캔 중이면 중복 실행을 막기 위해 즉시 반환한다. 이 가드가 없으면 두 번째 클릭이 첫 scanJob 참조를 덮어써 취소가 불가능해진다.
        if (_uiState.value.isNfcScanning) return // 중복 실행 방지

        // NFC 스캔 모달을 띄우기 위해 isNfcScanning을 true로 바꾼다.
        _uiState.update { it.copy(isNfcScanning = true) }

        // 새 코루틴을 시작해 그 Job을 scanJob 프로퍼티에 대입한다. cancelScan()이 나중에 이 참조로 취소를 요청할 수 있다.
        scanJob = viewModelScope.launch {
            // NFC 태깅 + 서버 처리 (현재는 Fake 구현이 지연 후 결과 반환)
            // TODO: 실제 NfcAdapter 콜백에서 읽은 태그 UID로 교체 — 지금은 시뮬레이션이라 임시 UID를 사용한다.
            // 어떤 세션인지는 서버가 태그 UID로 역추적하므로 여기서 session.id를 넘기지 않는다.
            // Repository의 출석 체크를 호출하고 완료(또는 취소)될 때까지 이 지점에서 대기한다. cancelScan()이 먼저 호출되면 delay 지점에서 취소되어 아래 줄이 실행되지 않는다.
            val resultStatus = attendanceRepository.checkAttendance(nfcTagUid = "FAKE-NFC-TAG-UID")
            // 스캔이 취소되지 않고 끝까지 진행된 경우에만 도달한다. 결과를 상태에 반영한다.
            _uiState.update {
                // 네 필드를 한꺼번에 갱신한 새 상태를 만든다.
                it.copy(
                    // 모달을 닫기 위해 스캔 중 플래그를 false로 되돌린다.
                    isNfcScanning = false,
                    // 현재 출석 상태를 이번 스캔 결과로 갱신한다.
                    status = resultStatus,
                    // 결과 모달을 띄우기 위해 result에도 같은 값을 저장한다. status와 값은 같지만 역할이 다르다.
                    result = resultStatus,
                    // 결과 모달에 표시할 처리 시각을 계산해 저장한다.
                    checkedAt = currentTimestamp()
                // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
                )
            // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
            }
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }

    /**
     * 책임: NFC 모달 닫기 및 진행 중인 스캔 취소. 흐름: scanJob 취소 요청 → isNfcScanning을 false로. 사용자가 모달을 닫을 때 호출한다.
     */
    // cancelScan 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
    fun cancelScan() {
        // scanJob이 null이 아니면 취소를 요청한다. 이미 끝난 코루틴이면 아무 효과가 없다.
        scanJob?.cancel()
        // 모달을 닫기 위해 스캔 중 플래그를 false로 바꾼다. result는 건드리지 않으므로 여전히 null이라 결과 모달도 뜨지 않는다.
        _uiState.update { it.copy(isNfcScanning = false) }
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }

    /**
     * 책임: 결과 모달 닫기. 흐름: result를 null로 되돌린다. 사용자가 결과 모달을 닫을 때 호출한다.
     */
    // dismissResult 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
    fun dismissResult() {
        // result를 null로 되돌려 결과 모달이 다시 뜨지 않게 한다. status는 유지되어 출석 상태 카드에는 계속 반영된다.
        _uiState.update { it.copy(result = null) }
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }

    /**
     * 책임: 현재 시각 문자열 생성. 결과 모달의 처리 시각 표시에 사용한다.
     */
    // currentTimestamp 함수를 선언한다. 반환 타입은 String이고 본문은 = 뒤의 식이다.
    private fun currentTimestamp(): String =
        // 현재 날짜·시각을 얻는다.
        java.time.LocalDateTime.now()
            // "yyyy-MM-dd HH:mm:ss" 패턴으로 사람이 읽기 쉬운 문자열로 바꾼다.
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    // AttendanceViewModel.Factory처럼 클래스 이름을 통해 접근할 동반 객체를 연다.
    companion object {
        /**
         * 책임: AppContainer에서 Repository를 꺼내 ViewModel을 생성하는 Factory. viewModel(factory = ...) 호출 시 사용한다.
         */
        // 의존성이 필요한 ViewModel의 생성 방법을 Factory DSL로 정의한다. 이미 존재하는 인스턴스는 viewModel()이 재사용한다.
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            // 새 ViewModel이 필요할 때 실행할 초기화 람다를 등록한다. 마지막 식이 생성 결과다.
            initializer {
                // CreationExtras의 Application을 가져와 AttendanceApp으로 캐스팅한다. Manifest의 Application 등록이 맞아야 한다.
                val app = this[APPLICATION_KEY] as AttendanceApp
                // AttendanceViewModel 생성자를 호출하며 Application의 수동 DI 컨테이너에서 출석 Repository를 꺼내 주입한다.
                AttendanceViewModel(attendanceRepository = app.container.attendanceRepository)
            // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
            }
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
// 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
}
```
