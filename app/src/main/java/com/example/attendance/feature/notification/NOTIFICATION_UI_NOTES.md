# NotificationScreen과 NotificationContent 줄별 해설

[알림 화면 구조·문법·호출 흐름으로 돌아가기](README.md)

package/import와 기존 주석은 생략했다. 아래 코드의 각 실행 줄에 상세 설명을 붙였으며, 동작은 실제 소스와 같다. 테스트는 사용자가 직접 실행해 통과를 확인했다(VALIDATION.md 참고).

## NotificationScreen.kt

원본: `app/src/main/java/com/example/attendance/feature/notification/presentation/NotificationScreen.kt`

```kotlin
// Compose UI를 구성하는 함수 표시다. 상태 변화로 다시 호출될 수 있다.
@Composable
/**
 * 책임: 상태 구독과 화면 틀 구성. 흐름: Factory로 ViewModel 확보 → 생명주기에 맞춰 수집 → Scaffold(상단바) 구성 → 상태·콜백·여백을 Content에 전달.
 */
// NotificationScreen 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
fun NotificationScreen(
    // 뒤로 가기 처리에 쓸 NavHostController를 인자로 받는다. 이 화면을 그래프에 등록하는 작업은 이번 범위에 없다.
    navController: NavHostController,
    // 호출자가 인스턴스를 주지 않으면 Factory로 현재 ViewModelStoreOwner의 ViewModel을 확보한다.
    viewModel: NotificationViewModel = viewModel(factory = NotificationViewModel.Factory)
// 인자 선언을 닫고 함수 본문 또는 마지막 람다를 연다.
) {
    // 기본 STARTED 이상에서 상태를 Compose State로 수집하고 by로 값을 읽는다. 값 변경은 이를 읽은 UI의 재구성을 유도한다.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 상단바와 본문을 담을 Material 화면 틀을 구성한다.
    Scaffold(
        // Scaffold 배경에 프로젝트의 Gray50 색상을 적용한다.
        containerColor = Gray50,
        // 상단바 슬롯에 뒤로가기 버튼이 있는 공통 상단바를 그리는 Composable 람다를 전달한다. 가이드의 BottomNavigationBar는 아직 이 저장소에 없어 대신 상단 뒤로가기를 쓴다.
        topBar = {
            // 제목과 뒤로가기 콜백을 전달해 공통 뒤로가기 상단바를 구성한다. 콜백은 NavHostController의 popBackStack을 호출하는 람다다.
            BackTopBar(title = "알림", onBack = { navController.popBackStack() })
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
    // Scaffold가 본문에 제공하는 여백을 받는다. 상단바 등에 본문이 가리지 않도록 아래에서 적용한다.
    ) { paddingValues ->
        // 상태와 콜백으로만 그리는 Content를 호출한다. 아래 인자는 데이터와 이벤트 계약이다.
        NotificationContent(
            // Screen에서 구독한 최신 NotificationUiState를 Content에 넘긴다.
            uiState = uiState,
            // 전달받은 markAllAsRead 메서드 참조를 그대로 연결한다. 지금 실행하는 것이 아니라 클릭 때 실행된다.
            onMarkAllAsRead = viewModel::markAllAsRead,
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
 * 책임: 상태를 알림 UI로 표시. 흐름: 타이틀+모두 읽음 처리 버튼 → 비었으면 EmptyState, 아니면 읽지 않은/읽은 섹션. 클릭은 콜백으로 전달한다.
 */
// NotificationContent 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
fun NotificationContent(
    // 렌더링에 필요한 알림 상태를 인자로 받는다. Content 내부에서 Repository를 조회하지 않는다.
    uiState: NotificationUiState,
    // onMarkAllAsRead는 인자 없이 실행하고 Unit을 반환하는 콜백 타입이다. 호출자가 실제 이벤트 처리를 제공한다.
    onMarkAllAsRead: () -> Unit,
    // 상위에서 크기·여백을 전달할 수 있는 Modifier 인자다. 생략하면 기본 Modifier를 쓴다.
    modifier: Modifier = Modifier
// 인자 선언을 닫고 함수 본문 또는 마지막 람다를 연다.
) {
    // 하위 UI를 세로 순서로 배치하는 레이아웃을 시작한다.
    Column(
        // 호출자가 전달한 Modifier를 보존하면서 추가 레이아웃 동작을 이어 붙인다.
        modifier = modifier
            // 부모의 허용 범위 안에서 최대 크기를 채운다.
            .fillMaxSize()
            // 재구성 사이에 유지되는 스크롤 상태로 Column 전체를 세로 스크롤 가능하게 만든다.
            .verticalScroll(rememberScrollState())
            // 본문 좌우에 24dp, 위아래에 16dp 여백을 추가한다.
            .padding(horizontal = 24.dp, vertical = 16.dp)
    // 인자 선언을 닫고 함수 본문 또는 마지막 람다를 연다.
    ) {
        // ── 타이틀 + 모두 읽음 처리 ──────────────────────
        // 타이틀과 버튼을 가로로 나란히, 양 끝으로 배치하는 Row를 시작한다.
        Row(
            // 자식들을 세로 중앙에 맞춘다.
            verticalAlignment = Alignment.CenterVertically,
            // 자식들을 양 끝으로 밀어 사이 공간을 벌린다.
            horizontalArrangement = Arrangement.SpaceBetween,
            // Row가 가로로 최대 너비를 채우도록 지정한다.
            modifier = Modifier.fillMaxWidth()
        // 인자 선언을 닫고 함수 본문 또는 마지막 람다를 연다.
        ) {
            // "알림" 타이틀을 그린다.
            Text(
                // 표시할 문자열을 지정한다.
                text = "알림",
                // 글자 크기를 18sp로 지정한다.
                fontSize = 18.sp,
                // 글자 굵기를 Bold로 지정한다.
                fontWeight = FontWeight.Bold,
                // 글자 색을 Gray900으로 지정한다.
                color = Gray900
            // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
            )
            // 공통 아웃라인 버튼 컴포넌트를 호출한다.
            OutlinedButton(
                // 버튼에 표시할 문구를 지정한다.
                text = "모두 읽음 처리",
                // 버튼 글자·아이콘 색을 Blue400으로 지정한다.
                contentColor = Blue400,
                // 클릭 시 실행할 콜백을 그대로 연결한다.
                onClick = onMarkAllAsRead
            // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
            )
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }

        // 타이틀 행과 아래 내용 사이에 16dp 여백을 넣는다. Spacer 대신 padding을 쓴 지점이라 실제로는 다음 요소의 top padding처럼 동작한다.
        Spacer(modifier = Modifier.padding(top = 16.dp))

        // 알림이 하나도 없으면(unread·read 모두 빈 목록) 이 분기로 들어간다.
        if (uiState.isEmpty) {
            // 알림이 없을 때 빈 상태
            // 빈 상태 안내 문구를 그린다.
            EmptyState(message = "알림이 없습니다")
        // 알림이 하나라도 있으면 이 분기로 들어간다.
        } else {
            // ── 읽지 않은 알림 섹션 ──────────────────────
            // 읽지 않은 알림이 있을 때만 섹션 전체를 그린다. 없으면 제목도 표시하지 않는다.
            if (uiState.unread.isNotEmpty()) {
                // 섹션 제목만 표시한다(더보기 콜백 없음).
                SectionHeader(title = "읽지 않은 알림")
                // 알림 카드들을 세로로 배치하고 10dp 간격을 둔다.
                Column(
                    // 카드 사이에 10dp 간격을 둔다.
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    // 섹션 제목과의 간격 10dp, 다음 섹션과의 간격 24dp를 준다.
                    modifier = Modifier.padding(top = 10.dp, bottom = 24.dp)
                // 인자 선언을 닫고 함수 본문 또는 마지막 람다를 연다.
                ) {
                    // 읽지 않은 알림 목록을 순회하며 각각 본문까지 보이는 행을 그린다(showContent 기본값 true).
                    uiState.unread.forEach { NotificationRow(notification = it) }
                // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
                }
            // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
            }
            // ── 읽은 알림 섹션 ──────────────────────────
            // 읽은 알림이 있을 때만 섹션 전체를 그린다.
            if (uiState.read.isNotEmpty()) {
                // 섹션 제목만 표시한다.
                SectionHeader(title = "읽은 알림")
                // 알림 카드들을 세로로 배치하고 10dp 간격을 둔다.
                Column(
                    // 카드 사이에 10dp 간격을 둔다.
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    // 섹션 제목과의 간격 10dp를 준다. 이 섹션이 마지막이라 하단 여백은 별도로 주지 않는다.
                    modifier = Modifier.padding(top = 10.dp)
                // 인자 선언을 닫고 함수 본문 또는 마지막 람다를 연다.
                ) {
                    // 읽은 알림 목록을 순회하며 각각 행을 그린다.
                    uiState.read.forEach { NotificationRow(notification = it) }
                // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
                }
            // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
            }
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
// 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
}

// Android Studio 미리보기의 배경과 390×900dp 크기를 지정한다. 실제 기기 실행 경로를 변경하지 않는다.
@Preview(showBackground = true, widthDp = 390, heightDp = 900)
// Compose UI를 구성하는 함수 표시다. 상태 변화로 다시 호출될 수 있다.
@Composable
/**
 * 책임: 샘플 알림 배치 미리보기. 흐름: 앱 테마 → 샘플 목록을 읽음/안읽음으로 나눈 상태 → 빈 콜백과 함께 Content 구성.
 */
// NotificationContentPreview 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
private fun NotificationContentPreview() {
    // 앱의 Compose 테마 아래에서 UI를 구성하여 프로젝트 스타일을 적용한다.
    AttendanceTheme {
        // 상태와 콜백으로만 그리는 Content를 호출한다. 아래 인자는 데이터와 이벤트 계약이다.
        NotificationContent(
            // Preview에 전달할 상태 객체를 직접 구성한다. ViewModel이나 Flow는 실행하지 않는다.
            uiState = NotificationUiState(
                // 샘플 알림 중 읽지 않은 것만 걸러 unread에 담는다.
                unread = SampleData.notifications.filter { !it.isRead },
                // 샘플 알림 중 읽은 것만 걸러 read에 담는다.
                read = SampleData.notifications.filter { it.isRead }
            // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
            ),
            // 미리보기에서 onMarkAllAsRead 이벤트가 실제 갱신을 하지 않도록 빈 람다를 전달한다.
            onMarkAllAsRead = {}
        // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
        )
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
// 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
}
```
