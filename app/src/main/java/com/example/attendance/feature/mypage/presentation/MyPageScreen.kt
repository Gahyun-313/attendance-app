package com.example.attendance.feature.mypage.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.attendance.R
import com.example.attendance.core.designsystem.component.ConfirmDialog
import com.example.attendance.core.designsystem.component.OutlinedButton
import com.example.attendance.ui.theme.AttendanceTheme

/**
 * 3-8의 정적 마이페이지 UI를 먼저 제공한다. 확인창은 화면 자체의 상태로 보관한다.
 * TODO(연습 과제): MyPageViewModel, AuthRepository.logout 호출, MyPageViewModelTest를 직접 작성한다.
 */
@Composable
fun MyPageScreen(
    onLogout: () -> Unit
) {
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var notice by rememberSaveable { mutableStateOf<String?>(null) }
    if (showLogoutDialog) {
        ConfirmDialog(
            title = "로그아웃 하시겠어요?",
            confirmText = "확인",
            onConfirm = { showLogoutDialog = false; onLogout() },
            onDismiss = { showLogoutDialog = false }
        )
    }
    notice?.let { title ->
        AlertDialog(
            onDismissRequest = { notice = null },
            title = { Text(title) },
            text = { Text("준비 중인 기능입니다.") },
            confirmButton = { TextButton(onClick = { notice = null }) { Text("닫기") } }
        )
    }
    MyPageContent(
        onProfile = { notice = "프로필 관리" },
        onNotificationSettings = { notice = "알림 설정" },
        onLogout = { showLogoutDialog = true }
    )
}

/** 정적 프로필과 계정 메뉴를 그리고 사용자 이벤트를 외부로 전달한다. */
@Composable
fun MyPageContent(
    onProfile: () -> Unit,
    onNotificationSettings: () -> Unit,
    onLogout: () -> Unit,
    studentName: String = "학생 이름",
    semester: String = "학기 정보 준비 중"
) {
    Scaffold { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("마이페이지", fontWeight = FontWeight.Bold)
            Card {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Image(painterResource(R.drawable.attendance_character), contentDescription = "프로필 이미지", modifier = Modifier.size(64.dp))
                    Column {
                        Text(studentName, fontWeight = FontWeight.Bold)
                        Text(semester)
                    }
                }
            }
            Text("계정 설정", fontWeight = FontWeight.Bold)
            OutlinedButton(text = "프로필 관리", onClick = onProfile)
            OutlinedButton(text = "알림 설정", onClick = onNotificationSettings)
            OutlinedButton(text = "로그아웃", onClick = onLogout)
        }
    }
}

/** ViewModel 없이 실제 마이페이지 UI를 미리 본다. */
@Preview(showBackground = true)
@Composable
private fun MyPageScreenPreview() {
    AttendanceTheme { MyPageScreen(onLogout = {}) }
}
