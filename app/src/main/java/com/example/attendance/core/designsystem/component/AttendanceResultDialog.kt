package com.example.attendance.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.attendance.core.model.ClassSession
import com.example.attendance.core.model.SampleData
import com.example.attendance.ui.theme.AttendanceTheme
import com.example.attendance.ui.theme.Red900
import com.example.attendance.ui.theme.Orange300
import com.example.attendance.ui.theme.Blue400
import com.example.attendance.ui.theme.Gray900
import com.example.attendance.ui.theme.Gray500

/**
 * 출석 결과 유형
 *
 * Figma의 결과 모달 4종(출석 완료/지각 출석/출석 실패/이미 출석)을 표현한다.
 *
 * @property title     모달 제목
 * @property iconColor 아이콘 원형 배경색
 * @property isSuccess 체크 아이콘(성공 계열) 여부 — false면 X 아이콘
 */
enum class AttendanceResultType(
    val title: String,
    val iconColor: Color,
    val isSuccess: Boolean
) {
    SUCCESS(title = "출석 완료", iconColor = Blue400, isSuccess = true),
    LATE(title = "지각 출석", iconColor = Orange300, isSuccess = true),
    FAIL(title = "출석 실패", iconColor = Red900, isSuccess = false),
    ALREADY(title = "이미 출석", iconColor = Orange300, isSuccess = true),
}

/**
 * 출석 결과 모달
 *
 * 가운데 상태 아이콘(체크/X), 제목, 처리 시각, 세션 정보 배너로 구성된다.
 * 오른쪽 상단 X 버튼으로 닫는다.
 *
 * @param type      결과 유형 (완료/지각/실패/이미 출석)
 * @param session   결과가 발생한 세션 정보
 * @param timestamp 처리 시각 문자열 (예: "2026-04-15 09:23:45")
 * @param onDismiss 모달 닫기 콜백
 */
@Composable
fun AttendanceResultDialog(
    type: AttendanceResultType,
    session: ClassSession,
    timestamp: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                // 오른쪽 상단 닫기(X) 버튼
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = Gray900
                        )
                    }
                }

                // 상태 아이콘 (색상 원 + 체크/X)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .background(type.iconColor, CircleShape)
                ) {
                    Icon(
                        imageVector = if (type.isSuccess) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = type.title,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 제목 (출석 완료 / 지각 출석 / ...)
                Text(
                    text = type.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray900
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 처리 시각
                Text(
                    text = timestamp,
                    fontSize = 12.sp,
                    color = Gray500
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 하단 세션 정보 배너 (공통 컴포넌트 재사용)
                SessionInfoBanner(session = session)
            }
        }
    }
}

@Preview
@Composable
private fun AttendanceResultDialogPreview() {
    AttendanceTheme {
        AttendanceResultDialog(
            type = AttendanceResultType.SUCCESS,
            session = SampleData.todaySessions[1],
            timestamp = "2026-04-15 09:23:45",
            onDismiss = {}
        )
    }
}
