# 로그인 UI 줄별 해설

[구조·문법·호출 흐름으로 돌아가기](README.md)

package/import와 기존 설명 주석은 생략하고, 실행 코드는 원본 순서 그대로 유지했다. 닫는 괄호까지 각 줄 위에 설명을 붙였다. 실제 파일의 대체본이 아니라 함께 읽는 학습용 코드다.

## LoginScreen.kt

원본: `app/src/main/java/com/example/attendance/feature/auth/presentation/LoginScreen.kt`

```kotlin
// Compose에서 UI를 구성하는 함수임을 표시한다. 재구성 과정에서 다시 호출될 수 있다.
@Composable
/**
 * 책임: 상태 구독과 성공 콜백 처리. 흐름: ViewModel 확보 → 생명주기 기반 수집 → 성공 효과 → Content에 상태/이벤트 전달.
 */
// LoginScreen 함수를 선언한다. 아래 인자와 본문이 이 UI의 계약과 표시 동작을 정의한다.
fun LoginScreen(
    // onLoginSuccess 콜백을 받는다. 인자 없이 실행하고 Unit을 반환하는 함수 타입이다.
    onLoginSuccess: () -> Unit,
    // 호출자가 생략하면 Factory로 현재 소유자의 ViewModel을 확보한다. 기존 인스턴스는 재사용한다.
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
// 인자 목록을 닫고 본문 또는 마지막 람다 블록을 시작한다.
) {
    // 기본 STARTED 이상에서 StateFlow를 Compose State로 수집하고 by로 값을 읽는다. 화면 중지와 ViewModel 제거는 다르다.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // 컴포지션 진입 또는 성공 값 변경 시 효과를 실행한다. 동일 key의 단순 재구성에서는 다시 실행하지 않는다.
    LaunchedEffect(uiState.isLoginSuccess) {
        // 성공일 때만 외부 콜백을 호출한다. 여기서 직접 NavController를 호출하지 않는다.
        if (uiState.isLoginSuccess) onLoginSuccess()
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
    // 순수 표시를 담당하는 Content를 호출한다. 아래 인자로 상태와 이벤트 콜백을 전달한다.
    LoginContent(
        // Screen이 구독한 현재 상태를 Content로 전달한다.
        uiState = uiState,
        // 학번 변경 메서드 참조를 넘긴다. 지금 실행하는 것이 아니라 사용자 입력 시 실행된다.
        onStudentIdChange = viewModel::onStudentIdChange,
        // 비밀번호 변경 메서드 참조를 넘겨 UI 이벤트가 ViewModel로 전달되도록 한다.
        onPasswordChange = viewModel::onPasswordChange,
        // 로그인 메서드 참조를 버튼 콜백에 연결한다. 괄호를 붙여 즉시 호출하는 코드와 다르다.
        onLoginClick = viewModel::login
    // 앞에서 시작한 인자 목록 또는 주 생성자의 소괄호를 닫는다.
    )
// 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
}
// Compose에서 UI를 구성하는 함수임을 표시한다. 재구성 과정에서 다시 호출될 수 있다.
@Composable
/**
 * 책임: 상태를 UI로 표현. 흐름: 로고/안내 → 학번/비밀번호 입력 → 조건부 오류 → 로딩에 따른 버튼. 요청은 직접 실행하지 않고 콜백으로 올린다.
 */
// LoginContent 함수를 선언한다. 아래 인자와 본문이 이 UI의 계약과 표시 동작을 정의한다.
fun LoginContent(
    // 화면을 그리는 데 필요한 불변 상태를 함수 인자로 받는다.
    uiState: LoginUiState,
    // onStudentIdChange 콜백을 받는다. 문자열을 받고 Unit을 반환하는 함수 타입이다.
    onStudentIdChange: (String) -> Unit,
    // onPasswordChange 콜백을 받는다. 문자열을 받고 Unit을 반환하는 함수 타입이다.
    onPasswordChange: (String) -> Unit,
    // onLoginClick 콜백을 받는다. 인자 없이 실행하고 Unit을 반환하는 함수 타입이다.
    onLoginClick: () -> Unit
// 인자 목록을 닫고 본문 또는 마지막 람다 블록을 시작한다.
) {
    // 자식 Composable들을 세로로 배치하는 레이아웃을 만든다.
    Column(
        // Column의 자식을 가로 방향 중앙에 맞춘다.
        horizontalAlignment = Alignment.CenterHorizontally,
        // 세로 방향의 남는 공간 안에서 자식 묶음을 중앙 배치한다.
        verticalArrangement = Arrangement.Center,
        // 크기와 여백을 순서대로 적용할 Modifier 체인을 시작한다.
        modifier = Modifier
            // 부모가 허용한 최대 크기를 채운다.
            .fillMaxSize()
            // 좌우에 각각 30dp의 내부 여백을 둔다. dp는 화면 밀도를 고려한 레이아웃 단위다.
            .padding(horizontal = 30.dp)
    // 인자 목록을 닫고 본문 또는 마지막 람다 블록을 시작한다.
    ) {
        // 그림 리소스를 화면에 표시하는 Composable을 호출한다.
        Image(
            // 이 구현 저장소에 있는 attendance_character 리소스를 Painter로 읽는다. 가이드의 다른 리소스 이름을 그대로 쓰지 않는다.
            painter = painterResource(id = R.drawable.attendance_character),
            // 이미지의 의미를 보조 기술에 전달할 접근성 설명을 지정한다.
            contentDescription = "출석하자 캐릭터",
            // 이미지의 가로와 세로 크기를 각각 100dp로 정한다.
            modifier = Modifier.size(100.dp)
        // 앞에서 시작한 인자 목록 또는 주 생성자의 소괄호를 닫는다.
        )
        // 20.dp만큼 세로 빈 공간을 넣어 인접 UI 요소를 구분한다.
        Spacer(modifier = Modifier.height(20.dp))
        // 아래에서 지정할 문자열과 글꼴 속성으로 텍스트를 그린다.
        Text(
            // 화면에 표시할 고정 문자열을 text라는 이름 붙인 인자로 전달한다.
            text = "출석하자",
            // 30.sp로 글자 크기를 지정한다. sp는 사용자의 글꼴 크기 설정을 반영한다.
            fontSize = 30.sp,
            // 글자 굵기를 SemiBold로 지정해 제목을 강조한다.
            fontWeight = FontWeight.SemiBold,
            // Blue600 테마 색상을 텍스트에 적용한다.
            color = Blue600
        // 앞에서 시작한 인자 목록 또는 주 생성자의 소괄호를 닫는다.
        )
        // 10.dp만큼 세로 빈 공간을 넣어 인접 UI 요소를 구분한다.
        Spacer(modifier = Modifier.height(10.dp))
        // 아래에서 지정할 문자열과 글꼴 속성으로 텍스트를 그린다.
        Text(
            // 화면에 표시할 고정 문자열을 text라는 이름 붙인 인자로 전달한다.
            text = "학번과 비밀번호로 로그인하세요",
            // 12.sp로 글자 크기를 지정한다. sp는 사용자의 글꼴 크기 설정을 반영한다.
            fontSize = 12.sp,
            // Gray900 테마 색상을 텍스트에 적용한다.
            color = Gray900
        // 앞에서 시작한 인자 목록 또는 주 생성자의 소괄호를 닫는다.
        )
        // 40.dp만큼 세로 빈 공간을 넣어 인접 UI 요소를 구분한다.
        Spacer(modifier = Modifier.height(40.dp))
        // 공통 입력 컴포넌트를 호출한다. 내부 OutlinedTextField에 값과 입력 콜백이 전달된다.
        AppTextField(
            // 학번 입력창에 현재 상태의 학번을 표시한다. 입력창이 별도 학번 상태를 소유하지 않는다.
            value = uiState.studentId,
            // 새 학번 문자열을 상위에서 받은 콜백에 전달한다.
            onValueChange = onStudentIdChange,
            // 입력이 비어 있을 때 표시할 안내 문자열을 지정한다. 입력값 자체를 설정하는 것은 아니다.
            placeholder = "학번",
            // 숫자 키보드를 요청한다. 붙여넣기 등을 포함한 숫자 유효성 검증을 대신하지 않는다.
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        // 앞에서 시작한 인자 목록 또는 주 생성자의 소괄호를 닫는다.
        )
        // 15.dp만큼 세로 빈 공간을 넣어 인접 UI 요소를 구분한다.
        Spacer(modifier = Modifier.height(15.dp))
        // 공통 입력 컴포넌트를 호출한다. 내부 OutlinedTextField에 값과 입력 콜백이 전달된다.
        AppTextField(
            // 비밀번호 원문을 입력값으로 전달한다. 표시할 때만 별도의 변환으로 가린다.
            value = uiState.password,
            // 새 비밀번호 문자열을 상위에서 받은 콜백에 전달한다.
            onValueChange = onPasswordChange,
            // 입력이 비어 있을 때 표시할 안내 문자열을 지정한다. 입력값 자체를 설정하는 것은 아니다.
            placeholder = "비밀번호",
            // 화면의 비밀번호 글자를 가린다. 상태에 보관된 문자열을 암호화하지 않는다.
            visualTransformation = PasswordVisualTransformation(),
            // 비밀번호 입력에 맞는 키보드 옵션을 요청한다.
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        // 앞에서 시작한 인자 목록 또는 주 생성자의 소괄호를 닫는다.
        )
        // 오류가 있을 때만 안내 UI를 구성한다. null 검사 뒤 이 불변 프로퍼티를 문자열로 사용할 수 있다.
        if (uiState.errorMessage != null) {
            // 10.dp만큼 세로 빈 공간을 넣어 인접 UI 요소를 구분한다.
            Spacer(modifier = Modifier.height(10.dp))
            // 아래에서 지정할 문자열과 글꼴 속성으로 텍스트를 그린다.
            Text(
                // 상태에 담긴 실패 메시지를 화면에 표시한다.
                text = uiState.errorMessage,
                // 12.sp로 글자 크기를 지정한다. sp는 사용자의 글꼴 크기 설정을 반영한다.
                fontSize = 12.sp,
                // Red900 테마 색상을 텍스트에 적용한다.
                color = Red900
            // 앞에서 시작한 인자 목록 또는 주 생성자의 소괄호를 닫는다.
            )
        // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
        }
        // 30.dp만큼 세로 빈 공간을 넣어 인접 UI 요소를 구분한다.
        Spacer(modifier = Modifier.height(30.dp))
        // 공통 로그인 버튼을 호출한다. 내부 Material Button이 enabled와 onClick을 사용한다.
        LoginButton(
            // if 식의 결과를 문구로 사용한다. 요청 중인지에 따라 사용자에게 다른 문구를 보여 준다.
            text = if (uiState.isLoading) "로그인 중..." else "로그인",
            // 버튼 클릭 이벤트를 전달받은 로그인 콜백에 연결한다.
            onClick = onLoginClick,
            // Boolean 부정으로 로딩 중에는 버튼을 비활성화한다. ViewModel의 중복 방어도 별도로 유지한다.
            enabled = !uiState.isLoading
        // 앞에서 시작한 인자 목록 또는 주 생성자의 소괄호를 닫는다.
        )
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
// 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
}
// Android Studio Preview에 표시할 배경과 390×844dp 크기를 지정한다. 앱의 실행 경로에는 연결하지 않는다.
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
// Compose에서 UI를 구성하는 함수임을 표시한다. 재구성 과정에서 다시 호출될 수 있다.
@Composable
/**
 * 책임: 초기 UI 미리보기. 흐름: 테마 적용 → 기본 상태와 빈 콜백으로 Content 호출. ViewModel을 생성하지 않는다.
 */
// LoginContentPreview 함수를 선언한다. 아래 인자와 본문이 이 UI의 계약과 표시 동작을 정의한다.
private fun LoginContentPreview() {
    // 앱의 Compose 테마 아래에서 UI를 구성한다. 색상·타이포그래피 환경을 제공한다.
    AttendanceTheme {
        // 순수 표시를 담당하는 Content를 호출한다. 아래 인자로 상태와 이벤트 콜백을 전달한다.
        LoginContent(
            // 기본값으로 초기 상태를 만들어 주입한다. 이 코드 자체는 서버를 호출하지 않는다.
            uiState = LoginUiState(),
            // 이 시나리오에서는 학번 변경을 처리하지 않는 빈 람다를 전달한다.
            onStudentIdChange = {},
            // 이 시나리오에서는 비밀번호 변경을 처리하지 않는 빈 람다를 전달한다.
            onPasswordChange = {},
            // 로그인 클릭에 부수효과가 없는 빈 람다를 전달한다.
            onLoginClick = {}
        // 앞에서 시작한 인자 목록 또는 주 생성자의 소괄호를 닫는다.
        )
    // 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
    }
// 현재 중괄호 블록을 닫는다. 해당 함수·클래스·람다의 범위가 여기서 끝난다.
}
```
