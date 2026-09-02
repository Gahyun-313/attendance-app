package com.example.attendance.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.attendance.core.model.ClassSession
import com.example.attendance.ui.theme.Gray50
import com.example.attendance.ui.theme.Gray900
import com.example.attendance.ui.theme.Gray500

/**
 * NFC 출석 인증 모달
 *
 * '출석 시작'을 누르면 표시되며, 기기를 NFC 태그에 가까이 대라고 안내한다.
 * 가운데에 스마트폰 그래픽, 하단에 세션 정보 배너가 들어간다.
 *
 * @param session   출석 체크 대상 세션
 * @param onDismiss 모달 닫기 콜백 (X 버튼/바깥 터치)
 */
@Composable
fun NfcScanDialog(
    session: ClassSession,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                // 오른쪽 상단 닫기(X) 버튼
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = Gray900
                        )
                    }
                }

                // 제목 + 안내 문구
                Text(
                    text = "NFC 출석 인증",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "기기를 NFC 태그에 가까이 대주세요.",
                    fontSize = 13.sp,
                    color = Gray500
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 스마트폰 태깅 그래픽 (회색 원 안의 휴대폰 아이콘)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(110.dp)
                        .background(Gray50, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = "NFC 태깅 안내",
                        tint = Gray900,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 하단 세션 정보 배너 (공통 컴포넌트 재사용)
                SessionInfoBanner(session = session)
            }
        }
    }
}
