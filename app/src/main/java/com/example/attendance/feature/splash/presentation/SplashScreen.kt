package com.example.attendance.feature.splash.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.attendance.R
import com.example.attendance.ui.theme.AttendanceTheme
import com.example.attendance.ui.theme.Blue200
import kotlinx.coroutines.delay


// 스플래시 노출 시간
private const val SPLASH_DURATION_MS = 1_500L

/**
 * 스플래시 화면
 */
@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    // 화면 진입 후 일정 시간 뒤 자동 이동
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        onTimeout()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Blue200)
    ) {
        // 중앙 캐릭터
        Image(
            painter = painterResource(id = R.drawable.attendance_character),
            contentDescription = "출석하자 캐릭터",
            modifier = Modifier.size(120.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    AttendanceTheme {
        SplashScreen(onTimeout = {})
    }
}