# MyPageScreen.kt 줄별 해설

실제 파일: `app/src/main/java/com/example/attendance/feature/mypage/presentation/MyPageScreen.kt`. package/import는 생략했다. 함수 위의 설명은 책임을, 각 줄 위의 주석은 실행 순서와 문법의 의미를 설명한다.

```kotlin
/**
 * 3-8의 정적 마이페이지 UI를 먼저 제공한다. 확인창은 화면 자체의 상태로 보관한다.
 * TODO(연습 과제): MyPageViewModel, AuthRepository.logout 호출, MyPageViewModelTest를 직접 작성한다.
 */
// Compose가 화면 구성 함수로 처리하도록 표시한다.
@Composable
// 도메인 ViewModel 없이 확인창 상태와 화면 이벤트를 연결한다.
fun MyPageScreen(
    // 현재는 확인 후 로그인 화면으로 이동하는 외부 콜백이다.
    onLogout: () -> Unit
// 함수의 매개변수 선언을 끝내고 처리 본문을 시작한다.
) {
    // 회전 시에도 확인창 표시 여부를 복원한다. 인증 상태가 아니다.
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    // 아직 없는 설정 화면의 준비 중 안내 제목이다.
    var notice by rememberSaveable { mutableStateOf<String?>(null) }
    // 로그아웃 메뉴를 눌렀을 때만 확인창을 구성한다.
    if (showLogoutDialog) {
        // 기존 공통 확인 다이얼로그를 재사용한다.
        ConfirmDialog(
            // 사용자에게 동작을 확인한다.
            title = "로그아웃 하시겠어요?",
            // 배경 메뉴와 구분되는 확인 버튼 이름이다.
            confirmText = "확인",
            // 확인창을 닫고 이동 콜백을 호출한다. Repository는 호출하지 않는다.
            onConfirm = { showLogoutDialog = false; onLogout() },
            // 취소·바깥 터치·뒤로가기는 확인창만 닫는다.
            onDismiss = { showLogoutDialog = false }
        // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
        )
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }
    // 설정 화면이 없는 항목은 무반응 대신 명시적인 안내를 보여준다.
    notice?.let { title ->
        // 프로필 관리·알림 설정 구현을 가장하지 않는 안내창이다.
        AlertDialog(
            // 안내창을 닫는다.
            onDismissRequest = { notice = null },
            // 어떤 메뉴인지 표시한다.
            title = { Text(title) },
            // 현재 지원 범위를 안내한다.
            text = { Text("준비 중인 기능입니다.") },
            // 닫기 버튼으로 안내 상태를 비운다.
            confirmButton = { TextButton(onClick = { notice = null }) { Text("닫기") } }
        // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
        )
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }
    // 표시와 상태 소유를 분리해 UI 테스트와 Preview를 쉽게 한다.
    MyPageContent(
        // 프로필 메뉴는 준비 중 안내로 연결한다.
        onProfile = { notice = "프로필 관리" },
        // 알림 목록과 알림 설정은 다른 기능이다.
        onNotificationSettings = { notice = "알림 설정" },
        // 메뉴 클릭은 곧바로 로그아웃하지 않고 확인창을 연다.
        onLogout = { showLogoutDialog = true }
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    )
// 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
}

/** 정적 프로필과 계정 메뉴를 그리고 사용자 이벤트를 외부로 전달한다. */
// Compose가 화면 구성 함수로 처리하도록 표시한다.
@Composable
// Repository나 Navigation에 의존하지 않는 표시 함수다.
fun MyPageContent(
    // 프로필 관리 메뉴 이벤트다.
    onProfile: () -> Unit,
    // 알림 설정 메뉴 이벤트다.
    onNotificationSettings: () -> Unit,
    // 로그아웃 확인 요청이다.
    onLogout: () -> Unit,
    // 3-8 가이드의 정적 프로필 기본값이다. 서버 사용자 정보가 아니다.
    studentName: String = "학생 이름",
    // 실제 학기를 추측해 표시하지 않는다.
    semester: String = "학기 정보 준비 중"
// 함수의 매개변수 선언을 끝내고 처리 본문을 시작한다.
) {
    // 상단 시스템 영역과 본문 여백을 처리한다. 하단 바는 AppNavGraph가 소유한다.
    Scaffold { padding ->
        // 세로 스크롤 가능한 프로필·메뉴 목록이다.
        Column(
            // 시스템 여백 뒤에 스크롤과 내부 여백을 적용한다.
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            // 항목 사이 간격이다.
            verticalArrangement = Arrangement.spacedBy(16.dp)
        // 함수의 매개변수 선언을 끝내고 처리 본문을 시작한다.
        ) {
            // 화면 제목이다.
            Text("마이페이지", fontWeight = FontWeight.Bold)
            // 프로필을 하나의 카드로 묶는다.
            Card {
                // 아바타와 정보를 나란히 배치한다.
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 실제 앱에 있는 마스코트 리소스를 사용한다.
                    Image(painterResource(R.drawable.attendance_character), contentDescription = "프로필 이미지", modifier = Modifier.size(64.dp))
                    // 이름과 학기 정보를 세로로 표시한다.
                    Column {
                        // 주입된 표시 이름이다.
                        Text(studentName, fontWeight = FontWeight.Bold)
                        // 학기 정보 표시 문자열이다.
                        Text(semester)
                    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
                    }
                // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
                }
            // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
            }
            // 설정 메뉴 묶음의 제목이다.
            Text("계정 설정", fontWeight = FontWeight.Bold)
            // 설정 메뉴 이벤트를 전달한다.
            OutlinedButton(text = "프로필 관리", onClick = onProfile)
            // 알림 환경설정 메뉴 이벤트를 전달한다.
            OutlinedButton(text = "알림 설정", onClick = onNotificationSettings)
            // 확인창을 여는 이벤트다.
            OutlinedButton(text = "로그아웃", onClick = onLogout)
        // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
        }
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }
// 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
}

/** ViewModel 없이 실제 마이페이지 UI를 미리 본다. */
// Android Studio Preview에서 이 함수를 화면 진입점으로 사용한다.
@Preview(showBackground = true)
// Compose가 화면 구성 함수로 처리하도록 표시한다.
@Composable
// Android Studio Preview 진입 함수다.
private fun MyPageScreenPreview() {
    // 앱 테마를 적용하고 이동만 생략한다.
    AttendanceTheme { MyPageScreen(onLogout = {}) }
// 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
}
```
