package com.example.attendance.core.data.repository
import kotlinx.coroutines.delay

/**
 * 인증(로그인/로그아웃) 데이터 소스 인터페이스
 */
interface AuthRepository {
    // 학번/비밀번호로 로그인. 성공 시 [Result.success], 실패 시 [Result.failure]
    suspend fun login(studentId: String, password: String): Result<Unit>

    // 로그아웃 (세션/토큰 정리)
    suspend fun logout()
}

/**
 * 서버 연동 전까지 사용하는 가짜 구현체
 * - 네트워크 왕복을 흉내내기 위해 약간의 지연 후 항상 성공을 반환한다.
 */
class FakeAuthRepository: AuthRepository {
    override suspend fun login(studentId: String, password: String): Result<Unit> {
        delay(500) // 네트워크 지연 시뮬레이션
        return Result.success(Unit)
    }

    override suspend fun logout() {
        delay(200)
    }
}