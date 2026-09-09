# 🧪 Testing & Run

[README로 돌아가기](../README.md)

## 검증 범위

| 구분 | 확인 대상 | 필요한 환경 |
| --- | --- | --- |
| JVM 단위 테스트 | ViewModel 상태와 Repository 호출 | JDK·Android SDK |
| Compose UI 테스트 | 표시, 입력, 다이얼로그, 화면 이동 | 연결된 에뮬레이터 또는 기기 |
| APK 빌드 | 앱·테스트 패키지 생성 | JDK·Android SDK |
| 수동 확인 | 화면 배치와 캡처, 실제 조작 | 앱 실행 환경 |

테스트 코드 존재, 과거 실행 결과, 이번 문서 작성 시 검증을 구분합니다. 저장소 AGENTS.md에는 2026-09-09 사용자가 Navigation 단계의 빌드·전체 JVM·전체 UI 테스트 통과를 확인한 기록이 있습니다. 이번 문서 작업에서는 테스트·빌드·에뮬레이터를 재실행하지 않았습니다.

## 작성된 테스트

| 대상 | JVM 테스트 | Compose UI 테스트 |
| --- | --- | --- |
| 스플래시 | 해당 없음 | SplashScreenTest |
| 로그인 | LoginViewModelTest, LoginViewModelMockkTest | LoginScreenTest |
| 홈 | HomeViewModelTest | HomeScreenTest |
| 출석 | AttendanceViewModelTest | AttendanceScreenTest |
| 알림 | NotificationViewModelTest | NotificationScreenTest |
| 기록 | HistoryViewModelTest | AttendanceHistoryScreenTest |
| 정정 요청 | CorrectionViewModelTest | CorrectionRequestScreenTest |
| 마이페이지 | 후속 과제 | MyPageScreenTest |
| Navigation | 해당 없음 | AppNavGraphTest, BottomNavigationBarTest |

ViewModel 테스트는 Fake·Mock과 코루틴 테스트 도구로 비동기 상태를 확인합니다. UI 테스트는 화면과 콜백·이동을 검증하며 실제 서버나 NFC 하드웨어를 검증하지 않습니다.

## 실행 환경

- Android Studio와 프로젝트 Gradle 실행에 맞는 JDK 17
- Android SDK 36 및 프로젝트에서 요구하는 SDK 구성요소
- UI 테스트·수동 실행용 에뮬레이터 또는 Android 기기
- 최초 Gradle 의존성 다운로드를 위한 네트워크
- 명령 실행 위치: 저장소 루트; 아래는 로컬 경로 예시

## 앱 실행과 캡처

Android Studio에서 실제 앱 프로젝트를 열고 app 실행 구성을 선택한 뒤 기기에서 실행합니다. 서버나 별도 Fake 전환 설정은 필요하지 않습니다.

로그인 입력 예시는 학번 `20260001`, 비밀번호 `test1234`입니다. 기본 FakeAuthRepository는 실제 계정을 검사하지 않고 약 500ms 뒤 성공을 반환합니다.

홈과 하단 탭을 이동하며 캡처할 수 있습니다. 출석 Fake는 약 2초 뒤 PRESENT를 반환하고 정정 요청 Fake는 약 500ms 뒤 true를 반환합니다. 실패 UI의 존재와 기본 Fake 실행 시 실패가 발생하는 것은 별개입니다.

## JVM 테스트

```powershell
cd D:\Dev\attendance-app
.\gradlew.bat :app:testDebugUnitTest
```

기대 결과는 실패 0건과 `BUILD SUCCESSFUL`입니다.

보고서: `app/build/reports/tests/testDebugUnitTest/index.html`

## UI 테스트

```powershell
cd D:\Dev\attendance-app
adb devices
.\gradlew.bat :app:connectedDebugAndroidTest
```

기기가 `device` 상태로 연결되어 있어야 합니다. adb가 PATH에 없으면 Android SDK의 platform-tools 안에 있는 adb.exe를 사용합니다.

기대 결과는 연결된 기기에서 테스트 실행 완료, 실패 0건과 `BUILD SUCCESSFUL`입니다.

보고서: `app/build/reports/androidTests/connected/debug/index.html`

생성 위치가 다르면 Gradle 출력의 보고서 경로를 확인합니다.

## APK 빌드

```powershell
cd D:\Dev\attendance-app
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest
```

앱 APK: `app/build/outputs/apk/debug/app-debug.apk`

APK 생성은 기기 테스트 실행과 다릅니다. 위 명령 성공만으로 UI 테스트 통과를 의미하지 않습니다.

## 후속 검증

실제 서버 연결 이후에는 인증 만료, 네트워크 오류, 출석·정정 이후 재조회와 데이터 일관성을 추가 검증해야 합니다. NFC 수신과 실제 기기 조건은 하드웨어를 포함한 별도 확인이 필요합니다.
