package com.example.attendance.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.attendance.ui.theme.Gray900

/**
 * 통계 요약 한 항목 (값 + 라벨)
 */
data class StatItem (
    val value: String,
    val label: String
)

/**
 * 통계 요약 행
 *
 * 홈 화면의 "이번 주 출석 현황'과 내 출석 기록 화면의 "이번 학기 출석 현황'에서 공용으로 사용
 */
@Composable
fun StatSymmmaryRow(
    items: List<StatItem>,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        items.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Text(
                    text = item.label,
                    fontSize = 12.sp,
                    color = Gray900
                )
            }
        }
    }
}