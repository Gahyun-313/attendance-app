# 화면 이동과 Navigation

## 진행 순서

3-7 이후 3-8의 기본 마이페이지 UI·UI 테스트를 준비하고 3-9 Navigation을 먼저 연결했다. **MyPageViewModel, AuthRepository.logout() 호출의 ViewModel 이전, MyPageViewModelTest는 사용자 연습 과제로 보류**한다. 연습 과제를 완료했다고 체크하지 않는다. 현재 로그아웃은 확인 후 화면과 백스택을 정리하는 동작이며 인증 세션 정리를 구현한 것이 아니다.

## 구조와 호출 흐름

```text
MainActivity → AttendanceTheme → rememberNavController → AppNavGraph
  Splash ─시간 종료→ Login ─로그인 성공→ Main 그래프 / Home
                                          ├─ Home ─출석 체크→ Attendance
                                          ├─ Notification
                                          ├─ History ─정정 요청(ID)→ Correction
                                          │                          └─ 취소/성공→ History
                                          └─ MyPage ─확인→ Login
```

ScreenRoute는 문자열·ID 인자 계약을 정의하고 AppNavGraph는 목적지와 이동 정책을 연결한다. BottomNavigationBar는 현재 경로를 관찰해 선택 탭을 표시한다. 실제 소스는 feature/navigation이 아니라 core/navigation에 있으며 이 디렉터리는 학습 문서 위치다.

| 경로 | 화면 | 하단 탭 | 백스택 정책 |
| --- | --- | --- | --- |
| splash | SplashScreen | 없음 | 타이머 종료 시 제거 |
| login | LoginScreen | 없음 | 로그인 성공 시 제거 |
| main | 화면 없는 중첩 그래프 | 해당 없음 | 로그인 이후 화면들의 제거 경계 |
| home | HomeScreen | 홈 | Main 시작점, 탭 이동의 기준 |
| notification | NotificationScreen | 알림 | 저장·복원되는 탭 |
| history | AttendanceHistoryScreen | 기록 | 저장·복원되는 탭 |
| mypage | MyPageScreen | 마이 | 저장·복원되는 탭 |
| attendance | AttendanceScreen | 없음 | 뒤로가기 시 이전 홈으로 복귀 |
| correction/{recordId} | CorrectionRequestScreen | 없음 | 취소·성공 시 popBackStack |

## 백스택·생명주기·상태

navigate는 목적지를 백스택에 추가하고 NavHost가 해당 Screen을 구성한다. 다른 Composable을 함수처럼 호출하는 것만으로는 이전 화면·뒤로가기·인자·ViewModel 수명이 관리되지 않으므로 이동은 controller에 맡긴다.

탭 이동의 popUpTo(Home)는 홈 위의 목적지를 제거한다. saveState=true는 떠나는 탭의 복원 상태를 보관하고 restoreState=true가 다시 진입할 때 이를 사용한다. launchSingleTop은 맨 위의 같은 목적지가 중복 생성되지 않게 하며, 현재 탭 재선택 가드도 함께 적용했다. 홈의 더보기 콜백 역시 navigateToTab을 사용한다.

ViewModel은 NavBackStackEntry 범위에서 생성된다. 탭을 저장해 떠나는 경우와 상세 화면을 완전히 pop하는 경우의 수명이 다르다. 정정 성공 시 해당 목적지를 제거하므로 동일 성공 상태가 계속 남아 재이동하는 것을 막는다. 프로세스 재생성에 대한 모든 도메인 상태 복원을 보장하는 구현은 아니다. 로그인 세션·입력 초안의 영구 저장은 없다.

로그아웃은 먼저 clearBackStack으로 저장된 탭 상태를 제거하고 Main 그래프를 inclusive=true로 pop한다. 현재 보이는 화면만 지우면 이전 탭의 상태가 다음 로그인에서 restoreState로 복원될 수 있기 때문이다. AuthRepository.logout 호출은 보류된 연습 과제다. 실제 토큰이나 사용자별 데이터가 없는 현재 Fake 범위에서 화면 정리만 수행한다.

## recordId 전달

HistoryRecordRow → HistoryContent.onRequestCorrection(record.id) → AttendanceHistoryScreen의 콜백 → ScreenRoute.Correction.createRoute(id) → NavHost의 NavType.LongType → 해당 entry의 CreationExtras → createSavedStateHandle() → CorrectionViewModel.getRecord(id) 순서다. Content는 NavController를 모르며 Long 값만 위로 전달한다. ARG_RECORD_ID는 기존 3-7 계약을 재사용한다. 별도의 수동 SavedStateHandle을 Screen에서 만들어 Navigation 인자를 가리지 않는다.

## 하단 바와 시스템 여백

AppNavGraph의 Scaffold에 하단 바를 한 번만 둔다. 경로가 네 탭 중 하나인 경우에만 표시한다. 바의 padding을 NavHost에 적용하고 consumeWindowInsets로 소비해 내부 Scaffold가 같은 공간을 다시 비우지 않게 한다. 상위 contentWindowInsets는 0이며 Home·Notification·MyPage·상세 화면의 자체 Scaffold가 상단 여백을 처리한다. Scaffold 없는 기록 Screen은 statusBarsPadding을 적용한다. 키보드·회전·작은 화면의 실제 배치는 사용자 검증 대상이다.

## 현재 경계

- 실제 서버 인증·NFC 하드웨어·파일 업로드·영구 저장은 이번 범위가 아니다.
- 출석 체크는 기존 Fake 스캔 모달과 결과 흐름을 연결했다. Fake는 홈 통계·세션 목록을 동적으로 다시 집계하지 않는다.
- 정정 성공은 기록 화면으로 돌아오지만 기존 Fake는 correctionRequested를 저장하지 않는다. 실제 접수 저장 및 기록 갱신을 구현했다고 주장하지 않는다.
- 알림은 기존 공유 Repository의 읽음 변경을 홈과 함께 관찰한다.
- 마이페이지는 정적 프로필이다. 프로필 관리·알림 설정은 별도 화면이 없어 준비 중 안내를 표시한다.

## 파일별 해설·검증

- [라우트·탭·그래프·Activity 줄별 해설](NAVIGATION_CODE_NOTES.md)
- [Navigation·탭 테스트 줄별 해설](NAVIGATION_TEST_NOTES.md)
- [사용자 실행 명령과 수동 확인](VALIDATION.md)
- [마이페이지와 보류한 연습 과제](../mypage/README.md)
- [기록에서 정정 화면으로 이동하는 변경](../history/HISTORY_NAVIGATION_NOTES.md)

테스트 도구와 저장 상태 정리의 근거: [Android Compose Navigation 테스트](https://developer.android.com/guide/navigation/testing/compose), [NavController API](https://developer.android.com/reference/androidx/navigation/NavController).

## 이해를 위해 스스로 설명해 볼 질문

1. route 문자열과 화면 함수의 역할은 어떻게 다른가?
2. saveState, restoreState, launchSingleTop은 각각 무엇을 보장하는가?
3. 로그인 화면을 제거하지 않으면 뒤로가기에서 어떤 문제가 생기는가?
4. 로그아웃 때 현재 백스택과 저장한 백스택을 모두 비우는 이유는 무엇인가?
5. recordId는 어느 경계에서 String에서 Long으로 바뀌는가?
6. 왜 Content는 NavController 대신 콜백을 받는가?
7. 이번 로그아웃 UI와 나중에 작성할 MyPageViewModel.logout은 각각 어떤 책임인가?
