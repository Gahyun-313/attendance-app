# 🧭 Navigation Flow

[README로 돌아가기](../README.md)

## 화면 그래프

```text
MainActivity -> AttendanceTheme -> AppNavGraph
  Splash
    -> Login
      -> Main (nested graph)
          Home (시작 화면)
          Notification
          History
          MyPage
          Attendance
          Correction(recordId: Long)
```

출석 체크와 정정 요청도 Main 그래프 안에 속합니다. 하단 바는 홈·알림·기록·마이페이지 경로에서만 표시됩니다.

## 진입과 로그인

| 이벤트 | 이동 | 스택 처리 |
| --- | --- | --- |
| 스플래시 대기 완료 | Splash -> Login | Splash 제거 |
| 로그인 성공 | Login -> Main/Home | Login 제거 |
| 출석 체크 진입 | Home -> Attendance | 상세 화면 추가 |
| 정정 요청 진입 | History -> Correction | recordId 전달 |
| 정정 요청 완료·뒤로가기 | Correction -> 이전 화면 | popBackStack |
| 로그아웃 확인 | MyPage -> Login | 저장된 탭 상태와 Main 제거 |

로그인 성공은 현재 Fake 인증 결과입니다. 그래프 이동이 실제 토큰 발급이나 세션 검증을 의미하지는 않습니다.

## 하단 탭

`navigateToTab`은 허용된 탭 경로를 확인하고 현재 탭을 다시 누르면 이동하지 않습니다.

- `popUpTo(Home)`으로 탭 전환 시 스택 기준을 정합니다.
- `saveState = true`로 떠나는 경로의 상태를 저장합니다.
- `restoreState = true`로 이전 탭 상태를 복원합니다.
- `launchSingleTop = true`로 맨 위 목적지의 중복 생성을 방지합니다.

홈의 알림·기록 바로가기도 같은 탭 이동 함수를 사용합니다.

## 정정 대상 전달

```text
기록의 정정 요청 선택
  -> Correction.createRoute(recordId)
  -> NavArgument: LongType
  -> SavedStateHandle
  -> CorrectionViewModel
  -> Repository.getRecord(recordId)
```

화면 사이에 기록 객체 전체를 넘기지 않고 ID를 전달한 뒤 ViewModel이 조회합니다. 잘못된 ID나 찾을 수 없는 기록은 오류 상태로 처리합니다.

## 로그아웃 경계

현재 로그아웃은 각 탭의 저장된 백스택을 정리하고 Main 그래프를 제거한 뒤 로그인 화면으로 이동합니다. `AuthRepository.logout()` 호출이나 인증 데이터 삭제는 아직 연결되지 않았습니다.

화면 이동 테스트가 통과하더라도 실제 서버 세션 종료가 검증된 것은 아닙니다. 전용 MyPageViewModel과 Repository 연결은 후속 과제입니다.

## 코드 근거

- [AppNavGraph](../app/src/main/java/com/example/attendance/core/navigation/AppNavGraph.kt)
- [BottomNavigationBar](../app/src/main/java/com/example/attendance/core/navigation/BottomNavigationBar.kt)
- [기능별 Navigation 해설](../app/src/main/java/com/example/attendance/feature/navigation/README.md)
