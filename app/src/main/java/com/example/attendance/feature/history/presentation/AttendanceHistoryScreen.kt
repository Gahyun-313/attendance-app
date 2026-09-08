package com.example.attendance.feature.history.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.attendance.core.designsystem.component.HistoryRecordRow
import com.example.attendance.core.designsystem.component.SectionHeader
import com.example.attendance.core.designsystem.component.StatItem
import com.example.attendance.core.designsystem.component.StatSummaryRow
import com.example.attendance.core.model.SampleData
import com.example.attendance.core.model.SemesterStats
import com.example.attendance.ui.theme.AttendanceTheme
import com.example.attendance.ui.theme.Gray300
import com.example.attendance.ui.theme.Gray50
import com.example.attendance.ui.theme.Gray500
import com.example.attendance.ui.theme.Gray900
import java.time.LocalDate
import java.time.YearMonth

/** ViewModel 상태를 수집하고 실제 UI는 [HistoryContent]에 위임한다. */
@Composable
fun AttendanceHistoryScreen(
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryContent(
        uiState = uiState,
        onPrevMonth = viewModel::moveToPreviousMonth,
        onNextMonth = viewModel::moveToNextMonth,
        onDayClick = viewModel::selectDay
    )
}

/** 통계, 달력, 선택 날짜의 기록 목록을 표시하는 상태 없는 콘텐츠 함수. */
@Composable
fun HistoryContent(
    uiState: HistoryUiState,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text("내 출석 기록", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray900)
        Spacer(Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Gray50),
            border = BorderStroke(1.dp, Gray300),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("이번 학기 출석 현황", fontSize = 12.sp, color = Gray500)
                Spacer(Modifier.height(12.dp))
                StatSummaryRow(
                    items = listOf(
                        StatItem("${uiState.stats.presentCount}", "출석"),
                        StatItem("${uiState.stats.lateCount}", "지각"),
                        StatItem("${uiState.stats.absentCount}", "결석"),
                        StatItem("${uiState.stats.attendanceRate}%", "출석률")
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        MonthCalendar(uiState.currentMonth, uiState.selectedDay, uiState.recordDays, onPrevMonth, onNextMonth, onDayClick)
        Spacer(Modifier.height(20.dp))
        SectionHeader(title = "날짜별 기록")
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            uiState.records.forEach { record -> HistoryRecordRow(record = record) }
        }
    }
}

/** 월의 시작 요일을 기준으로 날짜를 배치하고 기록일과 선택일을 시각적으로 구분한다. */
@Composable
private fun MonthCalendar(
    month: YearMonth,
    selectedDay: Int?,
    recordDays: Set<Int>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Gray50),
        border = BorderStroke(1.dp, Gray300),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onPrevMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "이전 달", tint = Gray900)
                }
                Text("${month.year}년 ${month.monthValue}월", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold, color = Gray900)
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "다음 달", tint = Gray900)
                }
            }
            Row(Modifier.fillMaxWidth()) {
                listOf("일", "월", "화", "수", "목", "금", "토").forEach { dayName ->
                    Text(dayName, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = Gray500)
                }
            }
            Spacer(Modifier.height(8.dp))

            val firstDayOffset = LocalDate.of(month.year, month.monthValue, 1).dayOfWeek.value % 7
            val weeks = (firstDayOffset + month.lengthOfMonth() + 6) / 7
            repeat(weeks) { week ->
                Row(Modifier.fillMaxWidth()) {
                    repeat(7) { dayOfWeek ->
                        val day = week * 7 + dayOfWeek - firstDayOffset + 1
                        Box(Modifier.weight(1f).height(36.dp), contentAlignment = Alignment.Center) {
                            if (day in 1..month.lengthOfMonth()) {
                                val selected = day == selectedDay
                                val hasRecord = day in recordDays
                                Box(
                                    Modifier
                                        .size(30.dp)
                                        .background(
                                            when {
                                                selected -> Gray500
                                                hasRecord -> Gray300
                                                else -> Color.Transparent
                                            },
                                            CircleShape
                                        )
                                        .clickable { onDayClick(day) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(day.toString(), fontSize = 12.sp, color = if (selected) Color.White else Gray900)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1100)
@Composable
private fun HistoryContentPreview() {
    AttendanceTheme {
        HistoryContent(
            uiState = HistoryUiState(
                stats = SemesterStats(22, 3, 1, 88),
                recordDays = SampleData.historyRecords.map { it.dayOfMonth }.toSet(),
                records = SampleData.historyRecords
            ),
            onPrevMonth = {}, onNextMonth = {}, onDayClick = {}
        )
    }
}
