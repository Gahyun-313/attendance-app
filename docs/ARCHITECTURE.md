# 🧩 Architecture

[README로 돌아가기](../README.md)

## 구현 기준

현재 앱은 단일 Android app 모듈 안에서 기능별 패키지를 나누고, ViewModel과 Repository 인터페이스를 통해 UI와 데이터 처리를 분리합니다. DI는 Hilt가 아닌 `DefaultAppContainer`를 사용하는 수동 주입 방식입니다.

## 디렉터리 구조

```text
app/src/main/java/com/example/attendance
  AttendanceApp.kt
  MainActivity.kt
  core/
    data/repository/
    designsystem/component/
    di/AppContainer.kt
    model/
    navigation/
  feature/
    splash/
    auth/
    home/
    attendance/
    notification/
    history/
    mypage/
    navigation/
  ui/theme/
```

| 영역 | 책임 |
| --- | --- |
| AttendanceApp | 앱 시작 시 DefaultAppContainer 생성 |
| MainActivity | 테마 적용과 AppNavGraph 호스팅 |
| core/di | 앱에서 공유하는 Repository 인스턴스 제공 |
| core/data/repository | 데이터 계약과 Fake 구현 |
| core/model | 출석·세션·통계 모델과 SampleData |
| core/designsystem | 공통 UI 컴포넌트 |
| core/navigation | Route, NavHost, 하단 탭 |
| feature/*/presentation | 기능별 화면, 상태와 ViewModel |
| feature/navigation | Navigation 학습 문서; 실제 코드는 core/navigation |
| ui/theme | Compose 테마 |

## 의존성 생성

```text
AttendanceApp.onCreate()
  -> DefaultAppContainer
      -> FakeAuthRepository
      -> FakeAttendanceRepository
      -> FakeNotificationRepository

ViewModel.Factory
  -> Application의 container 접근
  -> Repository 인터페이스를 생성자에 전달
  -> ViewModel 생성
```

Repository를 앱 컨테이너에서 공유하므로 각 화면이 독립적인 Fake 데이터를 새로 만드는 대신 같은 인스턴스를 사용할 수 있습니다. 예를 들어 알림 목록의 읽음 변경을 홈에서도 관찰할 수 있습니다.

생성자 주입은 테스트에서 별도 Fake 또는 Mock을 전달할 수 있게 합니다. 서버 연동 시에는 인터페이스 계약을 검토하고 실제 구현체와 컨테이너 구성을 변경해야 합니다. 현재 구조만으로 인증·캐시·네트워크 오류 정책까지 완성된 것은 아닙니다.

## 화면과 데이터의 관계

```text
사용자 조작
  -> Screen/Content의 callback
  -> ViewModel의 이벤트 함수
  -> Repository 호출
  -> 처리 결과 또는 Flow 값
  -> UiState 갱신
  -> Compose가 관찰한 상태로 화면 표시
```

Repository의 Flow 방출과 함수 호출은 구분됩니다. 조회 Flow는 ViewModel이 구독하며, 출석 체크·정정 요청 같은 일회성 작업은 suspend 함수로 호출합니다.

| 계층 | 처리 내용 |
| --- | --- |
| UI | 화면 표시, 입력 이벤트 전달, 화면 이동 콜백 |
| ViewModel | 입력 상태, 로딩·오류·성공 상태, 데이터 조합 |
| Repository | 조회·변경 계약과 데이터 공급 |
| Fake | 지연 및 샘플 응답으로 데이터 흐름 재현 |

마이페이지는 아직 정적 UI와 콜백 단계이며 전용 ViewModel은 없습니다. 모든 화면이 동일한 분리 수준에 도달한 상태로 설명하지 않습니다.

## 데이터의 현재 수명

Fake 데이터는 메모리에 존재합니다. 프로세스 종료 후 유지하는 DB나 DataStore는 연결되어 있지 않습니다. Navigation의 탭 상태 저장·복원도 데이터 영구 저장과는 별개입니다.

`FakeAttendanceRepository`는 기록을 MutableStateFlow로 보관하지만, 현재 출석·정정 함수는 이 값을 변경하지 않습니다. Flow 사용 자체가 기록 갱신을 보장하지는 않습니다.

## 코드 근거

- [Application](../app/src/main/java/com/example/attendance/AttendanceApp.kt)
- [앱 진입점](../app/src/main/java/com/example/attendance/MainActivity.kt)
- [AppContainer](../app/src/main/java/com/example/attendance/core/di/AppContainer.kt)
- [AttendanceRepository](../app/src/main/java/com/example/attendance/core/data/repository/AttendanceRepository.kt)
