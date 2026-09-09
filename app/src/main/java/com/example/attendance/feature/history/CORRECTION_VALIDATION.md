# 출석 정정 요청 검증

## 현재 상태

2026-09-09: CorrectionViewModelTest 9건, CorrectionRequestScreenTest 6건 작성. 기존 3-6 UI 테스트의 import 누락으로 APK 빌드가 실패했으나, 누락 수정 후 사용자가 전체 테스트 통과를 확인했다. 단위·UI 테스트 통과는 사용자 확인 기준이며 에이전트가 재실행하거나 개별 보고서를 확인한 결과는 아니다. APK 빌드 단독 재실행 로그와 Preview·수동 확인 결과는 별도로 확인하지 않았다.

## 실행 조건

PowerShell에서 실제 앱 루트 D:\Dev\attendance-app을 사용한다. 프로젝트의 Gradle/AGP에 맞는 JDK(현재 AGP 8 계열은 최소 JDK 17), Android SDK 36과 Gradle 의존성 다운로드 환경을 준비한다. 최초 실행은 다운로드 시간이 필요할 수 있다. UI 테스트에는 API 30 이상 기기 또는 에뮬레이터가 필요하다. adb devices에서 대상이 device 상태인지 확인하고 무선 연결이 불안정하면 연결 상태를 정리한 뒤 실행한다.

## 실행 명령

```powershell
cd D:\Dev\attendance-app

# JVM 단위 테스트: 9건, 실패 0건 기대
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.attendance.feature.history.presentation.CorrectionViewModelTest"

# 앱 및 UI 테스트 APK 빌드: 기기 실행과 별개
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest

adb devices
# Compose UI 테스트: 6건, 실패 0건 기대
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.attendance.feature.history.presentation.CorrectionRequestScreenTest"
```

adb가 PATH에 없다면 Android SDK의 platform-tools/adb.exe를 사용한다. 각 명령의 최종 BUILD SUCCESSFUL과 테스트 개수·실패 수를 확인한다. 기존 다른 테스트까지 확인하려면 클래스 제한 없이 :app:testDebugUnitTest를 별도로 실행한다.

## 검증 대상

| 종류 | 대상 |
| --- | --- |
| JVM 9건 | Long 인자 조회·로딩, 인자 누락, 없는 기록, 공백 사유 차단·해제·인자 전달·성공, 중복 방지·입력 스냅샷, false 후 재시도, 조회 예외, 제출 예외, 취소 처리 |
| UI 6건 | 제출 콜백, 제출 중 비활성, 사유·상세 입력, 오류 표시, 대상 부재 제출 차단, 취소 콜백 |
| 후속 확인 | 3-9에서 인자·완료 복귀 테스트 작성, 실행 대기. 프로세스 복원 통합은 별도 확인 |

## 보고서와 APK

- JVM: app/build/reports/tests/testDebugUnitTest/index.html
- 기기: app/build/reports/androidTests/connected/debug/index.html
- 앱 APK: app/build/outputs/apk/debug/app-debug.apk
- 테스트 APK: app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk

실제 생성 경로가 다르면 Gradle 출력에 표시된 위치를 우선한다.

## Preview와 수동 확인

CorrectionRequestScreen.kt의 CorrectionContentPreview를 Android Studio에서 열어 대상 카드·사유·상세·파일 영역·버튼 배치를 확인한다. 작은 화면의 스크롤, 사유 공백 오류, 실패 문구는 UI 테스트 또는 상태를 주입한 Preview에서 확인한다. 3-9 연결 후에는 앱에서 로그인 → 기록 탭 → 정정 요청으로 진입할 수 있다. 이번 Navigation 변경의 빌드·실행은 별도 검증 대상이며 ../navigation/VALIDATION.md를 따른다.

## 가이드의 커밋 메시지

사용자 요청에 따라 파일별로 커밋한다. 가이드의 소스 묶음은 ViewModel과 화면으로 나누고, 설명 문서도 파일별 docs 메시지를 사용한다.

```text
feat(history): CorrectionViewModel 도입 및 제출 검증 구현
feat(history): 출석 정정 요청 화면 구현
test(history): CorrectionViewModel 단위 테스트 추가 (SavedStateHandle/제출 검증)
test(history): CorrectionRequestScreen Compose UI 테스트 추가
docs(history): 출석 정정 요청 흐름과 코드별 해설 추가
```

## 실행 기록

- 단위 테스트: 2026-09-09 사용자 전체 통과 확인 (CorrectionViewModelTest 9건 포함). 에이전트 재실행 없음.
- 앱·UI 테스트 APK 빌드: 사용자 실행에서 :app:compileDebugAndroidTestKotlin 실패. AttendanceHistoryScreenTest의 onNodeWithContentDescription import 누락 수정 후 사용자가 전체 테스트 통과를 확인했다. assembleDebug·assembleDebugAndroidTest 단독 재실행 로그는 별도 확인하지 않음.
- Compose UI 테스트: 2026-09-09 사용자 전체 통과 확인 (CorrectionRequestScreenTest 6건 포함). 에이전트 재실행 없음.
- Preview·수동 기기 확인: 미실행

사용자가 실행 결과를 제공하면 날짜·명령·대상 변경·성공/실패를 구분해서 기록한다.
