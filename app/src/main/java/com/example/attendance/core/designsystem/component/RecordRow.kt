package com.example.attendance.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.attendance.core.model.AttendanceRecord
import com.example.attendance.core.model.AttendanceStatus
import com.example.attendance.ui.theme.Gray50
import com.example.attendance.ui.theme.Gray300
import com.example.attendance.ui.theme.Red50
import com.example.attendance.ui.theme.Red300
import com.example.attendance.ui.theme.Gray900
import com.example.attendance.ui.theme.Gray500

/**
 * 출석 기록 행 컴포넌트 모음
 * - [RecentRecordRow]  : 홈 화면 '최근 출석 기록' 행
 * - [HistoryRecordRow] : 내 출석 기록 화면 '날짜별 기록' 행
 */

/** 홈 화면용 최근 출석 기록 행 — "과목 · n교시" + 오른쪽 상태/시각 */
@Composable
fun RecentRecordRow(
    record: AttendanceRecord,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Gray50),
        border = BorderStroke(1.dp, Gray300),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 13.dp, vertical = 8.dp)
        ) {
            // 왼쪽: 과목 · 교시
            Text(
                text = "${record.subject} · ${record.detail}",
                fontSize = 12.sp,
                color = Gray900,
                modifier = Modifier.weight(1f)
            )
            // 오른쪽: 상태 + 체크 시각 (시각이 없으면 상태만)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = record.status.label,
                    fontSize = 11.sp,
                    color = Gray900
                )
                record.checkedAt?.let { time ->
                    Text(
                        text = time,
                        fontSize = 11.sp,
                        color = Gray900
                    )
                }
            }
        }
    }
}

/**
 * 내 출석 기록 화면용 날짜별 기록 행
 *
 * 상태 라벨은 상태별 색으로 강조되고, 오른쪽 끝 표시는 3가지로 갈린다.
 * - 출석: 흰 배경 + 체크 아이콘
 * - 지각/결석 & 정정 요청 전: 연분홍 배경 + '정정 요청' 버튼
 * - 지각/결석 & 정정 요청 완료([AttendanceRecord.correctionRequested] = true): 연분홍 배경 + '정정 요청 완료' 안내 텍스트
 *
 * @param onRequestCorrection '정정 요청' 버튼 클릭 콜백
 */
@Composable
fun HistoryRecordRow(
    record: AttendanceRecord,
    modifier: Modifier = Modifier,
    onRequestCorrection: () -> Unit = {}
) {
    // 정정 요청 가능한 상태(지각/결석)는 연분홍으로 구분
    val needsCorrection = record.status.canRequestCorrection
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (needsCorrection) Red50 else androidx.compose.ui.graphics.Color.White
        ),
        border = BorderStroke(1.dp, if (needsCorrection) Red300 else Gray300),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 13.dp, vertical = 10.dp)
        ) {
            // 상태 라벨 (출석/지각/결석 — 상태별 색상)
            Text(
                text = record.status.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = record.status.color
            )
            // 과목명 + 보조 정보
            Text(
                text = "${record.subject} ${record.detail}",
                fontSize = 13.sp,
                color = Gray900,
                modifier = Modifier.weight(1f)
            )
            when {
                !needsCorrection -> {
                    // 출석: 확인 체크 아이콘
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "출석 확인됨",
                        tint = Gray500,
                        modifier = Modifier.size(16.dp)
                    )
                }
                record.correctionRequested -> {
                    // 지각/결석 + 정정 요청 이미 제출함: 안내 텍스트만 표시
                    Text(
                        text = "정정 요청 완료",
                        fontSize = 13.sp,
                        color = Gray500
                    )
                }
                else -> {
                    // 지각/결석 + 아직 요청 안 함: 정정 요청 버튼
                    OutlinedButton(
                        text = "정정 요청",
                        onClick = onRequestCorrection
                    )
                }
            }
        }
    }
}
