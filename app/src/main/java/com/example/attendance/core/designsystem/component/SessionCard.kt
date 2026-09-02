package com.example.attendance.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.attendance.core.model.ClassSession
import com.example.attendance.ui.theme.Blue200
import com.example.attendance.ui.theme.Blue50
import com.example.attendance.ui.theme.Blue600
import com.example.attendance.ui.theme.Gray900

/**
 * 오늘 수업 세션 카드 (홈 화면)
 *
 * - 연한 파랑 배경 카드에 과목명/시간을 표시한다.
 * - 현재 출석 체크가 가능한 세션([ClassSession.isActive])이면
초록 점 표시와 함께 오른쪽에 '출석 체크' 버튼이 노출된다.
 */
@Composable
fun SessionCard(
    session: ClassSession,
    modifier: Modifier = Modifier,
    onCheckAttendance: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Blue50),
        border = BorderStroke(1.dp, Blue200),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp)
        ) {
            // 왼쪽: 과목명 + 시간
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = session.subject,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray900
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 진행 중 세션에는 초록 점으로 강조
                    if (session.isActive) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF22C55E), CircleShape)
                        )
                    }
                    Text(
                        text = session.timeRange,
                        fontSize = 12.sp,
                        color = Gray900
                    )
                }
            }
            // 오른쪽: 출석 체크 버튼 (진행 중 세션에만 노출)
            if (session.isActive) {
                AccentButton(
                    text = "출석 체크",
                    onClick = onCheckAttendance,
                    containerColor = Blue600
                )
            }
        }
    }
}
