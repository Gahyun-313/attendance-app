package com.example.attendance.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton as Material3OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.attendance.ui.theme.AttendanceTheme
import com.example.attendance.ui.theme.Blue200
import com.example.attendance.ui.theme.Blue600
import com.example.attendance.ui.theme.Gray300
import com.example.attendance.ui.theme.Gray400
import com.example.attendance.ui.theme.Gray900
import com.example.attendance.ui.theme.Red300

/**
 * 공통 버튼 모음
 *
 * [ LoginButton ] : 로그인 버튼
 * [ AccentButton ] : 액센트 버튼
 * [ NeutralButton ] : 회색 버튼 (취소)
 * [ OutlinedButton ] : 테두리 흰색 버튼 (정정 요청, 모두 읽음 처리)
 * [ OutlinedGrayButton ] : 테두리 회색 버튼 (파일 선택)
 */

/**
 * 로그인 버튼
 */
@Composable
fun LoginButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Blue600),
        modifier = modifier
            .fillMaxWidth()
            .height(51.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * 액센트 버튼 (메인 액션) : 호출부에서 배경색 지정
 * 가변형일 경우 before/after 색을, 불변형일 경우 고정 색을 넘긴다.
 */
@Composable
fun AccentButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 회색 버튼 (보조 액션)
 */
@Composable
fun NeutralButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Gray300,
            contentColor = Gray900
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 테두리 + 흰색 버튼 (ex. 정정 요청, 모두 읽음 처리)
 */
@Composable
fun OutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = Gray900
) {
    Material3OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, Gray400),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 테두리 + 회색 버튼 (ex. 파일 선택)
 */
@Composable
fun OutlinedGrayButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = Gray900
) {
    Material3OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, Gray400),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppButtonPreview() {
    AttendanceTheme {
        Column {
            LoginButton(text = "로그인", onClick = {})
            AccentButton(text = "출석 체크", onClick = {}, containerColor = Blue600)
            AccentButton(text = "제출", onClick = {}, containerColor = Blue200)
            AccentButton(text = "로그아웃", onClick = {}, containerColor = Red300)
            NeutralButton(text = "취소", onClick = {})
            OutlinedButton(text = "정정 요청", onClick = {})
            OutlinedGrayButton(text = "파일 선택", onClick = {})
        }
    }
}