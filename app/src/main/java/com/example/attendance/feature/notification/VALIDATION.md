# 알림 화면 직접 검증하기

사용자가 아래 명령을 직접 실행해 단위 테스트 2건과 UI 테스트 2건 모두 통과를 확인했다(2026-09-08). 실행 환경(에뮬레이터/실기기 모델)은 이 기록에서 별도로 특정하지 않는다. 이전 단계(3-4 출석 체크)의 통과 결과를 이번 3-5 검증으로 대신하지 않는다.

## 사전 조건

- Android Studio에서 `attendance-app` 프로젝트를 연다.
- Gradle을 실행할 JDK 17 이상과 프로젝트가 요구하는 Android SDK가 준비되어 있어야 한다.
- Android Studio Terminal의 PowerShell에서 다음 위치로 이동한다.

```powershell
cd D:\Dev\attendance-app
```

## 1. NotificationViewModel 단위 테스트

에뮬레이터 없이 JVM에서 실행한다.

```powershell
.\gradlew.bat testDebugUnitTest --tests "com.example.attendance.feature.notification.presentation.NotificationViewModelTest"
```

대상 2개 테스트:

1. 초기 상태는 읽음/안읽음으로 분류되어 있다
2. 모두 읽음 처리하면 unread가 비워진다

**결과: 통과.** 보고서: `app/build/reports/tests/testDebugUnitTest/index.html`.

## 2. 앱과 UI 테스트 APK 빌드

```powershell
.\gradlew.bat assembleDebug assembleDebugAndroidTest
```

앱과 계측 테스트 코드가 컴파일·패키징되는지 확인하는 단계다.

- 앱 APK: `app/build/outputs/apk/debug/app-debug.apk`
- 테스트 APK: `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`

## 3. NotificationScreen UI 테스트

에뮬레이터 또는 USB 디버깅을 활성화한 기기를 연결한다.

```powershell
adb devices
.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.attendance.feature.notification.presentation.NotificationScreenTest"
```

대상 2개 테스트:

1. 알림이 없으면 빈 상태 문구가 표시된다
2. 모두 읽음 처리 버튼 클릭 시 콜백이 호출된다

**결과: 통과.** 보고서: `app/build/reports/androidTests/connected/debug/index.html`.

## 4. Preview로 배치 확인

`feature/notification/presentation/NotificationScreen.kt`의 `NotificationContentPreview`를 Android Studio의 Split 또는 Design 모드에서 확인한다. Preview는 샘플 목록을 읽음/안읽음으로 나눈 상태만 그리며 ViewModel 수집, Repository 공유(홈 화면과의 동기화), 실제 뒤로가기는 검증하지 않는다.

이 화면은 아직 네비게이션 그래프에 연결되지 않았다(STEP 3-9에서 처리). 앱을 실행해도 이 화면으로 직접 진입할 수 있는 경로는 없다.

## 결과 기록

- [x] NotificationViewModel 단위 테스트 2개 통과
- [ ] 앱 APK 빌드 (별도 확인 필요 — 위 1·3 단계는 단위/UI 테스트 실행 여부만 확인됨)
- [x] NotificationScreen UI 테스트 2개 통과
- [ ] Preview 배치 확인

APK 빌드·Preview 확인 여부는 이번 대화에서 별도로 보고받지 않아 미체크로 남긴다. 실패가 있었다면 실패 테스트 이름, expected/actual, 보고서의 예외 원인을 함께 기록한다.
