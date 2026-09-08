package com.example.attendance.feature.notification.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.attendance.core.designsystem.component.BackTopBar
import com.example.attendance.core.designsystem.component.EmptyState
import com.example.attendance.core.designsystem.component.NotificationRow
import com.example.attendance.core.designsystem.component.OutlinedButton
import com.example.attendance.core.designsystem.component.SectionHeader
import com.example.attendance.core.model.SampleData
import com.example.attendance.ui.theme.Blue400
import com.example.attendance.ui.theme.AttendanceTheme
import com.example.attendance.ui.theme.Gray50
import com.example.attendance.ui.theme.Gray900

/**
 * 알림 화면 (Stateful)
 *
 * ViewModel이 알림 목록을 읽음/안읽음으로 분류해 제공한다.
 * '모두 읽음 처리'는 Repository까지 반영되므로 홈 화면에도 동기화된다.
 */
@Composable
fun NotificationScreen(
    navController: NavHostController,
    viewModel: NotificationViewModel = viewModel(factory = NotificationViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Gray50,
        topBar = {
            BackTopBar(title = "알림", onBack = { navController.popBackStack() })
        }
    ) { paddingValues ->
        NotificationContent(
            uiState = uiState,
            onMarkAllAsRead = viewModel::markAllAsRead,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

/**
 * 알림 화면 UI (Stateless)
 *
 * 구성: "알림" 타이틀 + 모두 읽음 처리 버튼,
 * 읽지 않은 알림 / 읽은 알림 섹션, 빈 상태 처리
 */
@Composable
fun NotificationContent(
    uiState: NotificationUiState,
    onMarkAllAsRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // ── 타이틀 + 모두 읽음 처리 ──────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "알림",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
            OutlinedButton(
                text = "모두 읽음 처리",
                contentColor = Blue400,
                onClick = onMarkAllAsRead
            )
        }

        Spacer(modifier = Modifier.padding(top = 16.dp))

        if (uiState.isEmpty) {
            // 알림이 없을 때 빈 상태
            EmptyState(message = "알림이 없습니다")
        } else {
            // ── 읽지 않은 알림 섹션 ──────────────────────
            if (uiState.unread.isNotEmpty()) {
                SectionHeader(title = "읽지 않은 알림")
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 10.dp, bottom = 24.dp)
                ) {
                    uiState.unread.forEach { NotificationRow(notification = it) }
                }
            }
            // ── 읽은 알림 섹션 ──────────────────────────
            if (uiState.read.isNotEmpty()) {
                SectionHeader(title = "읽은 알림")
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    uiState.read.forEach { NotificationRow(notification = it) }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun NotificationContentPreview() {
    AttendanceTheme {
        NotificationContent(
            uiState = NotificationUiState(
                unread = SampleData.notifications.filter { !it.isRead },
                read = SampleData.notifications.filter { it.isRead }
            ),
            onMarkAllAsRead = {}
        )
    }
}
