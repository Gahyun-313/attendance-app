package com.example.attendance.feature.splash.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.example.attendance.ui.theme.AttendanceTheme
import org.junit.Rule
import org.junit.Test

class SplashScreenTest {
    @get: Rule
    val composeTestRule = createComposeRule()

    @Test
    fun 캐릭터_이미지가_표시된다() {
        composeTestRule.setContent {
            AttendanceTheme {
                SplashScreen(onTimeout = {})
            }
        }

        composeTestRule
            .onNodeWithContentDescription("출석하자 캐릭터")
            .assertIsDisplayed()
    }
}