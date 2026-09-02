package com.example.attendance.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.attendance.R
import com.example.attendance.ui.theme.Blue400
import com.example.attendance.ui.theme.Gray300
import com.example.attendance.ui.theme.White
import com.example.attendance.ui.theme.Gray900

/**
 * 공통 상단 바 모듬
 *
 * - [ BackTopBar ] : 뒤로가기 + 중앙 타이틀
 * - [ LogoTopBar ] : 로고 + 앱 이름 (홈화면)
 */

/**
 * BackTopBar
 */
@Composable
fun BackTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(White)
        ) {
            // 왼쪽 뒤로가기 버튼
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Gray900,
                    modifier = Modifier.size(18.dp)
                )
            }
            // 중앙 타이틀
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray900,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        // 상단 바와 본문 구분선
        HorizontalDivider(color = Gray300, thickness = 1.dp)
    }
}

/**
 * LogoTopBar
 */
@Composable
fun LogoTopBar(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 20.dp)
    ) {
        // 로고 이미지
        Image(painter = painterResource(id = R.drawable.attendance_character),
            contentDescription = "출석하자 로고",
            modifier = Modifier.size(30.dp))

        Text(
            text = "출석하자",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Blue400
        )
    }
}