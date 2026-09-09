# 출석하자

> Kotlin과 Jetpack Compose로 구현 중인 Android 출석 관리 앱입니다.

## 📌 프로젝트 소개

출석하자는 수업 또는 세션 단위의 출석 상태를 확인하고, 사용자가 출석 체크와 출석 기록 조회, 정정 요청까지 이어서 사용할 수 있도록 설계한 Android 앱입니다.

현재 버전은 실제 서비스 서버와 연결된 앱이 아닙니다. 로그인, 홈, 출석 체크, 알림, 기록, 정정 요청, 마이페이지 기본 UI와 화면 이동 흐름을 먼저 구현했고, 데이터는 Fake Repository와 샘플 데이터를 사용합니다.

## 🚧 현재 진행 상태

| 항목 | 상태 |
| --- | --- |
| 서버 연동 | 미연동 |
| 인증/세션 | 실제 토큰 저장 없음 |
| NFC 출석 | 실제 NFC 태그 수신/서버 검증 미연동 |
| 데이터 저장 | 앱 재실행 후 유지되는 영구 저장 미구현 |
| 테스트 | JVM 단위 테스트와 Compose UI 테스트 코드 작성 |

## ✨ 주요 기능

| 기능 | 주요 구현 내용 |
| --- | --- |
| 스플래시 | 앱 시작 및 로그인 화면 진입 |
| 로그인 | 학번·비밀번호 입력, 로딩 및 성공·실패 상태 표시 |
| 홈 | 출석 대상 세션, 출석 요약, 최근 알림 |
| 출석 체크 | NFC 스캔을 가정한 처리 흐름과 결과 다이얼로그 |
| 알림 | 알림 목록 조회 및 읽음 상태 변경 |
| 출석 기록 | 월별 달력, 날짜별 상세 기록, 정정 요청 진입 |
| 정정 요청 | 기록 조회, 사유 입력, 제출 상태 처리 |
| 마이페이지 | 정적 프로필, 계정 메뉴, 로그아웃 확인창 |
| Navigation | 로그인 흐름, 하단 탭, 상세 화면 이동 |

<details>
<summary><strong>기능별 구현 내용과 현재 제한 사항</strong></summary>


### 1. 스플래시

앱 시작 시 스플래시 화면을 표시한 뒤 로그인 화면으로 이동합니다. 현재는 실제 자동 로그인이나 저장된 세션 검증 없이 화면 흐름을 확인하기 위한 진입점입니다.

### 2. 로그인

학번과 비밀번호를 입력해 로그인 흐름을 진행합니다. 입력값 검증, 로딩 상태, 실패 상태를 ViewModel에서 관리하며, 로그인 성공 시 메인 화면 그래프로 이동합니다.

현재 로그인은 실제 서버 인증이 아니라 Fake Repository 기반입니다. 토큰 발급, 토큰 저장, 만료 처리, 자동 로그인은 아직 구현하지 않았습니다.

### 3. 홈

오늘 출석 대상 세션, 출석 요약, 최근 알림, 빠른 이동 버튼을 제공합니다. 여러 Repository의 Flow를 조합해 화면 상태를 구성하는 구조를 연습했습니다.

홈 화면의 통계와 세션 목록은 샘플 데이터 기반입니다. 출석 체크 후 서버에서 다시 집계된 최신 통계를 받아오는 흐름은 아직 없습니다.

### 4. 출석 체크

NFC 스캔을 가정한 다이얼로그와 출석 결과 다이얼로그를 제공합니다. 출석 가능 세션 선택, 스캔 중 상태, 성공/실패 결과 표시를 ViewModel과 UI 상태로 분리했습니다.

현재는 실제 NFC 하드웨어 이벤트를 받지 않습니다. Fake Repository가 지연 후 성공/실패 결과를 반환하는 방식으로 화면 흐름을 확인합니다.

### 5. 알림

알림 목록을 보여주고 읽음 상태를 변경할 수 있습니다. 홈 화면에서도 최근 알림을 함께 관찰하므로, 같은 Repository 데이터를 여러 화면에서 공유하는 흐름을 확인할 수 있습니다.

현재 알림은 서버 푸시 또는 실시간 알림 API와 연결되어 있지 않습니다.

### 6. 내 출석 기록

월별 출석 기록을 달력 형태로 확인하고, 선택한 날짜의 기록 상세를 볼 수 있습니다. `YearMonth`, 날짜 선택 상태, 기록 목록을 조합해 달력 UI와 상세 UI를 구성했습니다.

기록 화면에서는 출석 상태별 표시와 정정 요청 진입 흐름을 제공합니다.

### 7. 출석 정정 요청

출석 기록에서 특정 기록을 선택해 정정 요청 화면으로 이동합니다. Navigation 인자로 `recordId`를 전달하고, `SavedStateHandle`을 통해 ViewModel이 대상 기록을 조회합니다.

정정 사유와 상세 내용을 입력하고 제출할 수 있으며, 공백 검증, 제출 중 중복 방지, 성공/실패 상태를 테스트했습니다.

현재 정정 요청은 실제 서버에 접수되지 않습니다. Fake Repository가 요청 흐름을 흉내 내며, 제출 후 기존 기록의 `correctionRequested` 값이 영구적으로 갱신되는 구조도 아직 아닙니다.

### 8. 마이페이지 기본 UI

정적 프로필, 계정 메뉴, 로그아웃 확인창을 구현했습니다. 프로필 관리와 알림 설정은 아직 실제 화면이 없어 준비 중 안내를 표시합니다.

현재 로그아웃은 인증 세션 삭제가 아니라 Navigation 백스택과 저장된 탭 상태를 정리하고 로그인 화면으로 이동하는 UI 흐름입니다. `MyPageViewModel`, `AuthRepository.logout()` 호출 이전, `MyPageViewModelTest`는 다음 연습 과제로 남겨두었습니다.

### 9. Navigation

스플래시, 로그인, 메인 탭, 출석 체크, 정정 요청 화면을 하나의 Navigation 그래프로 연결했습니다.

```text
MainActivity
  -> AttendanceTheme
  -> AppNavGraph
      -> Splash
      -> Login
      -> Main
          -> Home
          -> Notification
          -> History
          -> MyPage
      -> Attendance
      -> Correction
```

하단 탭은 홈, 알림, 기록, 마이페이지에서만 표시됩니다. 출석 체크와 정정 요청은 상세 화면처럼 진입하며, 뒤로가기 또는 완료 시 이전 흐름으로 복귀합니다.

</details>

## 🖼️ 화면

| 화면 | 캡처 |
| --- | --- |
| 스플래시 |  |
| 로그인 |  |
| 홈 |  |
| 출석 체크 |  |
| 출석 결과 |  |
| 알림 목록 |  |
| 내 출석 기록 |  |
| 출석 정정 요청 |  |
| 마이페이지 |  |
| 로그아웃 확인 |  |

## 🛠️ 기술 스택

| 구분 | 사용 기술 |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | ViewModel, UiState, Repository Pattern |
| State | StateFlow, Flow combine |
| Navigation | Navigation Compose |
| DI | 수동 DI, AppContainer |
| Async | Kotlin Coroutines |
| Test | JUnit4, kotlinx-coroutines-test, Turbine, Truth, MockK, Compose UI Test |
| Build | Gradle, Android Gradle Plugin |

Retrofit과 Gson 의존성은 추후 서버 연동을 대비해 포함되어 있지만, 현재 앱의 주요 화면 데이터는 아직 Retrofit API로 가져오지 않습니다.

## 📁 프로젝트 구조

<details>
<summary><strong>디렉터리 구조와 영역별 역할</strong></summary>


```text
app/src/main/java/com/example/attendance
├── AttendanceApp.kt
├── MainActivity.kt
├── core
│   ├── data/repository
│   │   ├── AttendanceRepository.kt
│   │   ├── AuthRepository.kt
│   │   └── NotificationRepository.kt
│   ├── designsystem/component
│   ├── di/AppContainer.kt
│   ├── model
│   └── navigation
│       ├── AppNavGraph.kt
│       ├── BottomNavigationBar.kt
│       └── ScreenRoute.kt
├── feature
│   ├── attendance
│   ├── auth
│   ├── history
│   ├── home
│   ├── mypage
│   ├── navigation
│   ├── notification
│   └── splash
└── ui/theme
```

### 구조 설명

| 영역 | 역할 |
| --- | --- |
| `core/model` | 화면과 Repository에서 함께 사용하는 도메인 모델과 샘플 데이터 |
| `core/data/repository` | 기능별 데이터 계약과 Fake 구현 |
| `core/di` | 앱 전체에서 사용할 Repository 인스턴스를 생성하는 수동 DI 컨테이너 |
| `core/designsystem` | 여러 화면에서 재사용하는 버튼, 카드, 다이얼로그, 행 컴포넌트 |
| `core/navigation` | 화면 Route, 하단 탭, NavHost 연결 |
| `feature/*/presentation` | 각 기능의 Screen, Content, ViewModel |
| `feature/*/*.md` | 기능별 구현 흐름, 코드 해설, 검증 안내 문서 |

</details>

## 🧩 아키텍처 방향

이 프로젝트는 화면을 먼저 만들고, 화면 상태와 데이터 흐름을 점진적으로 분리하는 방식으로 진행했습니다.

```text
User Event
  -> Screen callback
  -> ViewModel
  -> Repository interface
  -> Fake Repository
  -> UiState update
  -> Compose recomposition
```

UI는 가능한 한 상태를 전달받아 그리는 형태로 두고, 비즈니스 흐름은 ViewModel과 Repository로 이동시켰습니다. 다만 모든 화면이 같은 수준으로 완성된 것은 아니며, 마이페이지처럼 일부 기능은 아직 ViewModel 이전 단계로 남아 있습니다.

## 💡 구현하면서 중점적으로 다룬 내용

- Compose의 상태 기반 UI 구성
- `StateFlow`와 `collectAsStateWithLifecycle`을 사용한 화면 상태 관찰
- ViewModel에서 로딩, 성공, 실패, 입력 검증 상태 관리
- Repository 인터페이스와 Fake 구현을 분리한 테스트 가능한 구조
- Navigation Compose의 route, argument, nested graph, bottom tab 관리
- `SavedStateHandle`을 사용한 정정 요청 대상 recordId 전달
- JVM 단위 테스트와 Compose UI 테스트를 함께 작성하는 방식
- 기능별 README와 코드 해설 문서를 통한 구현 의도 기록

## 🧪 테스트

| 구분 | 검증 대상 | 실행 환경 |
| --- | --- | --- |
| JVM 단위 테스트 | ViewModel 상태 전환, 입력 검증, Repository 호출, Flow 조합 | 로컬 JVM |
| Compose UI 테스트 | 화면 표시, 입력, 버튼, 다이얼로그, Navigation | 에뮬레이터 또는 Android 기기 |

<details>
<summary><strong>JVM 단위 테스트 목록 · 실행 명령 · 보고서</strong></summary>


### JVM 단위 테스트

ViewModel의 상태 전환, 입력 검증, Repository 호출, Flow 조합을 검증합니다.

작성된 주요 테스트:

- `LoginViewModelTest`
- `LoginViewModelMockkTest`
- `HomeViewModelTest`
- `AttendanceViewModelTest`
- `NotificationViewModelTest`
- `HistoryViewModelTest`
- `CorrectionViewModelTest`

실행 명령:

```powershell
cd D:\Dev\attendance-app
.\gradlew.bat :app:testDebugUnitTest
```

보고서 위치:

```text
app/build/reports/tests/testDebugUnitTest/index.html
```

</details>

<details>
<summary><strong>Compose UI 테스트 목록 · 실행 명령 · 보고서</strong></summary>

### Compose UI 테스트

화면 표시, 버튼 클릭, 입력, 다이얼로그, Navigation 흐름을 검증합니다.

작성된 주요 테스트:

- `SplashScreenTest`
- `LoginScreenTest`
- `HomeScreenTest`
- `AttendanceScreenTest`
- `NotificationScreenTest`
- `AttendanceHistoryScreenTest`
- `CorrectionRequestScreenTest`
- `MyPageScreenTest`
- `AppNavGraphTest`
- `BottomNavigationBarTest`

실행 명령:

```powershell
cd D:\Dev\attendance-app
adb devices
.\gradlew.bat :app:connectedDebugAndroidTest
```

보고서 위치:

```text
app/build/reports/androidTests/connected/debug/index.html
```

</details>

과거 실행 확인 범위는 저장소의 기능별 검증 문서를 기준으로 합니다.

## ▶️ 실행 방법

### 필요 환경

- Android Studio
- JDK 17 이상 권장
- Android SDK 36
- Android Emulator 또는 Android 기기

<details>
<summary><strong>앱 및 UI 테스트 APK 빌드 명령</strong></summary>

### 앱 빌드

```powershell
cd D:\Dev\attendance-app
.\gradlew.bat :app:assembleDebug
```

빌드 결과:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### UI 테스트 APK 빌드

```powershell
cd D:\Dev\attendance-app
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest
```

UI 테스트 실행과 APK 빌드는 별개입니다. 실제 UI 테스트를 실행하려면 기기 또는 에뮬레이터가 `adb devices`에서 `device` 상태로 연결되어 있어야 합니다.

</details>

## ⚠️ 현재 한계

이 프로젝트는 아직 진행 중이므로 다음 기능은 실제 서비스 수준으로 완성되지 않았습니다.

| 영역 | 미구현 또는 미연동 항목 |
| --- | --- |
| 인증·세션 | 서버 로그인 API, 토큰 발급·저장·만료 처리, 자동 로그인, 로그아웃 시 실제 세션 삭제 |
| 출석 | 실제 NFC 태그 수신, 서버 기반 출석 검증 및 기록 조회 |
| 정정 요청 | 서버 접수 및 기록 갱신 |
| 알림 | 알림 API 및 푸시 알림 |
| 프로필 | 사용자 프로필 조회·수정 |
| 영구 저장 | 로컬 DB 또는 DataStore 기반 저장 |
| 배포 | 배포용 Release 설정 |

## 🗓️ 이후 작업 계획

- [ ] `MyPageViewModel` 작성
- [ ] `AuthRepository.logout()` 호출을 ViewModel로 이전
- [ ] `MyPageViewModelTest` 작성
- [ ] 실제 서버 API 명세에 맞춘 Retrofit 연동
- [ ] 인증 토큰 저장 구조 추가
- [ ] NFC 태그 수신 흐름 연결
- [ ] 출석 기록과 정정 요청의 실제 데이터 갱신
- [ ] 에러 처리와 빈 상태 UI 보강
- [ ] Release 빌드와 배포 설정 정리

## 🗂️ 작업 기록

현재 `develop`에 반영된 주요 작업 흐름입니다.

<details>
<summary><strong>단계별 작업 기록 보기</strong></summary>


| 순서 | 내용 |
| --- | --- |
| 1 | 프로젝트 초기 생성 |
| 2 | 디자인 시스템 기반 구축 |
| 3 | SampleData 기반 홈 화면 초안 구현 |
| 4 | Repository 패턴 데이터 레이어와 수동 DI 구성 |
| 5 | 스플래시 화면 구현 |
| 6 | 로그인 화면과 테스트 도구 도입 |
| 7 | 홈 화면 구현 |
| 8 | 출석 체크 화면 구현 |
| 9 | 알림 화면 구현 |
| 10 | 내 출석 기록 화면 구현 |
| 11 | 출석 정정 요청 화면과 테스트 구현 |
| 12 | 전체 Navigation 연결과 마이페이지 기본 UI 구현 |

</details>

## 🌱 정리

출석하자는 아직 서버와 연결된 완성 앱은 아니지만, Android 앱의 기본 화면 구성부터 상태 관리, Repository 분리, Navigation, ViewModel 테스트, Compose UI 테스트까지 단계적으로 구현한 프로젝트입니다.

특히 단순 화면 구현에 그치지 않고 각 기능의 상태 흐름과 검증 방식을 문서화해, 이후 실제 서버 연동과 기능 확장으로 이어갈 수 있는 구조를 만드는 데 초점을 두었습니다.
