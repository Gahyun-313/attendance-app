package com.example.attendance.feature.attendance.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.attendance.core.designsystem.component.AccentButton
import com.example.attendance.core.designsystem.component.AttendanceResultDialog
import com.example.attendance.core.designsystem.component.AttendanceResultType
import com.example.attendance.core.designsystem.component.BackTopBar
import com.example.attendance.core.designsystem.component.NfcScanDialog
import com.example.attendance.core.model.AttendanceStatus
import com.example.attendance.core.model.SampleData
import com.example.attendance.ui.theme.AttendanceTheme
import com.example.attendance.ui.theme.Blue200
import com.example.attendance.ui.theme.Blue600
import com.example.attendance.ui.theme.Gray50
import com.example.attendance.ui.theme.Gray300
import com.example.attendance.ui.theme.White
import com.example.attendance.ui.theme.Gray900
import com.example.attendance.ui.theme.Gray500

/**
 * 출석 체크 화면 (Stateful)
 *
 * ViewModel이 '출석 시작 → NFC 스캔 → 결과' 플로우 상태를 관리하고,
 * 이 Composable은 상태에 따라 모달을 띄우는 역할만 한다.
 */
@Composable
fun AttendanceScreen(
    navController: NavHostController,
    viewModel: AttendanceViewModel = viewModel(factory = AttendanceViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // NFC 인증 모달 — 스캔 중일 때 표시
    val session = uiState.session
    if (uiState.isNfcScanning && session != null) {
        NfcScanDialog(
            session = session,
            onDismiss = viewModel::cancelScan
        )
    }

    // 출석 결과 모달 — 도메인 상태(AttendanceStatus)를 UI 유형으로 매핑
    val result = uiState.result
    if (result != null && session != null) {
        AttendanceResultDialog(
            type = result.toResultType(),
            session = session,
            timestamp = uiState.checkedAt,
            onDismiss = viewModel::dismissResult
        )
    }

    AttendanceContent(
        uiState = uiState,
        onBack = { navController.popBackStack() },
        onStartAttendance = viewModel::startAttendance
    )
}

/** 도메인 출석 상태 → 결과 모달 UI 유형 매핑 */
private fun AttendanceStatus.toResultType(): AttendanceResultType = when (this) {
    AttendanceStatus.PRESENT -> AttendanceResultType.SUCCESS
    AttendanceStatus.LATE -> AttendanceResultType.LATE
    AttendanceStatus.ABSENT -> AttendanceResultType.FAIL
    AttendanceStatus.NOT_YET -> AttendanceResultType.FAIL
}

/**
 * 출석 체크 화면 UI (Stateless)
 *
 * 구성: 오늘의 출석 상태 카드 + 안내 문구 + 출석 시작 버튼
 */
@Composable
fun AttendanceContent(
    uiState: AttendanceUiState,
    onBack: () -> Unit,
    onStartAttendance: () -> Unit
) {
    Scaffold(
        containerColor = White,
        topBar = {
            BackTopBar(title = "출석 체크", onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // 섹션 제목
            Text(
                text = "출석 체크",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray900
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 오늘의 출석 상태 카드
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Gray50),
                border = BorderStroke(1.dp, Gray300),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "오늘의 출석 상태",
                        fontSize = 12.sp,
                        color = Gray500
                    )
                    // 현재 출석 상태 (미출석 → 출석 체크 후 출석)
                    Text(
                        text = uiState.status.label,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    Text(
                        text = "2025년 1월 기준 · 1교시 미완료",
                        fontSize = 11.sp,
                        color = Gray500
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 안내 문구
            Text(
                text = "출석 시작 버튼을 눌러 NFC 스캔을 시작하세요.",
                fontSize = 12.sp,
                color = Gray900
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 출석 시작 버튼 — 이미 출석했다면 비활성화 + 연한 색으로 전환
            val canStart = uiState.status == AttendanceStatus.NOT_YET
            AccentButton(
                text = "출석 시작",
                onClick = onStartAttendance,
                containerColor = if (canStart) Blue600 else Blue200,
                enabled = canStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AttendanceContentPreview() {
    AttendanceTheme {
        AttendanceContent(
            uiState = AttendanceUiState(session = SampleData.todaySessions[1]),
            onBack = {},
            onStartAttendance = {}
        )
    }
}
