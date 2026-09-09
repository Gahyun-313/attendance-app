# 📱 출석하자

![Android](https://img.shields.io/badge/Android-Native-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)

수업별 출석 체크, 출석 기록 조회와 정정 요청을 제공하는 Kotlin·Jetpack Compose 기반 Android 애플리케이션입니다.

화면 구현부터 ViewModel 상태 관리, Repository 분리, Navigation 연결과 테스트까지 단계적으로 구현하고 있습니다.

> 🚧 **개발 진행 중입니다.** 현재는 Fake Repository와 샘플 데이터로 동작하며, 서버 인증·실제 NFC 태그 수신·영구 저장은 연결되지 않았습니다. 문서의 구현 범위는 마이페이지 기본 UI와 Navigation 연결까지이며, 마이페이지 ViewModel 연습 과제는 포함하지 않습니다.

## 🖼️ 주요 화면

| 로그인 | 홈 | 출석 체크 |
| --- | --- | --- |
|  |  |  |
| 학번·비밀번호 입력과 로그인 흐름 | 출석 대상 세션·통계·최근 알림 | NFC 스캔을 가정한 처리 화면 |

| 출석 결과 | 알림 | 출석 기록 |
| --- | --- | --- |
|  |  |  |
| 출석 처리 결과 다이얼로그 | 알림 목록과 읽음 상태 | 월별 달력과 날짜별 기록 |

| 정정 요청 | 마이페이지 | 로그아웃 확인 |
| --- | --- | --- |
|  |  |  |
| 대상 기록 확인과 사유 제출 | 정적 프로필과 계정 메뉴 | 확인 후 로그인 화면으로 이동 |

| 스플래시 |
| --- |
|  |
| 앱 시작 화면 |

## 📖 서비스 소개

**출석하자는 출석 대상자(학생)가 수업별 출석을 체크하고, 자신의 출석 기록을 확인하며 정정을 요청할 수 있는 Android 앱입니다.**

홈에서 출석 대상 수업과 출석 요약을 확인하고, 출석 체크부터 기록 조회·정정 요청·알림 확인까지 하나의 앱에서 이어갈 수 있도록 구성했습니다.

현재는 **Fake Repository와 샘플 데이터로 동작하는 개발 단계**입니다. Compose UI, ViewModel 상태 관리, Repository 분리와 Navigation을 구현했으며, 실제 서버 인증·NFC 태그 수신·영구 저장은 아직 연결되지 않았습니다.

<details>
<summary><strong>🌐 전체 서비스 구성 및 관련 저장소</strong></summary>

출석하자는 **학생용 Android 앱**, **관리자용 어드민 웹**, **공통 백엔드 API**로 구성하는 출석 관리 서비스를 목표로 합니다. 이 저장소는 학생이 사용하는 Android 앱을 다룹니다.

| 구성 | 사용자 | 역할 |
| --- | --- | --- |
| 📱 Android 앱 | 출석 대상자(학생) | 출석 체크, 본인 기록 조회, 정정 요청, 알림 확인 |
| 🖥️ 어드민 웹 | 관리자 | 수업·출석 현황 관리, 정정 요청 확인·처리 |
| ⚙️ 백엔드 API | 앱·웹 공통 | 인증, 출석 검증, 데이터 저장·조회와 처리 결과 공유 |

### 서비스 연동 목표

```text
학생 · Android 앱          백엔드 API               관리자 · 어드민 웹

출석 체크 ──────────────> 출석 검증·저장 ──────────> 출석 현황 확인
본인 기록 조회 <───────── 출석 데이터 제공 ────────> 수업별 기록 관리
정정 요청 제출 ─────────> 요청 접수 ──────────────> 요청 확인·처리
처리 결과 확인 <───────── 변경 사항 반영 <───────── 처리 결과 전달
```

위 흐름은 연동 후의 목표 구성이며, 현재 Android 앱은 실제 백엔드 API 대신 Fake Repository를 사용합니다.

### 관련 저장소

| 프로젝트 | 저장소 |
| --- | --- |
| 📱 학생용 Android 앱 | 현재 저장소 |
| 🖥️ 관리자용 어드민 웹 | [attendance-web-claude](https://github.com/Gahyun-313/attendance-web-claude) |
| ⚙️ 백엔드 API | [attendance-be](https://github.com/Gahyun-313/attendance-be) |

</details>

## 📌 프로젝트 정보

| 항목 | 내용 |
| --- | --- |
| 플랫폼 | Native Android |
| UI | Kotlin, Jetpack Compose, Material 3 |
| 구현 기준 | `develop`의 Navigation·마이페이지 기본 UI 구현 범위 |
| 구조 | ViewModel, UiState, Repository Pattern, 수동 DI |
| 데이터 | Fake Repository와 메모리 내 샘플 데이터 |
| 검증 구성 | JVM 단위 테스트, Compose UI 테스트 |
| 진행 상태 | 개발 중; 서버·NFC 하드웨어·영구 저장 미연동 |

## 👨‍💻 구현 범위

- 스플래시부터 로그인, 메인 탭과 상세 화면까지의 화면 흐름
- 로그인 입력 상태와 로딩·성공·실패 처리
- 홈 화면의 세션·통계·최근 알림 구성
- 출석 스캔 및 결과 다이얼로그와 Fake 출석 처리
- 알림 목록 조회와 읽음 상태 공유
- 출석 달력, 날짜 선택과 기록 필터링
- 출석 정정 대상 조회, 필수 사유 검증과 중복 제출 방지
- 마이페이지 기본 UI와 로그아웃 화면 이동
- 기능별 ViewModel·UI 테스트와 구현 해설 문서

마이페이지의 `MyPageViewModel`, `AuthRepository.logout()` 호출 연결, `MyPageViewModelTest`는 구현 중입니다.

## 🛠️ Tech Stack

| Category | Stack |
| --- | --- |
| Core | Kotlin, Android SDK |
| UI | Jetpack Compose, Material 3 |
| Architecture | ViewModel, UiState, Repository Pattern |
| State | StateFlow, Flow, collectAsStateWithLifecycle |
| Async | Kotlin Coroutines |
| Navigation | Navigation Compose, SavedStateHandle |
| DI | 수동 DI, AppContainer |
| Unit Test | JUnit4, kotlinx-coroutines-test, Turbine, Truth, MockK |
| UI Test | Compose UI Test |
| Build | Gradle, Android Gradle Plugin |

Retrofit·Gson 의존성은 포함되어 있으나 현재 화면 데이터를 가져오는 서버 API 연동에는 사용하지 않습니다.

## ✨ 주요 구현

### 상태 기반 Compose UI

사용자 입력과 비동기 처리 결과를 UiState로 표현하고, 화면은 상태를 관찰해 표시합니다. ViewModel은 Repository 인터페이스에 의존하며, UI와 데이터 처리 책임을 분리했습니다.

[아키텍처 자세히 보기](./docs/ARCHITECTURE.md)

### Flow를 조합한 화면 상태

홈은 여러 Repository 데이터를 조합해 표시하고, 출석 기록은 통계·기록·달력 선택 상태를 결합합니다. 월과 선택 날짜는 하나의 상태로 관리해 월 이동 시 함께 변경합니다.

[상태 흐름 자세히 보기](./docs/STATE_FLOW.md)

### 기록 ID 기반 정정 요청

Navigation에서 전달한 `recordId`를 `SavedStateHandle`로 받아 정정 대상을 조회합니다. 잘못된 대상, 조회 실패, 필수 사유 누락, 제출 중 상태와 실패 후 재시도를 처리합니다.

[구현 판단과 제한 사항 보기](./docs/IMPLEMENTATION_NOTES.md)

### 화면 이동과 탭 상태 관리

로그인 완료 후 로그인 화면을 백스택에서 제거하고, 하단 탭 이동 시 상태를 저장·복원합니다. 로그아웃은 Main 그래프와 저장된 탭 상태를 정리하는 UI 흐름으로 구현했습니다.

[Navigation 자세히 보기](./docs/NAVIGATION_FLOW.md)

### ViewModel과 UI 테스트 분리

상태 전환과 Repository 호출은 JVM 테스트로, 화면 표시·입력·다이얼로그와 화면 이동은 Compose UI 테스트로 검증하도록 구성했습니다.

[테스트 구성과 실행 방법 보기](./docs/TESTING.md)

## 🔧 설계 포인트

| 주제 | 적용 내용 |
| --- | --- |
| 데이터 소스 분리 | Repository 인터페이스와 Fake 구현을 분리하고 AppContainer에서 제공 |
| 달력 상태 일관성 | 월 이동과 날짜 선택 초기화를 하나의 상태 갱신으로 처리 |
| 중복 제출 방지 | 코루틴 실행 전에 제출 상태를 설정하고 연속 요청 차단 |
| 취소 처리 | 정정 요청에서 CancellationException을 일반 오류와 구분 |
| 탭 이동 | 동일 탭 재선택 방지와 저장 상태 복원 |
| 구현 범위 구분 | Fake 성공 응답과 실제 서버 반영을 문서에서 구분 |

[설계 배경과 현재 제한 사항 보기](./docs/IMPLEMENTATION_NOTES.md)

## ⚠️ 현재 한계

| 영역 | 현재 상태 |
| --- | --- |
| 로그인 | Fake 인증; 실제 계정 확인, 토큰 발급·저장·만료, 자동 로그인 미구현 |
| 출석 | Fake가 지연 후 출석 결과 반환; 실제 NFC·서버 검증 미연동 |
| 기록·통계 | 샘플 데이터; 출석·정정 제출에 따른 기록 및 통계 갱신 미구현 |
| 달력 | 월 이동 UI는 있으나 서버의 연월별 조회·필터링 미구현 |
| 정정 요청 | Fake 성공 응답; 서버 접수 및 기록 변경 미구현 |
| 알림 | 메모리 내 읽음 처리; 알림 API·푸시 미연동 |
| 마이페이지 | 정적 UI; 프로필 조회·수정 및 실제 세션 삭제 미구현 |
| 저장·배포 | 영구 저장과 배포용 Release 설정 미완료 |

## 🗓️ 이후 작업 계획

- [ ] 마이페이지 ViewModel과 로그아웃 Repository 호출·단위 테스트
- [ ] 서버 API 명세에 맞춘 Retrofit 연동
- [ ] 인증 토큰 저장과 세션 생명주기 처리
- [ ] 실제 NFC 태그 수신과 출석 검증
- [ ] 연월별 기록 조회, 출석·정정 요청 이후 데이터 갱신
- [ ] 오류·빈 상태 UI 보강
- [ ] Release 빌드와 배포 설정 정리

## 📚 Documentation

| Document | Description |
| --- | --- |
| [Architecture](./docs/ARCHITECTURE.md) | 앱 구조, 수동 DI와 계층별 역할 |
| [State Flow](./docs/STATE_FLOW.md) | UiState, Flow 조합과 사용자 이벤트 전달 |
| [Navigation Flow](./docs/NAVIGATION_FLOW.md) | 로그인·탭·상세 화면 이동과 백스택 |
| [Testing](./docs/TESTING.md) | 테스트 범위, 실행·빌드 방법과 보고서 |
| [Implementation Notes](./docs/IMPLEMENTATION_NOTES.md) | 설계 판단, 구현 한계와 작업 단계 |
