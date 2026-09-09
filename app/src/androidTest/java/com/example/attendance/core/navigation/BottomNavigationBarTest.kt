package com.example.attendance.core.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import com.example.attendance.ui.theme.AttendanceTheme
import org.junit.Rule
import org.junit.Test

/** 가이드의 하단 탭 렌더링 스모크 테스트다. */
class BottomNavigationBarTest {
    @get:Rule
    val rule = createComposeRule()

    /** 네 개 탭이 모두 사용자에게 표시되는지 확인한다. */
    @Test
    fun 하단_탭_4개가_표시된다() {
        rule.setContent {
            AttendanceTheme { BottomNavigationBar(rememberNavController()) }
        }
        listOf("홈", "알림", "기록", "마이").forEach { label ->
            rule.onNodeWithText(label).assertIsDisplayed()
        }
    }
}
