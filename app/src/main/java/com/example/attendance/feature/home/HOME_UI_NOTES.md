# HomeScreen과 HomeContent 줄별 해설

[홈 구조·문법·호출 흐름으로 돌아가기](README.md)

package/import와 기존 주석은 생략했다. 아래 코드의 각 실행 줄에 상세 설명을 붙였으며, 동작은 실제 소스와 같다. 테스트 코드는 작성 상태이며 이번 작업에서는 실행하지 않았다.

## HomeScreen.kt

원본: `app/src/main/java/com/example/attendance/feature/home/presentation/HomeScreen.kt`

```kotlin
// Compose UI를 구성하는 함수 표시다. 상태 변화로 다시 호출될 수 있다.
@Composable
/**
 * 책임: 상태 구독과 화면 틀 구성. 흐름: Factory로 ViewModel 확보 → 생명주기에 맞춰 수집 → Scaffold 구성 → 상태·콜백·여백을 Content에 전달. 이동은 외부 콜백의 책임이다.
 */
// HomeScreen 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
fun HomeScreen(
    // onCheckAttendance는 인자 없이 실행하고 Unit을 반환하는 콜백 타입이다. 호출자가 실제 이벤트 처리를 제공한다.
    onCheckAttendance: () -> Unit,
    // onMoreNotifications는 인자 없이 실행하고 Unit을 반환하는 콜백 타입이다. 호출자가 실제 이벤트 처리를 제공한다.
    onMoreNotifications: () -> Unit,
    // onMoreRecords는 인자 없이 실행하고 Unit을 반환하는 콜백 타입이다. 호출자가 실제 이벤트 처리를 제공한다.
    onMoreRecords: () -> Unit,
    // 호출자가 인스턴스를 주지 않으면 Factory로 현재 ViewModelStoreOwner의 ViewModel을 확보한다.
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
// 인자 선언을 닫고 함수 본문 또는 마지막 람다를 연다.
) {
    // 기본 STARTED 이상에서 상태를 Compose State로 수집하고 by로 값을 읽는다. 값 변경은 이를 읽은 UI의 재구성을 유도한다.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // 상단바와 본문을 담을 Material 화면 틀을 구성한다.
    Scaffold(
        // Scaffold 배경에 프로젝트의 Gray50 색상을 적용한다.
        containerColor = Gray50,
        // 상단바 슬롯에 앱 로고 바를 그리는 Composable 람다를 전달한다.
        topBar = { LogoTopBar() }
    // Scaffold가 본문에 제공하는 여백을 받는다. 상단바 등에 본문이 가리지 않도록 아래에서 적용한다.
    ) { paddingValues ->
        // 상태와 콜백으로만 그리는 Content를 호출한다. 아래 인자는 데이터와 이벤트 계약이다.
        HomeContent(
            // Screen에서 구독한 최신 HomeUiState를 Content에 넘긴다.
            uiState = uiState,
            // 전달받은 onCheckAttendance 함수를 하위 UI 콜백에 연결한다. 지금 실행하는 것이 아니라 클릭 때 실행된다.
            onCheckAttendance = onCheckAttendance,
            // 전달받은 onMoreNotifications 함수를 하위 UI 콜백에 연결한다. 지금 실행하는 것이 아니라 클릭 때 실행된다.
            onMoreNotifications = onMoreNotifications,
            // 전달받은 onMoreRecords 함수를 하위 UI 콜백에 연결한다. 지금 실행하는 것이 아니라 클릭 때 실행된다.
            onMoreRecords = onMoreRecords,
            // Scaffold가 계산한 외부 여백을 본문 Modifier에 적용한다.
            modifier = Modifier.padding(paddingValues)
        // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
        )
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
// 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
}
// Compose UI를 구성하는 함수 표시다. 상태 변화로 다시 호출될 수 있다.
@Composable
/**
 * 책임: 상태를 홈 UI로 표시. 흐름: 통계 → 세션 → 알림 → 기록 순으로 그린다. 클릭은 콜백으로 전달하고 자체 Repository나 ViewModel을 만들지 않는다.
 */
// HomeContent 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
fun HomeContent(
    // 렌더링에 필요한 홈 상태를 인자로 받는다. Content 내부에서 Repository를 조회하지 않는다.
    uiState: HomeUiState,
    // onCheckAttendance는 인자 없이 실행하고 Unit을 반환하는 콜백 타입이다. 호출자가 실제 이벤트 처리를 제공한다.
    onCheckAttendance: () -> Unit,
    // onMoreNotifications는 인자 없이 실행하고 Unit을 반환하는 콜백 타입이다. 호출자가 실제 이벤트 처리를 제공한다.
    onMoreNotifications: () -> Unit,
    // onMoreRecords는 인자 없이 실행하고 Unit을 반환하는 콜백 타입이다. 호출자가 실제 이벤트 처리를 제공한다.
    onMoreRecords: () -> Unit,
    // 상위에서 크기·여백을 전달할 수 있는 Modifier 인자다. 생략하면 기본 Modifier를 쓴다.
    modifier: Modifier = Modifier
// 인자 선언을 닫고 함수 본문 또는 마지막 람다를 연다.
) {
    // 하위 UI를 세로 순서로 배치하는 레이아웃을 시작한다.
    Column(
        // 상위 네 섹션 사이에 30dp 간격을 둬 시각적으로 구분한다.
        verticalArrangement = Arrangement.spacedBy(30.dp),
        // 호출자가 전달한 Modifier를 보존하면서 추가 레이아웃 동작을 이어 붙인다.
        modifier = modifier
            // 부모의 허용 범위 안에서 최대 크기를 채운다.
            .fillMaxSize()
            // 재구성 사이에 유지되는 스크롤 상태로 Column 전체를 세로 스크롤 가능하게 만든다.
            .verticalScroll(rememberScrollState())
            // 본문 좌우에 24dp, 위아래에 10dp 여백을 추가한다. dp는 화면 밀도를 고려한 단위다.
            .padding(horizontal = 24.dp, vertical = 10.dp)
    // 인자 선언을 닫고 함수 본문 또는 마지막 람다를 연다.
    ) {
        // 하위 요소를 세로로 배치하고 12.dp 간격을 둔다.
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // 공통 섹션 제목을 그린다. 이 호출에는 더보기 콜백이 없어 제목만 표시한다.
            SectionHeader(title = "이번 주 출석 현황")
            // 공통 통계 행 컴포넌트를 호출한다. 아래 StatItem 목록을 나란히 표시한다.
            StatSummaryRow(
                // 세 개의 통계 표시 모델을 읽기 전용 목록으로 묶어 전달한다.
                items = listOf(
                    // 통계 수치 문자열과 레이블을 담는 표시 모델을 만든다.
                    StatItem(
                        // 문자열 템플릿으로 출석 횟수와 전체 횟수를 연결한다. 예를 들어 3 / 10으로 표시된다.
                        value = "${uiState.weeklyStats.attendedCount} / ${uiState.weeklyStats.totalCount}",
                        // 통계 수치의 의미를 설명할 고정 레이블 "이번 주 출석"를 지정한다.
                        label = "이번 주 출석"
                    // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다. 쉼표는 다음 인자를 구분한다.
                    ),
                    // 통계 수치 문자열과 레이블을 담는 표시 모델을 만든다.
                    StatItem(
                        // Repository가 준 출석률에 %를 붙인다. 이 코드가 출석 횟수에서 비율을 다시 계산하는 것은 아니다.
                        value = "${uiState.weeklyStats.attendanceRate}%",
                        // 통계 수치의 의미를 설명할 고정 레이블 "활성 기간 출석률"를 지정한다.
                        label = "활성 기간 출석률"
                    // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다. 쉼표는 다음 인자를 구분한다.
                    ),
                    // 통계 수치 문자열과 레이블을 담는 표시 모델을 만든다.
                    StatItem(
                        // 남은 세션 정수에 회 단위를 붙여 표시한다.
                        value = "${uiState.weeklyStats.remainingSessions}회",
                        // 통계 수치의 의미를 설명할 고정 레이블 "오늘 남은 세션"를 지정한다.
                        label = "오늘 남은 세션"
                    // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다. 쉼표는 다음 인자를 구분한다.
                    ),
                // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
                )
            // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
            )
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
        // 하위 요소를 세로로 배치하고 9.dp 간격을 둔다.
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            // 공통 섹션 제목을 그린다. 이 호출에는 더보기 콜백이 없어 제목만 표시한다.
            SectionHeader(title = "오늘 수업 세션")
            // 하위 요소를 세로로 배치하고 15.dp 간격을 둔다.
            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                // 오늘 세션 목록의 원소마다 다음 UI를 구성한다. session은 현재 순회 중인 ClassSession이다.
                uiState.todaySessions.forEach { session ->
                    // 한 세션의 정보와 출석 체크 버튼을 표시하는 공통 카드를 호출한다.
                    SessionCard(
                        // 현재 순회 중인 세션을 카드에 전달한다. 카드 내부에서 isActive를 보고 버튼 표시를 결정한다.
                        session = session,
                        // 전달받은 onCheckAttendance 함수를 하위 UI 콜백에 연결한다. 지금 실행하는 것이 아니라 클릭 때 실행된다.
                        onCheckAttendance = onCheckAttendance
                    // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
                    )
                // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
                }
            // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
            }
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
        // 하위 요소를 세로로 배치하고 10.dp 간격을 둔다.
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // 공통 섹션 제목을 그린다. 아래 제목과 더보기 콜백을 지정한다.
            SectionHeader(
                // 해당 섹션에 표시할 제목 "읽지 않은 알림"을 지정한다.
                title = "읽지 않은 알림",
                // 전달받은 onMoreNotifications 함수를 하위 UI 콜백에 연결한다. 지금 실행하는 것이 아니라 클릭 때 실행된다.
                onMore = onMoreNotifications
            // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
            )
            // ViewModel이 이미 필터링한 알림 목록을 순회한다. Content에서 별도 조회나 필터링을 하지 않는다.
            uiState.unreadNotifications.forEach { notification ->
                // 알림 한 건을 표시하되 본문은 숨긴다. 홈 요약용 행이다.
                NotificationRow(notification = notification, showContent = false)
            // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
            }
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
        // 하위 요소를 세로로 배치하고 10.dp 간격을 둔다.
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // 공통 섹션 제목을 그린다. 아래 제목과 더보기 콜백을 지정한다.
            SectionHeader(
                // 해당 섹션에 표시할 제목 "최근 출석 기록"을 지정한다.
                title = "최근 출석 기록",
                // 전달받은 onMoreRecords 함수를 하위 UI 콜백에 연결한다. 지금 실행하는 것이 아니라 클릭 때 실행된다.
                onMore = onMoreRecords
            // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
            )
            // 최근 기록 목록을 순회하며 각 기록의 표시 UI를 만든다.
            uiState.recentRecords.forEach { record ->
                // 현재 기록을 공통 최근 기록 행에 전달한다.
                RecentRecordRow(record = record)
            // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
            }
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
// 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
}
// Android Studio 미리보기의 배경과 390×1200dp 크기를 지정한다. 실제 기기 실행 경로를 변경하지 않는다.
@Preview(showBackground = true, widthDp = 390, heightDp = 1200)
// Compose UI를 구성하는 함수 표시다. 상태 변화로 다시 호출될 수 있다.
@Composable
/**
 * 책임: 샘플 홈 배치 미리보기. 흐름: 앱 테마 → 샘플 HomeUiState → 빈 콜백과 함께 Content 구성. 실데이터 수집과 이동은 검증하지 않는다.
 */
// HomeContentPreview 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
private fun HomeContentPreview() {
    // 앱의 Compose 테마 아래에서 UI를 구성하여 프로젝트 스타일을 적용한다.
    AttendanceTheme {
        // 상태와 콜백으로만 그리는 Content를 호출한다. 아래 인자는 데이터와 이벤트 계약이다.
        HomeContent(
            // Preview에 전달할 상태 객체를 직접 구성한다. ViewModel이나 Flow는 실행하지 않는다.
            uiState = HomeUiState(
                // 샘플 통계 네 값을 넣는다. 실제 데이터 조회 결과라고 해석하지 않는다.
                weeklyStats = WeeklyStats(3, 10, 55, 2),
                // Preview에서 오늘 세션의 샘플 목록을 보여 준다.
                todaySessions = SampleData.todaySessions,
                // Preview에서도 홈 표시 정책과 같은 읽지 않은 알림 앞의 두 건을 준비한다.
                unreadNotifications = SampleData.notifications.filter { !it.isRead }.take(2),
                // Preview에서 샘플 최근 기록을 보여 준다.
                recentRecords = SampleData.recentRecords
            // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다. 쉼표는 다음 인자를 구분한다.
            ),
            // 미리보기에서 onCheckAttendance 이벤트가 실제 이동을 하지 않도록 빈 람다를 전달한다.
            onCheckAttendance = {},
            // 미리보기에서 onMoreNotifications 이벤트가 실제 이동을 하지 않도록 빈 람다를 전달한다.
            onMoreNotifications = {},
            // 미리보기에서 onMoreRecords 이벤트가 실제 이동을 하지 않도록 빈 람다를 전달한다.
            onMoreRecords = {}
        // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
        )
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
// 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
}
```
