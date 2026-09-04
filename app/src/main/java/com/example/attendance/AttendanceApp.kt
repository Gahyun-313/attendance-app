package com.example.attendance

import android.app.Application
import com.example.attendance.core.di.AppContainer
import com.example.attendance.core.di.DefaultAppContainer
import timber.log.Timber

/**
 * Application 클래스
 *
 * 앱 전역 의존성 컨테이너를 생성해 보관한다.
 * 각 viewModel Factory는 Application을 통해 이 컨테이너에 접근한다.
 */
class AttendanceApp : Application() {
    /** 앱 전역 의존성 컨테이너 (Repository 제공) */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        // 의존성 컨테이너 초기화
        container = DefaultAppContainer()

        // Timber(로깅 라이브러리) 초기화 - 디버그 빌드에서만 로그 출력
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}