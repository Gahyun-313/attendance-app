package com.example.attendance.core.data.repository
import kotlinx.coroutines.delay

/**
 * 인증(로그인/로그아웃) 데이터 소스 인터페이스
 */
interface AuthRepository {
    // 학번/비밀번호로 로그인
    // 성공 시 [Result.success], 실패 시 [Result.failure]
    suspend fun login(studentId: String, password: String): Result<Unit>

    // 로그아웃 (세션/토큰 정리)
    suspend fun logout()
}

/**
 * 서버 연동 전까지 사용하는 가짜 구현체
 * - 지연 후 shouldFail에 따라 성공 또는 실패를 반환한다. 실제 인증이나 세션 저장은 하지 않는다.
 */
class FakeAuthRepository : AuthRepository {

    /** 테스트에서 실패 케이스를 시뮬레이션하기 위한 스위치 */
    var shouldFail: Boolean = false

    /** 테스트에서 네트워크 지연 시간을 조절하기 위한 값 (기본 500ms) */
    var networkDelayMs: Long = 500

    /** 입력값 자체를 검증하지 않고, 테스트에서 설정한 결과를 지연 후 반환한다. */
    override suspend fun login(studentId: String, password: String): Result<Unit> {
        delay(networkDelayMs) // 네트워크 지연 시뮬레이션
        return if (shouldFail) {
            Result.failure(Exception("아이디 또는 비밀번호가 올바르지 않습니다."))
        } else {
            Result.success(Unit)
        }
    }

    /** 로그아웃 대기만 흉내 낸다. 현재 Fake에는 제거할 토큰이나 세션이 없다. */
    override suspend fun logout() {
        delay(200)
    }
}
