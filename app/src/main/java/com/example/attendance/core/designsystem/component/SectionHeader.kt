package com.example.attendance.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.example.attendance.ui.theme.Gray500

/**
 * 섹션 제목 헤더
 *
 * 홈 화면의 "이번 주 출석 현황", "오늘 수업 세션" 등
 * 회색 SemiBold 14sp 제목 스타일을 통일해서 사용한다.
 */
@Composable
fun SectionHeader (
    title: String,
    modifier: Modifier = Modifier,
    onMore: (() -> Unit)? = null
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Gray500
        )
        // 더보기 링크
        if (onMore != null) {
            Text(
                text = "더보기",
                fontSize = 14.sp,
                color = Gray500,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onMore() }
            )
        }
    }
}