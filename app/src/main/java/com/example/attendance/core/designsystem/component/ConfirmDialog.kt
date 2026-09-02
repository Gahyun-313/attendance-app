package com.example.attendance.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.attendance.ui.theme.Gray900
import com.example.attendance.ui.theme.Gray500
import com.example.attendance.ui.theme.Red300

/**
 * 공통 확인 다이얼로그
 *
 * 로그아웃 등 사용자의 확인이 필요한 액션에 사용한다.
 * 제목 + (선택) 설명 + 취소/확인 버튼으로 구성된다.
 *
 * @param title       다이얼로그 제목 (예: "로그아웃 하시겠어요?")
 * @param message     보조 설명 (없으면 미표시)
 * @param confirmText 확인 버튼 텍스트 (예: "로그아웃")
 * @param confirmColor 확인 버튼 색 (기본값: 살몬 [Red300])
 * @param onConfirm   확인 클릭 콜백
 * @param onDismiss   취소/닫기 콜백
 */
@Composable
fun ConfirmDialog(
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    message: String? = null,
    confirmColor: Color = Red300
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // 제목
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray900
                )
                // 보조 설명 (옵션)
                if (message != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        fontSize = 13.sp,
                        color = Gray500
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 취소 / 확인 버튼 (동일 너비)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NeutralButton(
                        text = "취소",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    AccentButton(
                        text = confirmText,
                        onClick = onConfirm,
                        containerColor = confirmColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
