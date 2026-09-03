package com.example.attendance.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.attendance.core.designsystem.component.LogoTopBar
import com.example.attendance.core.designsystem.component.NotificationRow
import com.example.attendance.core.designsystem.component.RecentRecordRow
import com.example.attendance.core.designsystem.component.SectionHeader
import com.example.attendance.core.designsystem.component.SessionCard
import com.example.attendance.core.designsystem.component.StatItem
import com.example.attendance.core.designsystem.component.StatSummaryRow
import com.example.attendance.core.model.SampleData
import com.example.attendance.ui.theme.AttendanceTheme
import com.example.attendance.ui.theme.Gray50

/**
 * 데이터 연동 전 홈 화면 초안
 * 추후 HomeScreen/HomeContent 구조로 교체 예정.
 */
@Composable
fun HomePrototypeScreen(modifier: Modifier = Modifier) {
    Scaffold(
        containerColor = Gray50,
        topBar = { LogoTopBar() },
        modifier = modifier
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.spacedBy(30.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(title = "이번 주 출석 현황")
                StatSummaryRow(
                    items = listOf(
                        StatItem(value = "3 / 10", label = "이번 주 출석"),
                        StatItem(value = "55%", label = "활성 기간 출석률"),
                        StatItem(value = "2회", label = "오늘 남은 세션")
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                SectionHeader(title = "오늘 수업 세션")
                Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                    SampleData.todaySessions.forEach { session ->
                        SessionCard(
                            session = session,
                            onCheckAttendance = {}
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(title = "읽지 않은 알림")
                SampleData.notifications
                    .filter { !it.isRead }
                    .take(2)
                    .forEach { notification ->
                        NotificationRow(
                            notification = notification,
                            showContent = false
                        )
                    }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(title = "최근 출석 기록")
                SampleData.recentRecords.forEach { record ->
                    RecentRecordRow(record = record)
                }
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 390, heightDp = 1200)
@Composable
private fun HomePrototypeScreenPreview() {
    AttendanceTheme {
        HomePrototypeScreen()
    }
}