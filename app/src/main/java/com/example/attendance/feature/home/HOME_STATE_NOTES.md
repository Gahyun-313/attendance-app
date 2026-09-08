# HomeViewModel 줄별 해설

[홈 구조·문법·호출 흐름으로 돌아가기](README.md)

package/import와 기존 주석은 생략했다. 아래 코드의 각 실행 줄에 상세 설명을 붙였으며, 동작은 실제 소스와 같다. 테스트 코드는 작성 상태이며 이번 작업에서는 실행하지 않았다.

## HomeViewModel.kt

원본: `app/src/main/java/com/example/attendance/feature/home/presentation/HomeViewModel.kt`

```kotlin
// 홈 화면의 데이터를 한 객체로 전달할 데이터 클래스를 선언한다. copy와 equals 등이 자동 생성된다.
data class HomeUiState(
    // 이번 주 통계를 읽기 전용 프로퍼티로 보관한다. 네 정수의 기본값은 출석 횟수·전체 횟수·출석률·남은 세션이 모두 0이다.
    val weeklyStats: WeeklyStats = WeeklyStats(0, 0, 0, 0),
    // 오늘의 수업 객체 목록이다. Repository의 첫 결과를 받기 전에는 빈 목록으로 시작한다.
    val todaySessions: List<ClassSession> = emptyList(),
    // 홈에 표시할 읽지 않은 알림 목록이다. ViewModel의 필터 결과를 담으며 초기에는 비어 있다.
    val unreadNotifications: List<NotificationItem> = emptyList(),
    // 최근 출석 기록의 읽기 전용 목록이다. 기본 빈 목록을 사용해 UI의 null 분기를 줄인다.
    val recentRecords: List<AttendanceRecord> = emptyList()
// 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
)
// class HomeViewModel를 선언하고 관련 상태와 메서드를 묶는다.
class HomeViewModel(
    // 출석 관련 조회 계약을 생성자 인자로 받는다. 여기서는 val이 없어 별도 프로퍼티로 선언하지 않는다.
    attendanceRepository: AttendanceRepository,
    // 알림 관련 조회 계약을 생성자 인자로 받는다. 구체적인 Fake 생성은 외부의 책임이다.
    notificationRepository: NotificationRepository
// 생성자 인자 목록을 닫고 AndroidX ViewModel을 상속한다. viewModelScope가 이 객체의 수명에 연결된다.
) : ViewModel() {
    // 외부에 공개할 읽기 전용 상태를 선언하고 네 Flow의 최신 값을 결합하는 연산을 시작한다.
    val uiState: StateFlow<HomeUiState> = combine(
        // 첫 번째 upstream으로 이번 주 통계 Flow를 받는다. 아래 stats 매개변수와 대응한다.
        attendanceRepository.getWeeklyStats(),
        // 두 번째 upstream으로 오늘 세션 목록 Flow를 받는다. 아래 sessions와 대응한다.
        attendanceRepository.getTodaySessions(),
        // 세 번째 upstream으로 전체 알림 Flow를 받는다. 아래 notifications와 대응한다.
        notificationRepository.getNotifications(),
        // 네 번째 upstream으로 최근 출석 기록 Flow를 받는다. 아래 records와 대응한다.
        attendanceRepository.getRecentRecords()
    // 네 Flow가 모두 최초 값을 내보낸 뒤 최신 값들을 이 순서의 매개변수로 받아 상태를 계산한다.
    ) { stats, sessions, notifications, records ->
        // 전달받은 데이터를 하나의 홈 상태 객체로 묶는다. 람다의 마지막 식이 combine의 출력값이다.
        HomeUiState(
            // 첫 번째 Flow의 최신 통계를 화면 상태에 저장한다.
            weeklyStats = stats,
            // 두 번째 Flow의 최신 세션 목록을 화면 상태에 저장한다.
            todaySessions = sessions,
            // it은 알림 한 건이다. 읽음 항목을 제외한 뒤 앞의 두 건을 선택한다. 목록 순서는 유지하며 별도 시간 정렬은 하지 않는다.
            unreadNotifications = notifications.filter { !it.isRead }.take(2),
            // 네 번째 Flow의 최신 출석 기록을 상태에 저장한다.
            recentRecords = records
        // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
        )
    // 결합 결과 Flow를 현재 값을 갖고 여러 구독자가 공유할 수 있는 StateFlow로 바꾼다.
    }.stateIn(
        // upstream 수집 작업을 ViewModel 범위에서 실행한다. ViewModel 제거 시 작업도 취소된다.
        scope = viewModelScope,
        // 구독자가 있으면 수집하고 마지막 구독자가 사라진 후 5000ms까지 유지한다. 숫자의 밑줄은 가독성용 구분이다.
        started = SharingStarted.WhileSubscribed(5_000),
        // 첫 결합 결과 전에도 StateFlow.value가 반환할 기본 상태다. 실제 조회 결과와 구분해야 한다.
        initialValue = HomeUiState()
    // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
    )
    // HomeViewModel.Factory처럼 클래스 이름을 통해 접근할 동반 객체를 연다.
    companion object {
        // 의존성이 필요한 ViewModel의 생성 방법을 Factory DSL로 정의한다. 이미 존재하는 인스턴스는 viewModel()이 재사용한다.
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            // 새 ViewModel이 필요할 때 실행할 초기화 람다를 등록한다. 마지막 식이 생성 결과다.
            initializer {
                // CreationExtras의 Application을 가져와 AttendanceApp으로 캐스팅한다. Manifest의 Application 등록이 맞아야 한다.
                val app = this[APPLICATION_KEY] as AttendanceApp
                // 홈 ViewModel 생성자를 호출한다. 바로 아래에서 두 Repository를 전달한다.
                HomeViewModel(
                    // Application의 수동 DI 컨테이너에서 출석 Repository를 꺼내 주입한다.
                    attendanceRepository = app.container.attendanceRepository,
                    // 같은 컨테이너에서 알림 Repository를 꺼내 주입한다. 같은 객체를 쓰는 화면은 알림 변경을 공유할 수 있다.
                    notificationRepository = app.container.notificationRepository
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
