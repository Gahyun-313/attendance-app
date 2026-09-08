# NotificationViewModel 줄별 해설

[알림 화면 구조·문법·호출 흐름으로 돌아가기](README.md)

package/import와 기존 주석은 생략했다. 아래 코드의 각 실행 줄에 상세 설명을 붙였으며, 동작은 실제 소스와 같다. 테스트는 사용자가 직접 실행해 통과를 확인했다(VALIDATION.md 참고).

## NotificationViewModel.kt

원본: `app/src/main/java/com/example/attendance/feature/notification/presentation/NotificationViewModel.kt`

```kotlin
// 알림 화면의 데이터를 한 객체로 전달할 데이터 클래스를 선언한다. copy와 equals 등이 자동 생성된다.
data class NotificationUiState(
    // 읽지 않은 알림 목록이다. 기본값은 빈 목록이다.
    val unread: List<NotificationItem> = emptyList(),
    // 읽은 알림 목록이다. 기본값은 빈 목록이다.
    val read: List<NotificationItem> = emptyList()
// 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫고 클래스 본문 블록을 연다.
) {
    // 저장 프로퍼티가 아니라 매번 계산되는 프로퍼티다. unread와 read가 모두 비어 있을 때만 true다. copy() 대상에는 포함되지 않는다.
    val isEmpty: Boolean get() = unread.isEmpty() && read.isEmpty()
// 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
}
// class NotificationViewModel를 선언하고 관련 상태와 메서드를 묶는다.
class NotificationViewModel(
    // 알림 조회·처리 계약을 생성자 인자로 받는다. private val이라 프로퍼티로도 보관되어 이후 메서드에서 재사용한다.
    private val notificationRepository: NotificationRepository
// 생성자 인자 목록을 닫고 AndroidX ViewModel을 상속한다. viewModelScope가 이 객체의 수명에 연결된다.
) : ViewModel() {

    // 알림 목록 상태 — Repository 변경 시 자동 갱신
    // 외부에 공개할 읽기 전용 상태를 선언한다. 대입 식 전체가 아래에서 여러 줄로 이어진다.
    val uiState: StateFlow<NotificationUiState> =
        // Repository의 전체 알림 목록 Flow를 upstream으로 가져온다.
        notificationRepository.getNotifications()
            // upstream이 새 목록을 낼 때마다 이 변환 람다가 다시 실행된다. list는 그 시점의 전체 알림 목록이다.
            .map { list ->
                // 원본 목록 하나를 읽음/안읽음 두 목록으로 나눠 담은 상태를 만든다.
                NotificationUiState(
                    // it은 알림 한 건이다. 읽지 않은 것만 걸러 unread에 담는다.
                    unread = list.filter { !it.isRead },
                    // 같은 목록에서 읽은 것만 걸러 read에 담는다. unread와 겹치지 않는다.
                    read = list.filter { it.isRead }
                // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
                )
            // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
            }
            // map이 계산한 결과 Flow를 현재 값을 갖고 여러 구독자가 공유할 수 있는 StateFlow로 바꾼다.
            .stateIn(
                // upstream 수집 작업을 ViewModel 범위에서 실행한다. ViewModel 제거 시 작업도 취소된다.
                scope = viewModelScope,
                // 구독자가 있으면 수집하고 마지막 구독자가 사라진 후 5000ms까지 유지한다.
                started = SharingStarted.WhileSubscribed(5_000),
                // 첫 분류 결과가 나오기 전에도 StateFlow.value가 반환할 기본 상태다. 두 목록 모두 빈 상태다.
                initialValue = NotificationUiState()
            // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
            )

    /**
     * 책임: '모두 읽음 처리' 클릭 이벤트를 Repository에 위임. 흐름: 코루틴 시작 → Repository의 markAllAsRead 호출. UI에서 버튼 클릭 시 호출한다.
     */
    // markAllAsRead 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
    fun markAllAsRead() {
        // ViewModel의 생명주기에 묶인 코루틴을 시작한다. suspend 함수는 코루틴 안에서만 호출할 수 있다.
        viewModelScope.launch {
            // Repository에 실제 갱신을 위임한다. 이 호출이 끝나면 Repository의 Flow가 새 목록을 방출하고, 위 map이 다시 실행되어 화면이 갱신된다.
            notificationRepository.markAllAsRead()
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }

    // NotificationViewModel.Factory처럼 클래스 이름을 통해 접근할 동반 객체를 연다.
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
                // NotificationViewModel 생성자를 호출하며 Application의 수동 DI 컨테이너에서 알림 Repository를 꺼내 주입한다. 이 Repository는 홈 화면과 같은 인스턴스다(컨테이너의 by lazy).
                NotificationViewModel(notificationRepository = app.container.notificationRepository)
            // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
            }
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
// 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
}
```
