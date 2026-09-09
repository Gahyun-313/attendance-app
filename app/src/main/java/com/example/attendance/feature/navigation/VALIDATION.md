# Navigation 선행 구현 검증

2026-09-09 사용자가 앱·UI 테스트 APK 빌드, 전체 JVM 테스트, 새 UI 테스트 10건, 전체 UI 테스트 통과를 확인했다. 에이전트가 재실행하거나 개별 보고서를 확인한 결과는 아니다. 회전·키보드 등 수동 확인은 별도다.

## 실행 조건과 명령

실제 앱 루트에서 프로젝트에 맞는 JDK 17 이상, Android SDK 36을 준비한다. Navigation 테스트 의존성 2.7.7을 추가했으므로 최초 실행 시 다운로드가 필요할 수 있다. UI 테스트는 API 30 이상 기기 또는 에뮬레이터가 adb devices에서 device 상태여야 한다.

```powershell
cd D:\Dev\attendance-app
# 기존 ViewModel 로직 회귀 확인
.\gradlew.bat :app:testDebugUnitTest
# 앱과 UI 테스트 APK 빌드
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest
adb devices
# 새 테스트 10건: Navigation 6, 하단 탭 1, 마이페이지 3
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.attendance.core.navigation.AppNavGraphTest,com.example.attendance.core.navigation.BottomNavigationBarTest,com.example.attendance.feature.mypage.presentation.MyPageScreenTest"
# 모든 기존 UI 테스트까지 확인할 때
.\gradlew.bat :app:connectedDebugAndroidTest
```

각 명령의 최종 BUILD SUCCESSFUL, 실패 0건, 대상 테스트 개수를 확인한다. 단위 보고서: app/build/reports/tests/testDebugUnitTest/index.html. 기기 보고서: app/build/reports/androidTests/connected/debug/index.html. 앱 APK: app/build/outputs/apk/debug/app-debug.apk. 실제 경로가 다르면 Gradle 출력을 따른다.

## 수동 앱 확인

Android Studio에서 app을 실행하면 이제 스플래시 → 로그인 → 홈으로 연결된다. FakeAuthRepository는 기본적으로 입력값을 검사하지 않고 지연 후 성공하지만, 검증 시 학번·비밀번호를 입력해 진행한다.

- [ ] 스플래시 후 로그인으로 이동하고 뒤로가기로 스플래시에 돌아가지 않는다.
- [ ] 로그인 성공 후 홈을 표시하고 뒤로가기로 로그인에 돌아가지 않는다.
- [ ] 홈·알림·기록·마이 탭을 왕복하고 같은 탭을 반복 눌러도 화면이 쌓이지 않는다.
- [ ] 홈의 알림·기록 더보기가 각각 해당 탭으로 이동한다.
- [ ] 홈 → 출석 체크 → 스캔·결과 모달 → 뒤로가기 흐름을 확인한다. 실제 NFC 테스트는 아니다.
- [ ] 알림 모두 읽음 후 홈의 읽지 않은 알림이 사라진다.
- [ ] 기록의 정정 요청 → 올바른 대상 표시 → 사유 검증 → 제출 후 기록 복귀를 확인한다.
- [ ] 정정 취소·시스템 뒤로가기도 기록으로 돌아간다.
- [ ] 로그아웃 취소는 화면을 유지하고 확인은 로그인으로 이동한다. 로그인 전 화면이 뒤로가기로 보이지 않는다.
- [ ] 다시 로그인한 후 이전 탭 ViewModel·저장 상태가 복원되지 않는다.
- [ ] 회전·키보드·작은 화면에서 하단 바와 콘텐츠가 겹치지 않는지 확인한다.

앱 로그아웃은 현재 화면 스택 정리만 제공한다. 실제 Repository.logout과 그 ViewModel·단위 테스트는 연습 과제로 남겨둔다. Fake 출석 통계 자동 갱신·정정 접수 저장·실제 서버·파일 업로드는 구현하지 않았다.

## 작성한 자동 검증

| 테스트 | 건수 | 검증 |
| --- | --- | --- |
| BottomNavigationBarTest | 1 | 네 탭 표시 |
| AppNavGraphTest | 6 | Splash 제거, Login 제거, 탭 복원·중복 방지, 정정 ID·성공 복귀, 로그아웃 상태 제거, 출석 상세 왕복 |
| MyPageScreenTest | 3 | 정적 정보, 로그아웃 확인·취소, 준비 중 안내 |

Navigation 테스트는 TestNavHostController에 ComposeNavigator를 추가하고 실제 AppNavGraph·화면·수동 Factory·Fake를 사용한다. 회전·프로세스 복원은 위 수동 체크 범위이며 자동 검증을 완료했다고 표시하지 않는다.

## 커밋 메시지 안내

```text
feat(navigation): 전체 라우트 구성 및 하단 탭 4개(홈·알림·기록·마이) 적용
feat(history): 기록에서 정정 요청 화면 이동 연결
feat(mypage): 마이페이지 화면 및 로그아웃 다이얼로그 구현
test(navigation): BottomNavigationBar Compose UI 테스트 추가
test(navigation): 화면 이동과 백스택 통합 테스트 추가
test(mypage): MyPageScreen Compose UI 테스트 추가
docs: 3-8 연습 과제 보류 및 3-9 선행 순서 반영
```

사용자 요청에 따라 소스·테스트·설명 문서를 각각 파일별 커밋한다.
