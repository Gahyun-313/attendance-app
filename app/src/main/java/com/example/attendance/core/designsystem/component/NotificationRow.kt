package com.example.attendance.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.attendance.core.model.NotificationItem
import com.example.attendance.ui.theme.Gray50
import com.example.attendance.ui.theme.Gray300
import com.example.attendance.ui.theme.Gray500
import com.example.attendance.ui.theme.Gray900

/**
 * 알림 한 줄 (홈 화면 '읽지 않은 알림' 요약 / 알림 화면 '알림 목록' 공통)
 *
 * 연회색 배경 카드에 제목 + 오른쪽 상대 시간을 기본으로 표시하고,
 * [showContent]로 본문 노출 여부와 텍스트 스타일을 조정한다.
 *
 * @param notification 표시할 알림 정보
 * @param showContent  본문 표시 여부
 *                      - true(기본, 알림 화면): 제목 13sp/SemiBold + 본문 11sp + 시간 Gray500
 *                      - false(홈 화면 요약): 제목 12sp만 표시(본문 생략) + 시간 Gray900
 */
@Composable
fun NotificationRow(
    notification: NotificationItem,
    modifier: Modifier = Modifier,
    showContent: Boolean = true
) {
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Gray50),
        border = BorderStroke(1.dp, Gray300),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = if (showContent) Alignment.Top else Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp)
        ) {
            if (showContent) {
                // 알림 화면: 제목(굵게) + 본문
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray900
                    )
                    Text(
                        text = notification.content,
                        fontSize = 11.sp,
                        color = Gray900
                    )
                }
                Text(
                    text = notification.timeAgo,
                    fontSize = 11.sp,
                    color = Gray500
                )
            } else {
                // 홈 화면 요약: 제목만 간략히
                Text(
                    text = notification.title,
                    fontSize = 12.sp,
                    color = Gray900,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = notification.timeAgo,
                    fontSize = 11.sp,
                    color = Gray900
                )
            }
        }
    }
}
