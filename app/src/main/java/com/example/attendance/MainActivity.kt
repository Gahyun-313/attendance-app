package com.example.attendance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.attendance.core.navigation.AppNavGraph
import com.example.attendance.ui.theme.AttendanceTheme

/** 하나의 Activity가 Navigation 그래프를 호스팅한다. 홈 초안은 Preview용 파일로 유지한다. */
class MainActivity : ComponentActivity() {
    /** Activity 생성 시 테마와 화면 이동 컨트롤러를 구성한다. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AttendanceTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}
