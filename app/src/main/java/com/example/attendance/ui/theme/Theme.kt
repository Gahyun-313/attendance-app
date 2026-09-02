package com.example.attendance.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * 앱 라이트 컬러 스킴
 *
 * Figma 디자인이 라이트 모드 기준으로만 제공되므로
 * 다크 모드/다이나믹 컬러 대신 브랜드 컬러를 고정으로 사용한다.
 */
private val LightColorScheme = lightColorScheme(
    primary = Blue600,           // 주요 버튼, 강조 요소
    onPrimary = White,           // primary 위에 올라가는 텍스트
    secondary = Blue400,         // 보조 버튼 (출석 체크 등)
    onSecondary = White,
    background = Gray50,         // 화면 기본 배경
    onBackground = Gray900,
    surface = White,             // 카드, 다이얼로그 표면
    onSurface = Gray900,
    onSurfaceVariant = Gray500,  // 보조 텍스트
    outline = Gray400,           // 입력창 테두리
    outlineVariant = Gray300,    // 카드 테두리
    error = Red900,
)

/**
 * 앱 전체 테마
 *
 * MaterialTheme을 감싸서 색상/타이포그래피를 일괄 적용한다.
 * 모든 화면은 이 테마 안에서 렌더링된다.
 */
@Composable
fun AttendanceTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}