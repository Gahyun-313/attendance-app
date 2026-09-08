# AttendanceScreen과 AttendanceContent 줄별 해설

[출석 체크 구조·문법·호출 흐름으로 돌아가기](README.md)

package/import와 기존 주석은 생략했다. 아래 코드의 각 실행 줄에 상세 설명을 붙였으며, 동작은 실제 소스와 같다. 테스트는 사용자가 직접 실행해 통과를 확인했다(VALIDATION.md 참고).

## AttendanceScreen.kt

원본: `app/src/main/java/com/example/attendance/feature/attendance/presentation/AttendanceScreen.kt`

```kotlin
// Compose UI를 구성하는 함수 표시다. 상태 변화로 다시 호출될 수 있다.
@Composable
/**
 * 책임: 상태 구독과 모달 표시 결정. 흐름: Factory로 ViewModel 확보 → 상태 수집 → isNfcScanning/result 값에 따라 다이얼로그 표시 → AttendanceContent에 상태·콜백 전달.
 */
// AttendanceScreen 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
fun AttendanceScreen(
    // 뒤로 가기 처리에 쓸 NavHostController를 인자로 받는다. 이 화면을 그래프에 등록하는 작업은 이번 범위에 없다.
    navController: NavHostController,
    // 호출자가 인스턴스를 주지 않으면 Factory로 현재 ViewModelStoreOwner의 ViewModel을 확보한다.
    viewModel: AttendanceViewModel = viewModel(factory = AttendanceViewModel.Factory)
// 인자 선언을 닫고 함수 본문 또는 마지막 람다를 연다.
) {
    // 기본 STARTED 이상에서 상태를 Compose State로 수집하고 by로 값을 읽는다. 값 변경은 이를 읽은 UI의 재구성을 유도한다.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // NFC 인증 모달 — 스캔 중일 때 표시
    // 아래 두 조건에서 반복 사용할 세션 값을 지역 변수로 꺼내 둔다.
    val session = uiState.session
    // 스캔 중이면서 대상 세션이 있을 때만 모달을 그린다. 둘 중 하나라도 아니면 이 블록 자체가 실행되지 않는다.
    if (uiState.isNfcScanning && session != null) {
        // 스캔 중임을 보여주는 다이얼로그를 그린다.
        NfcScanDialog(
            // 다이얼로그에 표시할 세션 정보를 전달한다.
            session = session,
            // 다이얼로그를 닫는 동작(뒤로가기, 바깥 클릭 등)을 cancelScan 메서드 참조에 연결한다.
            onDismiss = viewModel::cancelScan
        // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
        )
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }

    // 출석 결과 모달 — 도메인 상태(AttendanceStatus)를 UI 유형으로 매핑
    // 결과 값도 조건에서 반복 사용하므로 지역 변수로 꺼내 둔다. null이면 아직 결과가 없다는 뜻이다.
    val result = uiState.result
    // 결과가 있고 세션 정보도 있을 때만 결과 모달을 그린다.
    if (result != null && session != null) {
        // 출석 결과를 보여주는 다이얼로그를 그린다.
        AttendanceResultDialog(
            // 도메인 상태를 UI가 이해하는 결과 유형으로 변환해 전달한다. 아래 toResultType() 확장 함수가 매핑한다.
            type = result.toResultType(),
            // 다이얼로그에 표시할 세션 정보를 전달한다.
            session = session,
            // 처리 시각 문자열을 그대로 전달한다.
            timestamp = uiState.checkedAt,
            // 결과 모달을 닫는 동작을 dismissResult 메서드 참조에 연결한다.
            onDismiss = viewModel::dismissResult
        // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
        )
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }

    // 상태와 콜백으로만 그리는 Content를 호출한다. 아래 인자는 데이터와 이벤트 계약이다.
    AttendanceContent(
        // Screen에서 구독한 최신 AttendanceUiState를 Content에 넘긴다.
        uiState = uiState,
        // 뒤로가기 버튼을 누르면 NavHostController의 popBackStack을 호출하는 람다를 만들어 전달한다.
        onBack = { navController.popBackStack() },
        // 전달받은 startAttendance 메서드 참조를 그대로 연결한다. 지금 실행하는 것이 아니라 클릭 때 실행된다.
        onStartAttendance = viewModel::startAttendance
    // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
    )
// 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
}

/**
 * 책임: 도메인 출석 상태를 결과 모달 UI 유형으로 매핑. AttendanceStatus의 네 값을 모두 분기한다.
 */
// AttendanceStatus에 대한 확장 함수 toResultType을 선언한다. 반환 타입은 AttendanceResultType이고 본문은 when 식이다.
private fun AttendanceStatus.toResultType(): AttendanceResultType = when (this) {
    // 출석이면 성공 유형으로 매핑한다.
    AttendanceStatus.PRESENT -> AttendanceResultType.SUCCESS
    // 지각이면 지각 유형으로 매핑한다.
    AttendanceStatus.LATE -> AttendanceResultType.LATE
    // 결석이면 실패 유형으로 매핑한다.
    AttendanceStatus.ABSENT -> AttendanceResultType.FAIL
    // 미출석(아직 체크 전)도 실패 유형으로 매핑한다. 이 분기는 status가 PRESENT/LATE/ABSENT로 갱신된 뒤에만 result에 담기므로 실제로는 거의 도달하지 않는다.
    AttendanceStatus.NOT_YET -> AttendanceResultType.FAIL
// 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
}

// Compose UI를 구성하는 함수 표시다. 상태 변화로 다시 호출될 수 있다.
@Composable
/**
 * 책임: 상태를 출석 체크 UI로 표시. 흐름: 상단바 → 출석 상태 카드 → 안내 문구 → 출석 시작 버튼. 클릭은 콜백으로 전달하고 자체 Repository나 ViewModel을 만들지 않는다.
 */
// AttendanceContent 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
fun AttendanceContent(
    // 렌더링에 필요한 출석 체크 상태를 인자로 받는다. Content 내부에서 Repository를 조회하지 않는다.
    uiState: AttendanceUiState,
    // onBack은 인자 없이 실행하고 Unit을 반환하는 콜백 타입이다. 호출자가 실제 뒤로가기 처리를 제공한다.
    onBack: () -> Unit,
    // onStartAttendance는 인자 없이 실행하고 Unit을 반환하는 콜백 타입이다. 호출자가 실제 이벤트 처리를 제공한다.
    onStartAttendance: () -> Unit
// 인자 선언을 닫고 함수 본문 또는 마지막 람다를 연다.
) {
    // 상단바와 본문을 담을 Material 화면 틀을 구성한다.
    Scaffold(
        // Scaffold 배경에 프로젝트의 White 색상을 적용한다.
        containerColor = White,
        // 상단바 슬롯에 뒤로가기 버튼이 있는 공통 상단바를 그리는 Composable 람다를 전달한다.
        topBar = {
            // 제목과 뒤로가기 콜백을 전달해 공통 뒤로가기 상단바를 구성한다.
            BackTopBar(title = "출석 체크", onBack = onBack)
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
    // Scaffold가 본문에 제공하는 여백을 받는다. 상단바 등에 본문이 가리지 않도록 아래에서 적용한다.
    ) { paddingValues ->
        // 하위 UI를 세로 순서로 배치하는 레이아웃을 시작한다.
        Column(
            // Modifier 체인을 새로 시작한다.
            modifier = Modifier
                // 부모의 허용 범위 안에서 최대 크기를 채운다.
                .fillMaxSize()
                // Scaffold가 계산한 외부 여백을 적용한다.
                .padding(paddingValues)
                // 좌우 24dp, 위아래 20dp의 본문 여백을 추가한다.
                .padding(horizontal = 24.dp, vertical = 20.dp)
        // 인자 선언을 닫고 함수 본문 또는 마지막 람다를 연다.
        ) {
            // 섹션 제목
            // "출석 체크" 문구를 섹션 제목 스타일로 그린다.
            Text(
                // 표시할 문자열을 지정한다.
                text = "출석 체크",
                // 글자 크기를 16sp로 지정한다.
                fontSize = 16.sp,
                // 글자 굵기를 SemiBold로 지정한다.
                fontWeight = FontWeight.SemiBold,
                // 글자 색을 프로젝트의 Gray900으로 지정한다.
                color = Gray900
            // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
            )

            // 제목과 카드 사이에 14dp 간격을 두는 빈 공간을 넣는다.
            Spacer(modifier = Modifier.height(14.dp))

            // 오늘의 출석 상태 카드
            // 출석 상태를 보여줄 카드 컨테이너를 만든다.
            Card(
                // 모서리를 8dp 라운드로 지정한다.
                shape = RoundedCornerShape(8.dp),
                // 카드 배경색을 Gray50으로 지정한다.
                colors = CardDefaults.cardColors(containerColor = Gray50),
                // 1dp 두께의 Gray300 테두리를 지정한다.
                border = BorderStroke(1.dp, Gray300),
                // 카드가 가로로 최대 너비를 채우도록 지정한다.
                modifier = Modifier.fillMaxWidth()
            // 인자 선언을 닫고 함수 본문 또는 마지막 람다를 연다.
            ) {
                // 카드 내부 내용을 세로로 배치하고 8dp 간격을 둔다.
                Column(
                    // 세 Text 사이에 8dp 간격을 둔다.
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    // 카드 안쪽에 16dp 여백을 준다.
                    modifier = Modifier.padding(16.dp)
                // 인자 선언을 닫고 함수 본문 또는 마지막 람다를 연다.
                ) {
                    // 카드 상단의 작은 안내 라벨을 그린다.
                    Text(
                        // 표시할 문자열을 지정한다.
                        text = "오늘의 출석 상태",
                        // 작은 글자 크기 12sp를 지정한다.
                        fontSize = 12.sp,
                        // 보조 텍스트 색상 Gray500을 지정한다.
                        color = Gray500
                    // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
                    )
                    // 현재 출석 상태 (미출석 → 출석 체크 후 출석)
                    // 실제 상태 라벨을 크게 강조해서 그린다.
                    Text(
                        // AttendanceStatus enum이 갖고 있는 한글 라벨(예: "미출석", "출석")을 표시한다.
                        text = uiState.status.label,
                        // 강조를 위해 글자 크기를 18sp로 지정한다.
                        fontSize = 18.sp,
                        // 글자 굵기를 Bold로 지정한다.
                        fontWeight = FontWeight.Bold,
                        // 글자 색을 Gray900으로 지정한다.
                        color = Gray900
                    // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
                    )
                    // 카드 하단의 보조 설명 문구를 그린다.
                    Text(
                        // 고정 안내 문구를 표시한다. 실제 날짜·교시 계산 로직은 없다.
                        text = "2025년 1월 기준 · 1교시 미완료",
                        // 작은 글자 크기 11sp를 지정한다.
                        fontSize = 11.sp,
                        // 보조 텍스트 색상 Gray500을 지정한다.
                        color = Gray500
                    // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
                    )
                // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
                }
            // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
            }

            // 카드와 안내 문구 사이에 24dp 간격을 두는 빈 공간을 넣는다.
            Spacer(modifier = Modifier.height(24.dp))

            // 안내 문구
            // 사용자에게 다음 행동을 안내하는 문구를 그린다.
            Text(
                // 표시할 문자열을 지정한다.
                text = "출석 시작 버튼을 눌러 NFC 스캔을 시작하세요.",
                // 작은 글자 크기 12sp를 지정한다.
                fontSize = 12.sp,
                // 글자 색을 Gray900으로 지정한다.
                color = Gray900
            // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
            )

            // 안내 문구와 버튼 사이에 12dp 간격을 두는 빈 공간을 넣는다.
            Spacer(modifier = Modifier.height(12.dp))

            // 출석 시작 버튼 — 이미 출석했다면 비활성화 + 연한 색으로 전환
            // 현재 상태가 미출석일 때만 버튼을 누를 수 있다는 조건을 계산해 지역 변수로 둔다.
            val canStart = uiState.status == AttendanceStatus.NOT_YET
            // 공통 강조 버튼 컴포넌트를 호출한다.
            AccentButton(
                // 버튼에 표시할 문구를 지정한다.
                text = "출석 시작",
                // 클릭 시 실행할 콜백을 그대로 연결한다.
                onClick = onStartAttendance,
                // canStart가 true면 진한 Blue600, false면 연한 Blue200을 배경색으로 써서 상태를 시각적으로 구분한다.
                containerColor = if (canStart) Blue600 else Blue200,
                // canStart 값으로 버튼의 활성/비활성을 제어한다. false면 클릭이 막힌다.
                enabled = canStart,
                // Modifier 체인을 새로 시작한다.
                modifier = Modifier
                    // 버튼이 가로로 최대 너비를 채우도록 지정한다.
                    .fillMaxWidth()
                    // 버튼 높이를 44dp로 고정한다.
                    .height(44.dp)
            // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
            )
        // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
        }
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
// 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
}

// Android Studio 미리보기의 배경과 390×844dp(휴대폰 화면 비율) 크기를 지정한다. 실제 기기 실행 경로를 변경하지 않는다.
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
// Compose UI를 구성하는 함수 표시다. 상태 변화로 다시 호출될 수 있다.
@Composable
/**
 * 책임: 샘플 출석 체크 배치 미리보기. 흐름: 앱 테마 → 샘플 AttendanceUiState → 빈 콜백과 함께 Content 구성. 실데이터 수집과 이동은 검증하지 않는다.
 */
// AttendanceContentPreview 함수를 선언한다. 세부 책임과 호출 순서는 바로 위 설명과 본문을 함께 읽는다.
private fun AttendanceContentPreview() {
    // 앱의 Compose 테마 아래에서 UI를 구성하여 프로젝트 스타일을 적용한다.
    AttendanceTheme {
        // 상태와 콜백으로만 그리는 Content를 호출한다. 아래 인자는 데이터와 이벤트 계약이다.
        AttendanceContent(
            // Preview에 전달할 상태 객체를 직접 구성한다. ViewModel이나 Flow는 실행하지 않는다.
            uiState = AttendanceUiState(session = SampleData.todaySessions[1]),
            // 미리보기에서 onBack 이벤트가 실제 이동을 하지 않도록 빈 람다를 전달한다.
            onBack = {},
            // 미리보기에서 onStartAttendance 이벤트가 실제 스캔을 시작하지 않도록 빈 람다를 전달한다.
            onStartAttendance = {}
        // 앞에서 시작한 생성자 또는 함수 호출의 인자 목록을 닫는다.
        )
    // 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
    }
// 현재 클래스·함수·람다의 중괄호 블록을 닫는다. 들여쓰기로 어떤 범위가 끝나는지 확인한다.
}
```
