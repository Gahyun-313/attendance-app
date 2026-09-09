package com.example.attendance.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.attendance.core.designsystem.component.LogoTopBar
import com.example.attendance.core.designsystem.component.NotificationRow
import com.example.attendance.core.designsystem.component.RecentRecordRow
import com.example.attendance.core.designsystem.component.SectionHeader
import com.example.attendance.core.designsystem.component.SessionCard
import com.example.attendance.core.designsystem.component.StatItem
import com.example.attendance.core.designsystem.component.StatSummaryRow
import com.example.attendance.core.model.WeeklyStats
import com.example.attendance.core.model.SampleData
import com.example.attendance.ui.theme.AttendanceTheme
import com.example.attendance.ui.theme.Gray50

/**
 * 홈 화면 (Stateful)
 *
 * ViewModel 상태를 구독하고 화면 이동 요청을 호출자가 제공한 콜백으로 전달한다.
 * AppNavGraph가 이동 콜백을 제공하며 하단 탭은 그래프에서 관리한다.
 * 실제 UI는 [HomeContent]가 그린다.
 */
@Composable
fun HomeScreen(
    onCheckAttendance: () -> Unit,
    onMoreNotifications: () -> Unit,
    onMoreRecords: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    // 기본 STARTED 이상에서만 수집한다. 수집 중지와 ViewModel 제거는 다르다.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Gray50,
        topBar = { LogoTopBar() }
    ) { paddingValues ->
        HomeContent(
            uiState = uiState,
            onCheckAttendance = onCheckAttendance,
            onMoreNotifications = onMoreNotifications,
            onMoreRecords = onMoreRecords,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

/**
 * 홈 화면 UI (Stateless)
 *
 * 구성 (Figma 기준, 위에서부터):
 * 1. 이번 주 출석 현황 — 3칸 통계
 * 2. 오늘 수업 세션 — 세션 카드 목록 (진행 중 세션에 출석 체크 버튼)
 * 3. 읽지 않은 알림 — 최근 2건 + 더보기
 * 4. 최근 출석 기록 + 더보기
 */
@Composable
fun HomeContent(
    uiState: HomeUiState,
    onCheckAttendance: () -> Unit,
    onMoreNotifications: () -> Unit,
    onMoreRecords: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(30.dp),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // 내용이 길어지면 스크롤
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        // ── 1. 이번 주 출석 현황 ──────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader(title = "이번 주 출석 현황")
            StatSummaryRow(
                items = listOf(
                    StatItem(
                        value = "${uiState.weeklyStats.attendedCount} / ${uiState.weeklyStats.totalCount}",
                        label = "이번 주 출석"
                    ),
                    StatItem(
                        value = "${uiState.weeklyStats.attendanceRate}%",
                        label = "활성 기간 출석률"
                    ),
                    StatItem(
                        value = "${uiState.weeklyStats.remainingSessions}회",
                        label = "오늘 남은 세션"
                    ),
                )
            )
        }

        // ── 2. 오늘 수업 세션 ────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            SectionHeader(title = "오늘 수업 세션")
            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                uiState.todaySessions.forEach { session ->
                    SessionCard(
                        session = session,
                        onCheckAttendance = onCheckAttendance
                    )
                }
            }
        }

        // ── 3. 읽지 않은 알림 ────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader(
                title = "읽지 않은 알림",
                onMore = onMoreNotifications
            )
            uiState.unreadNotifications.forEach { notification ->
                NotificationRow(notification = notification, showContent = false)
            }
        }

        // ── 4. 최근 출석 기록 ────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader(
                title = "최근 출석 기록",
                onMore = onMoreRecords
            )
            uiState.recentRecords.forEach { record ->
                RecentRecordRow(record = record)
            }
        }
    }
}

/** Repository를 실행하지 않고 샘플 상태로 홈 콘텐츠 배치를 확인한다. */
@Preview(showBackground = true, widthDp = 390, heightDp = 1200)
@Composable
private fun HomeContentPreview() {
    AttendanceTheme {
        HomeContent(
            uiState = HomeUiState(
                weeklyStats = WeeklyStats(3, 10, 55, 2),
                todaySessions = SampleData.todaySessions,
                unreadNotifications = SampleData.notifications.filter { !it.isRead }.take(2),
                recentRecords = SampleData.recentRecords
            ),
            onCheckAttendance = {},
            onMoreNotifications = {},
            onMoreRecords = {}
        )
    }
}
