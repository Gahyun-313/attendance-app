# 홈 화면 직접 검증하기

이번 수정에서는 테스트, APK 빌드, 에뮬레이터 실행을 수행하지 않았다. 아래 명령으로 직접 검증한 뒤 결과를 기록한다. 이전 로그인 단계의 빌드 성공을 이번 홈 단계의 검증 결과로 사용하지 않는다.

## 사전 조건

- Android Studio에서 `attendance-app` 프로젝트를 연다.
- Gradle을 실행할 JDK 17 이상과 프로젝트가 요구하는 Android SDK가 준비되어 있어야 한다.
- Android Studio Terminal의 PowerShell에서 다음 위치로 이동한다.

```powershell
cd D:\Dev\attendance-app
```

## 1. 홈 ViewModel 단위 테스트

에뮬레이터 없이 JVM에서 실행한다.

```powershell
.\gradlew.bat testDebugUnitTest --tests "com.example.attendance.feature.home.presentation.HomeViewModelTest"
```

기대 결과는 아래 5개 테스트의 통과와 `BUILD SUCCESSFUL`이다.

1. 구독 전 빈 초기 상태 유지
2. 구독 후 네 Flow의 결합 결과와 읽지 않은 알림 앞의 두 건
3. 모두 읽음 처리 후 알림 제거 및 다른 데이터 유지
4. 통계·세션·기록 각각의 변경 반영
5. 목록이 비어도 통계 유지

보고서: `app/build/reports/tests/testDebugUnitTest/index.html`.

## 2. 앱과 UI 테스트 APK 빌드

```powershell
.\gradlew.bat assembleDebug assembleDebugAndroidTest
```

앱과 계측 테스트 코드가 컴파일·패키징되는지 확인한다. 이것만으로 UI 테스트가 기기에서 실행되지는 않는다.

- 앱 APK: `app/build/outputs/apk/debug/app-debug.apk`
- 테스트 APK: `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`

## 3. 홈 UI 테스트

Device Manager에서 에뮬레이터를 직접 시작하거나, USB 디버깅을 활성화한 기기를 연결한다. SDK의 platform-tools가 PATH에 있어야 아래 adb 명령을 사용할 수 있다.

```powershell
adb devices
```

연결 대상이 `device`로 표시되는지 확인한다. `offline` 또는 `unauthorized`이면 연결이나 기기의 디버깅 허용을 먼저 확인한다. 무선 ADB에서 타임아웃이 반복되면 연결을 확인하거나 USB로 재시도한다.

```powershell
.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.attendance.feature.home.presentation.HomeScreenTest"
```

기대 결과는 통계 표시, 빈 목록의 섹션 표시, 출석 체크 콜백, 알림·기록 더보기 콜백의 4개 테스트 통과다. 보고서: `app/build/reports/androidTests/connected/debug/index.html`.

## 4. Preview로 배치 확인

`feature/home/presentation/HomeScreen.kt`의 `HomeContentPreview`를 Android Studio의 Split 또는 Design 모드에서 확인한다. Preview는 샘플 상태만 그리며 ViewModel 수집이나 실제 이동을 검증하지 않는다.

현재 MainActivity는 기존 HomePrototypeScreen을 호출한다. 앱 실행 화면이 새 HomeScreen으로 바뀌었다고 기대하면 안 된다. 이후 Navigation 연결 시 실제 Screen 진입과 이동도 별도로 확인한다.

## 결과 기록

- [ ] 홈 단위 테스트 5개 통과
- [ ] 앱 APK 빌드 통과
- [ ] UI 테스트 APK 빌드 통과
- [ ] 홈 UI 테스트 4개 통과
- [ ] Preview 배치 확인

실행하지 않은 항목은 체크하지 않는다. 실패 시 전체 실패 테스트 이름, expected/actual, 보고서의 예외 원인을 함께 확인한다.
