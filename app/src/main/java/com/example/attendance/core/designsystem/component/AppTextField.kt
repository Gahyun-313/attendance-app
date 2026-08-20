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
 * [ cornerRadius ]로 모서리 곡률 조절 - 로그인(16), 정정 요청(8)
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    cornerRadius: Dp = 16.dp,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
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