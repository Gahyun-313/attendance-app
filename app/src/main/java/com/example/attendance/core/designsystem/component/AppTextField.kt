package com.example.attendance.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.attendance.ui.theme.Blue400
import com.example.attendance.ui.theme.Red900
import com.example.attendance.ui.theme.Gray400
import com.example.attendance.ui.theme.White
import com.example.attendance.ui.theme.Gray500

/**
 * 공통 텍스트 입력 필드
 *
 * - 흰 배경 + 회색 테두리
 * - [ cornerRadius ]로 모서리 곡률 조절 - 로그인(16), 정정 요청(8)
 */
@Composable
fun AppTextField(
    value: String,                      // 현재 입력값
    onValueChange: (String) -> Unit,    // 입력 변경 콜백
    placeholder: String,                // 힌트 텍스트
    modifier: Modifier = Modifier,
    isError: Boolean = false,           // 에러 상태 (사유 미입력 등)
    singleLine: Boolean = true,         // 한 줄 입력 여부
    minLines: Int = 1,                  // 여러 줄 입력 시 최소 줄 수
    cornerRadius: Dp = 16.dp,           // 모서리 곡률
    visualTransformation: VisualTransformation = VisualTransformation.None, // 비밀번호 마스킹 등 표시 변환
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default  // 키보드 타입/IME 옵션
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(text = placeholder, fontSize = 14.sp, color = Gray500)
        },
        isError = isError,
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(cornerRadius),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            // 흰 배경 유지 + 포커스 시 액센트 컬러 테두리
            focusedContainerColor = White,
            unfocusedContainerColor = White,
            errorContainerColor = White,
            focusedBorderColor = Blue400,
            unfocusedBorderColor = Gray400,
            errorBorderColor = Red900
        ),
        modifier = modifier.fillMaxWidth()
    )
}