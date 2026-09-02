package com.example.attendance.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.attendance.core.model.ClassSession
import com.example.attendance.ui.theme.Blue50
import com.example.attendance.ui.theme.Gray900

/**
 * 세션 정보 배너
 *
 * NFC 인증 모달과 출석 결과 모달 하단에 공통으로 들어가는 연한 파랑 배경의 과목 정보 영역
 * (예: "운영체제 6527" / "10:30 - 11:50 | 공학관 305호")
 */
@Composable
fun SessionInfoBanner(
    session: ClassSession,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(Blue50, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // 과목명
        Text(
            text = session.subject,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Gray900
        )
        // 시간 | 강의실
        Text(
            text = "${session.timeRange} | ${session.room}",
            fontSize = 12.sp,
            color = Gray900
        )
    }
}
