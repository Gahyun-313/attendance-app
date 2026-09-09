# 정정 요청 화면 줄별 해설

## 책임과 실행 흐름

StateFlow → collectAsStateWithLifecycle → CorrectionContent → 입력·제출 콜백 → ViewModel → 새 상태. 성공이면 Screen의 LaunchedEffect가 onSubmitted를 호출한다.

실제 파일: `app/src/main/java/com/example/attendance/feature/history/presentation/CorrectionRequestScreen.kt`. package/import는 생략하고 나머지 코드의 실행 순서와 동작을 유지했다. 실제 소스의 주석은 주요 책임 중심이며, 아래는 학습을 위한 줄별 해설이다.

```kotlin
/**
 * 상태를 수집하고 입력·제출 이벤트를 ViewModel에 전달한다.
 * 뒤로 이동과 접수 완료의 처리는 AppNavGraph가 제공한 콜백에 위임한다.
 */
// Compose가 UI 구성 함수로 처리하도록 선언한다.
@Composable
// 상태를 소유한 ViewModel과 화면 콘텐츠를 연결하는 Stateful Screen이다.
fun CorrectionRequestScreen(
    // 뒤로가기 및 취소 동작이다. AppNavGraph가 popBackStack을 연결한다.
    onBack: () -> Unit,
    // 접수 성공 후 호출할 동작이며 이 화면이 이동 목적지를 정하지 않는다.
    onSubmitted: () -> Unit,
    // 기본적으로 현재 소유자의 ViewModelStore와 수동 Factory를 사용한다.
    viewModel: CorrectionViewModel = viewModel(factory = CorrectionViewModel.Factory)
// 매개변수 선언을 마치고 이 함수의 본문을 시작한다.
) {
    // 생명주기가 STARTED 이상일 때 상태를 수집하고 Compose State로 읽는다.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // 재구성마다 알림 표시 상태가 새로 만들어지지 않도록 기억한다.
    val snackbarHostState = remember { SnackbarHostState() }
    // 효과를 재시작하지 않고 최신 완료 콜백을 참조한다.
    val currentOnSubmitted by rememberUpdatedState(onSubmitted)

    // 사유 오류 값이 바뀔 때 실행하며 같은 값의 재구성에서는 재실행하지 않는다.
    LaunchedEffect(uiState.isReasonError) {
        // 오류가 켜질 때 스낵바로 알린다. Content의 오류 문구도 함께 남는다.
        if (uiState.isReasonError) snackbarHostState.showSnackbar("정정 사유를 입력해주세요.")
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }
    // false에서 true로 바뀌면 성공 처리를 실행한다.
    LaunchedEffect(uiState.isSubmitted) {
        // 실제 화면 이동은 외부 콜백이 담당한다. 성공한 목적지는 AppNavGraph에서 popBackStack으로 제거한다.
        if (uiState.isSubmitted) currentOnSubmitted()
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }
    // 상태와 함수 참조를 내려 Content를 ViewModel 없이도 렌더링할 수 있게 한다.
    CorrectionContent(
        // 이번에 수집한 화면 상태를 전달한다.
        uiState = uiState,
        // Screen이 만든 스낵바 상태를 Scaffold에 연결한다.
        snackbarHostState = snackbarHostState,
        // 외부 취소 동작을 그대로 전달한다.
        onBack = onBack,
        // 함수 참조로 사유 이벤트를 ViewModel에 연결한다.
        onReasonChange = viewModel::onReasonChange,
        // 함수 참조로 상세 입력 이벤트를 연결한다.
        onDetailChange = viewModel::onDetailChange,
        // 제출 이벤트가 검증 로직을 거치게 한다.
        onSubmit = viewModel::submit
    // 앞에서 지정한 생성자 또는 함수 호출의 인자 목록을 닫는다.
    )
// 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
}

/** 대상 카드·입력·오류·버튼을 표시한다. 입력의 실제 상태 변경은 외부 콜백이 담당한다. */
// Compose가 UI 구성 함수로 처리하도록 선언한다.
@Composable
// 입력 상태를 직접 소유하지 않는 Stateless Content라 Preview와 UI 테스트에서 독립적으로 사용할 수 있다.
fun CorrectionContent(
    // 렌더링할 현재 상태다.
    uiState: CorrectionUiState,
    // 일시적인 안내를 표시할 호스트 상태다.
    snackbarHostState: SnackbarHostState,
    // 뒤로가기와 취소 버튼이 호출한다.
    onBack: () -> Unit,
    // 새 사유 문자열을 위로 전달한다.
    onReasonChange: (String) -> Unit,
    // 새 상세 문자열을 위로 전달한다.
    onDetailChange: (String) -> Unit,
    // 제출 클릭을 위로 전달한다.
    onSubmit: () -> Unit
// 매개변수 선언을 마치고 이 함수의 본문을 시작한다.
) {
    // 상단 바·스낵바·본문의 배치를 구성한다.
    Scaffold(
        // 화면 배경을 흰색으로 지정한다.
        containerColor = White,
        // 기존 공통 상단 바를 재사용한다.
        topBar = { BackTopBar(title = "출석 정정 요청", onBack = onBack) },
        // Screen의 스낵바 요청이 표시되는 자리다.
        snackbarHost = { SnackbarHost(snackbarHostState) }
    // 상단 바와 시스템 영역을 고려한 본문 여백을 받는다.
    ) { paddingValues ->
        // 폼의 항목을 위에서 아래로 배치한다.
        Column(
            // 아래 순서대로 크기·여백·스크롤 속성을 적용한다.
            modifier = Modifier
                // 사용 가능한 본문 영역을 채운다.
                .fillMaxSize()
                // Scaffold가 예약한 영역을 피한다.
                .padding(paddingValues)
                // 작은 화면에서도 하단 제출 버튼까지 스크롤할 수 있게 한다.
                .verticalScroll(rememberScrollState())
                // 폼 내부 여백을 둔다.
                .padding(horizontal = 24.dp, vertical = 16.dp),
            // 항목 사이의 기본 간격을 통일한다.
            verticalArrangement = Arrangement.spacedBy(12.dp)
        // 매개변수 선언을 마치고 이 함수의 본문을 시작한다.
        ) {
            // 대상 카드 위 제목이다.
            FieldLabel("요청 대상")
            // 조회 중과 기록 없음 상태를 구분한다.
            if (uiState.isLoading) Text("기록을 불러오는 중입니다.")
            // nullable 기록이 있을 때만 카드 UI를 구성한다.
            uiState.record?.let { record ->
                // 정정 대상 정보를 테두리 있는 카드로 묶는다.
                Card(
                    // 폼 가로 폭을 채운다.
                    modifier = Modifier.fillMaxWidth(),
                    // 모서리를 둥글게 한다.
                    shape = RoundedCornerShape(8.dp),
                    // 대상 강조 배경색이다.
                    colors = CardDefaults.cardColors(containerColor = Red50),
                    // 카드 테두리 두께와 색을 지정한다.
                    border = BorderStroke(1.dp, Red300)
                // 매개변수 선언을 마치고 이 함수의 본문을 시작한다.
                ) {
                    // 카드 안쪽 여백과 줄 간격을 지정한다.
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // 모델의 과목명과 보조 설명을 표시한다.
                        Text("${record.subject} ${record.detail}", fontWeight = FontWeight.Bold, color = Blue600)
                        // 출석 상태의 기존 라벨과 색을 재사용한다.
                        Text(record.status.label, color = record.status.color)
                        // 현재 모델에는 일(dayOfMonth)만 있어 임의의 연월·수업 시간을 만들지 않는다.
                        // 실제 모델이 제공하는 날짜 정보만 표시한다.
                        Text("기록일: ${record.dayOfMonth}일", fontSize = 12.sp)
                    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
                    }
                // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
                }
            // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
            }
            // 조회·제출 오류가 있으면 지속적으로 보여준다.
            uiState.errorMessage?.let { Text(it, color = Red900) }
            // 필수 입력 항목의 제목이다.
            FieldLabel("정정 사유")
            // 기존 공통 입력 필드를 사용해 스타일을 일치시킨다.
            AppTextField(
                // ViewModel 또는 테스트가 돌려준 최신 입력값이다.
                value = uiState.reason,
                // 입력 이벤트를 외부로 전달한다.
                onValueChange = onReasonChange,
                // 빈 입력일 때 예시를 표시한다.
                placeholder = "예) NFC 오류",
                // 필수 입력 오류 시 테두리를 강조한다.
                isError = uiState.isReasonError,
                // 정정 폼의 모서리 스타일을 적용한다.
                cornerRadius = 8.dp
            // 앞에서 지정한 생성자 또는 함수 호출의 인자 목록을 닫는다.
            )
            // 반복 제출 시 스낵바가 재실행되지 않더라도 오류를 확인할 수 있다.
            if (uiState.isReasonError) Text("정정 사유를 입력해주세요.", color = Red900)
            // 선택 입력 항목의 제목이다.
            FieldLabel("상세 내용")
            // 여러 줄을 허용하는 상세 설명 입력이다.
            AppTextField(
                // 상세 입력의 현재 값을 표시한다.
                value = uiState.detail,
                // 새 값을 상태 소유자에게 전달한다.
                onValueChange = onDetailChange,
                // 입력 예시를 안내한다.
                placeholder = "정정이 필요한 사유를 자세히 적어주세요.",
                // 줄바꿈을 허용한다.
                singleLine = false,
                // 최소 다섯 줄의 입력 영역을 확보한다.
                minLines = 5,
                // 사유 필드와 모서리 스타일을 맞춘다.
                cornerRadius = 8.dp
            // 앞에서 지정한 생성자 또는 함수 호출의 인자 목록을 닫는다.
            )
            // 가이드의 첨부 영역을 배치한다.
            FieldLabel("증빙 파일 첨부")
            // 현재 단계에서는 파일 선택·업로드가 구현되지 않았음을 화면에 알린다.
            Text("파일 첨부 기능은 준비 중입니다.", fontSize = 12.sp, color = Gray500)
            // TODO: 후속 단계에서 파일 피커와 업로드를 연결한다.
            // 가이드의 배치용 버튼이다. 현재는 파일 선택 동작이 없다.
            OutlinedGrayButton(text = "파일 선택", onClick = {})
            // 제출 영역 앞에 여백을 더한다.
            Spacer(Modifier.height(8.dp))
            // 취소와 제출 버튼을 나란히 배치한다.
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // 남은 가로 공간의 절반을 쓰고 외부 취소 콜백을 실행한다.
                NeutralButton(text = "취소", onClick = onBack, modifier = Modifier.weight(1f))
                // 주요 행동인 제출 버튼이다.
                AccentButton(
                    // 진행 상태를 버튼 문구에 반영한다.
                    text = if (uiState.isSubmitting) "제출 중..." else "제출",
                    // 내부에서는 검증하지 않고 ViewModel 콜백에 맡긴다.
                    onClick = onSubmit,
                    // 공통 강조색을 적용한다.
                    containerColor = Blue600,
                    // 대상 부재·조회·제출·완료 상태에서는 클릭을 막는다. 빈 사유는 클릭 후 검증한다.
                    enabled = uiState.record != null && !uiState.isLoading && !uiState.isSubmitting && !uiState.isSubmitted,
                    // 취소 버튼과 같은 가로 비율을 사용한다.
                    modifier = Modifier.weight(1f)
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

/** 폼 항목 제목의 글자 크기와 색을 일관되게 적용한다. */
// Compose가 UI 구성 함수로 처리하도록 선언한다.
@Composable
// 이 파일 안에서만 사용하는 작은 표시 함수다.
private fun FieldLabel(text: String) {
    // 전달받은 제목을 공통 스타일로 그린다.
    Text(text = text, fontSize = 12.sp, color = Gray500)
// 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
}

/** ViewModel·Navigation 없이 샘플 상태로 정정 폼 배치를 확인한다. */
// Android Studio에서 사용할 미리보기의 배경과 크기를 지정한다.
@Preview(showBackground = true, widthDp = 390, heightDp = 900)
// Compose가 UI 구성 함수로 처리하도록 선언한다.
@Composable
// Android Studio Preview의 진입 함수다.
private fun CorrectionContentPreview() {
    // 앱의 테마를 적용한다.
    AttendanceTheme {
        // 실제 Content를 직접 호출해 화면 배치를 재사용한다.
        CorrectionContent(
            // 기록이 로드된 상태로 미리보기를 구성한다.
            uiState = CorrectionUiState(record = SampleData.historyRecords[1], isLoading = false),
            // Preview 재구성에서도 스낵바 상태를 유지한다.
            snackbarHostState = remember { SnackbarHostState() },
            // 정적 Preview이므로 외부 동작은 빈 람다로 제공한다.
            onBack = {}, onReasonChange = {}, onDetailChange = {}, onSubmit = {}
        // 앞에서 지정한 생성자 또는 함수 호출의 인자 목록을 닫는다.
        )
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }
// 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
}
```
