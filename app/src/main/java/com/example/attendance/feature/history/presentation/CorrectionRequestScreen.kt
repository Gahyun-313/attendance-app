package com.example.attendance.feature.history.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.attendance.core.designsystem.component.AccentButton
import com.example.attendance.core.designsystem.component.AppTextField
import com.example.attendance.core.designsystem.component.BackTopBar
import com.example.attendance.core.designsystem.component.NeutralButton
import com.example.attendance.core.designsystem.component.OutlinedGrayButton
import com.example.attendance.core.model.SampleData
import com.example.attendance.ui.theme.AttendanceTheme
import com.example.attendance.ui.theme.Blue600
import com.example.attendance.ui.theme.Gray500
import com.example.attendance.ui.theme.Red50
import com.example.attendance.ui.theme.Red300
import com.example.attendance.ui.theme.Red900
import com.example.attendance.ui.theme.White

/**
 * 상태를 수집하고 입력·제출 이벤트를 ViewModel에 전달한다.
 * 뒤로 이동과 접수 완료의 처리는 AppNavGraph가 제공한 콜백에 위임한다.
 */
@Composable
fun CorrectionRequestScreen(
    onBack: () -> Unit,
    onSubmitted: () -> Unit,
    viewModel: CorrectionViewModel = viewModel(factory = CorrectionViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnSubmitted by rememberUpdatedState(onSubmitted)

    LaunchedEffect(uiState.isReasonError) {
        if (uiState.isReasonError) snackbarHostState.showSnackbar("정정 사유를 입력해주세요.")
    }
    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) currentOnSubmitted()
    }
    CorrectionContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onReasonChange = viewModel::onReasonChange,
        onDetailChange = viewModel::onDetailChange,
        onSubmit = viewModel::submit
    )
}

/** 대상 카드·입력·오류·버튼을 표시한다. 입력의 실제 상태 변경은 외부 콜백이 담당한다. */
@Composable
fun CorrectionContent(
    uiState: CorrectionUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onReasonChange: (String) -> Unit,
    onDetailChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Scaffold(
        containerColor = White,
        topBar = { BackTopBar(title = "출석 정정 요청", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FieldLabel("요청 대상")
            if (uiState.isLoading) Text("기록을 불러오는 중입니다.")
            uiState.record?.let { record ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Red50),
                    border = BorderStroke(1.dp, Red300)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${record.subject} ${record.detail}", fontWeight = FontWeight.Bold, color = Blue600)
                        Text(record.status.label, color = record.status.color)
                        // 현재 모델에는 일(dayOfMonth)만 있어 임의의 연월·수업 시간을 만들지 않는다.
                        Text("기록일: ${record.dayOfMonth}일", fontSize = 12.sp)
                    }
                }
            }
            uiState.errorMessage?.let { Text(it, color = Red900) }
            FieldLabel("정정 사유")
            AppTextField(
                value = uiState.reason,
                onValueChange = onReasonChange,
                placeholder = "예) NFC 오류",
                isError = uiState.isReasonError,
                cornerRadius = 8.dp
            )
            if (uiState.isReasonError) Text("정정 사유를 입력해주세요.", color = Red900)
            FieldLabel("상세 내용")
            AppTextField(
                value = uiState.detail,
                onValueChange = onDetailChange,
                placeholder = "정정이 필요한 사유를 자세히 적어주세요.",
                singleLine = false,
                minLines = 5,
                cornerRadius = 8.dp
            )
            FieldLabel("증빙 파일 첨부")
            Text("파일 첨부 기능은 준비 중입니다.", fontSize = 12.sp, color = Gray500)
            // TODO: 후속 단계에서 파일 피커와 업로드를 연결한다.
            OutlinedGrayButton(text = "파일 선택", onClick = {})
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NeutralButton(text = "취소", onClick = onBack, modifier = Modifier.weight(1f))
                AccentButton(
                    text = if (uiState.isSubmitting) "제출 중..." else "제출",
                    onClick = onSubmit,
                    containerColor = Blue600,
                    enabled = uiState.record != null && !uiState.isLoading && !uiState.isSubmitting && !uiState.isSubmitted,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** 폼 항목 제목의 글자 크기와 색을 일관되게 적용한다. */
@Composable
private fun FieldLabel(text: String) {
    Text(text = text, fontSize = 12.sp, color = Gray500)
}

/** ViewModel·Navigation 없이 샘플 상태로 정정 폼 배치를 확인한다. */
@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun CorrectionContentPreview() {
    AttendanceTheme {
        CorrectionContent(
            uiState = CorrectionUiState(record = SampleData.historyRecords[1], isLoading = false),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {}, onReasonChange = {}, onDetailChange = {}, onSubmit = {}
        )
    }
}
