package com.example.attendance.feature.auth.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.attendance.R
import com.example.attendance.core.designsystem.component.AppTextField
import com.example.attendance.core.designsystem.component.LoginButton
import com.example.attendance.ui.theme.AttendanceTheme
import com.example.attendance.ui.theme.Red900
import com.example.attendance.ui.theme.Blue600
import com.example.attendance.ui.theme.Gray900

/**
 * 로그인 화면 (Stateful)
 *
 * ViewModel의 상태를 구독하고 이벤트를 전달하는 역할만 하고,
 * 실제 UI는 LoginContent 함수가 그린다. 이동 목적지는 호출자가 결정한다.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    // 기본적으로 STARTED 이상에서 상태를 수집하며, 중지된 화면의 수집은 멈춘다.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 같은 성공 값으로 재구성될 때는 재실행하지 않는다. 화면 재진입은 별도 정책이 필요하다.
    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) onLoginSuccess()
    }

    LoginContent(
        uiState = uiState,
        onStudentIdChange = viewModel::onStudentIdChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::login
    )
}

/**
 * 로그인 화면 UI (Stateless)
 * 전달받은 상태만 표시하고 입력 이벤트를 콜백으로 올려 Preview와 UI 테스트를 독립시킨다.
 */
@Composable
fun LoginContent(
    uiState: LoginUiState,
    onStudentIdChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp)
    ) {
        // 캐릭터 로고
        Image(
            painter = painterResource(id = R.drawable.attendance_character),
            contentDescription = "출석하자 캐릭터",
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        // 앱 타이틀
        Text(
            text = "출석하자",
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            color = Blue600
        )
        Spacer(modifier = Modifier.height(10.dp))

        // 안내 문구
        Text(
            text = "학번과 비밀번호로 로그인하세요",
            fontSize = 12.sp,
            color = Gray900
        )
        Spacer(modifier = Modifier.height(40.dp))

        // 학번 입력
        AppTextField(
            value = uiState.studentId,
            onValueChange = onStudentIdChange,
            placeholder = "학번",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(15.dp))

        // 비밀번호 입력 (마스킹)
        AppTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            placeholder = "비밀번호",
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        // 로그인 실패 안내
        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = uiState.errorMessage,
                fontSize = 12.sp,
                color = Red900
            )
        }
        Spacer(modifier = Modifier.height(30.dp))

        // 로그인 버튼 - 요청 중에는 중복 클릭 방지
        LoginButton(
            text = if (uiState.isLoading) "로그인 중..." else "로그인",
            onClick = onLoginClick,
            enabled = !uiState.isLoading
        )
    }
}

/** ViewModel이나 Repository 없이 초기 로그인 화면의 배치를 확인한다. */
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginContentPreview() {
    AttendanceTheme {
        LoginContent(
            uiState = LoginUiState(),
            onStudentIdChange = {},
            onPasswordChange = {},
            onLoginClick = {}
        )
    }
}
