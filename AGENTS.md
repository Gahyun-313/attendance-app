# attendance-app 작업 지침

이 문서는 이전 대화 없이 작업을 이어가기 위한 시작점이다. 작업 규칙은 여기서 확인하고, 단계별 요구사항과 상세 해설은 아래 경로에서 직접 찾아 읽는다. 사용자가 매번 두 저장소의 관계나 작업 방식을 다시 설명하도록 요구하지 않는다.

## 1. 저장소 역할과 판단 기준

- 실제 구현 대상은 이 문서가 있는 저장소 `attendance-app`이다. 기본 로컬 경로는 `D:\Dev\attendance-app`이다.
- 형제 디렉터리 `../attendance-app-claude`는 학습 가이드와 후속 단계 참고 구현 저장소다. 기본 로컬 경로는 `D:\Dev\attendance-app-claude`이다.
- 실제 앱 코드·테스트·기능 설명 문서는 `attendance-app`에 작성한다. 가이드와 진행 이력은 참고 저장소의 해당 문서에 기록한다.
- 경로를 옮겼다면 현재 저장소 루트와 형제 디렉터리를 기준으로 찾는다. 참고 저장소가 없으면 접근 가능한 자료로 확인 가능한 작업을 진행하고, 해당 단계의 요구사항 등 필수 자료가 없을 때만 그 자료의 위치를 사용자에게 묻는다. 내용을 추측해 구현하지 않는다.
- 최신 사용자 요청을 우선한다. 현재 구현 여부는 실제 소스와 Git 기록으로, 단계의 목표·커밋 구성은 학습 가이드로, 테스트 성공 여부는 해당 변경에 대한 실행 결과로 판단한다.
- 문서와 코드가 다르면 불일치를 알리고 근거를 확인한다. 문서의 체크박스, 과거 브랜치명, 참고 구현의 존재만으로 현재 앱의 완료 상태를 판단하지 않는다.

## 2. 새 세션 시작 순서

1. 실제 앱 루트에서 현재 경로, 브랜치, 변경 상태, 최근 커밋을 확인한다.

   ```powershell
   Get-Location
   git rev-parse --show-toplevel
   git status --short --branch
   git log -5 --oneline
   ```

2. 이 문서의 현재 구현 경계를 읽고, 요청된 단계의 가이드와 관련 기능의 README·설명 MD·소스·테스트를 확인한다. 참고 저장소의 진행 기록도 함께 확인한다.
3. 이해한 내용, 확인한 현재 브랜치, 수정할 파일과 범위, 커밋 요청이 있다면 예정 커밋 메시지를 설명한다.
4. 사용자가 이해한 바만 먼저 요청했다면 설명 후 기다린다. 사용자가 작업을 요청했거나 이미 시작을 승인했다면 같은 범위를 반복 확인하지 않고 진행한다.
5. 기존 사용자 변경을 보존한다. 두 저장소를 수정해야 한다면 각각 Git 상태와 적용 지침을 확인하고, 요청과 관련된 파일만 다룬다.

브랜치 생성·전환과 푸시는 사용자가 직접 한다. 현재 브랜치가 가이드의 예시 브랜치와 달라도 임의로 전환하지 않는다. 파일 수정만 요청하면 스테이징이나 커밋도 하지 않는다.

## 3. 자료 위치와 읽는 목적

아래 상대 경로는 실제 앱 루트 기준이다. 형제 저장소 링크는 로컬 배치를 기준으로 하며 GitHub에서 다른 저장소로 연결되는 링크라고 가정하지 않는다.

| 자료 | 경로 | 확인할 내용 |
| --- | --- | --- |
| 단계별 학습 가이드 | `../attendance-app-claude/LEARNING_GUIDE.md` | 요청 단계의 목표, 작업 파일, 구현 예시, 테스트, 파일별 커밋 메시지, PR 예시 |
| 후속 작업 목록 | `../attendance-app-claude/LEARNING_GUIDE_TODO.md` | 남은 범위와 후속 계획 |
| 실제 구현 진행 기록 | `../attendance-app-claude/IMPLEMENTATION_STATUS.md` | 단계별 이력, 실제 앱의 구현 경계, 과거 검증 근거 |
| 참고 저장소 문맥 | `../attendance-app-claude/CONTEXT.md` | 참고 프로젝트의 목적과 상태 |
| 참고 아키텍처 | `../attendance-app-claude/ARCHITECTURE.md` | 후속 구조와 설계 의도; 실제 앱 구조와 비교해서 읽기 |
| Android 개념 설명 | `../attendance-app-claude/android-project-concepts.md` | 문법·생명주기·아키텍처 개념과 비교 설명 |
| 참고 저장소 수정 규칙 | `../attendance-app-claude/AGENTS.md` | 참고 문서를 수정하기 전에 읽을 지침 |
| 기능 소스와 설명 | `app/src/main/java/com/example/attendance/feature/<기능>/` | 현재 구현과 파일별 해설 |
| JVM 단위 테스트 | `app/src/test/java/com/example/attendance/` | 대상 기능 테스트와 공통 테스트 도구 |
| 계측·Compose UI 테스트 | `app/src/androidTest/java/com/example/attendance/` | 기기에서 실행하는 테스트 |

사용자가 “3-3 진행해줘”처럼 단계를 지정하면 가이드에서 해당 절과 커밋·검증 지침을 찾은 뒤 현재 앱 구조에 맞춰 구현한다. 가이드 전체나 참고 소스를 그대로 복사하지 않는다. 가이드와 실제 구조의 차이로 구현 방식을 조정하면 이유와 적용 범위를 설명 문서에 남긴다.

## 4. 현재 구현 경계와 재확인 지점

다음은 2026-09-09 실제 앱의 `feat/screen-navigation-graph` 작업 트리 기준이다. 3-7까지의 기존 구현 위에 3-8 기본 UI·3-9 연결을 추가했으며 사용자가 이번 빌드·전체 JVM·전체 UI 테스트 통과를 확인했다. 새 세션의 브랜치나 최신 상태를 고정하는 지시가 아니며 작업 전 재확인한다.

- 2-1 홈 초안, 2-2 데이터·수동 DI, 3-1 스플래시, 3-2 로그인, 3-3 홈, 3-4 출석 체크, 3-5 알림, 3-6 내 출석 기록의 구현이 있다. 3-7 출석 정정 요청을 추가했으며, 단위 9건·UI 6건에 대해 2026-09-09 사용자가 전체 테스트 통과를 확인했다. 에이전트가 직접 재실행한 결과는 아니다. 상세 문서는 feature/history/CORRECTION_README.md와 CORRECTION_VALIDATION.md를 읽는다. 구현·테스트 코드의 존재와 실행 검증 완료는 별개다.
- 진입점은 `app/src/main/java/com/example/attendance/MainActivity.kt`이며 3-9에서 AppNavGraph를 호스팅하도록 전환했다. 연결 코드는 작성했으나 이번 변경의 빌드·전체 JVM·전체 UI 테스트는 2026-09-09 사용자가 통과를 확인했다. 에이전트 재실행은 없으며 수동 확인은 별도다.
- 수동 DI는 `app/src/main/java/com/example/attendance/core/di/AppContainer.kt`, Repository 인터페이스와 Fake는 `app/src/main/java/com/example/attendance/core/data/repository/`에 있다.
- 현재 수동 DI·Kotlin Result·Fake Repository 구조를 기준으로 확장한다. 참고 저장소의 Hilt·AppResult·SessionRepository·`data/repository/` 구조는 후속 참고 구현이며 해당 전환 요청 없이 가져오지 않는다.
- 기능 디렉터리는 `splash`, `auth`, `home`, `attendance`, `notification`, `history`, `mypage`다. feature/navigation은 실제 core/navigation 코드의 학습 문서 위치다. 새 기능은 요청 단계에 맞춰 추가한다.
- 서버 인증, 실제 NFC 태그 수신 및 서버 검증, 알림 API, 영구 저장은 미연동이다. Fake 지연·샘플 데이터·성공 콜백을 실제 외부 연동으로 설명하지 않는다.
- 진행 순서는 3-7 → 3-8 기본 UI와 UI 테스트 → 3-9 Navigation 선행 → 3-8 연습 과제 재개다. MyPageViewModel, AuthRepository.logout 호출의 ViewModel 이전, MyPageViewModelTest는 사용자 연습 과제로 남겨 임의 구현하지 않는다. 현재 로그아웃은 화면 스택만 정리한다. feature/navigation/README.md와 VALIDATION.md를 먼저 읽는다.
- 참고 진행 기록에는 HistoryViewModel 단위 테스트 3건의 사용자 통과 확인이 있으나 기능의 VALIDATION.md에는 아직 미실행으로 남아 있다. 이처럼 기록이 다르면 실행 시점과 대상 변경을 확인하며, 이번 체크아웃에서 재실행한 결과로 표현하지 않는다.

## 5. 실제 코드 주석과 기능별 설명 MD

목적은 AI의 도움으로 작성한 코드도 사용자가 흐름과 설계 이유를 이해하며 이어갈 수 있도록 하는 것이다. 외부 독자도 읽는 프로젝트 문서로 작성한다.

- 실제 Kotlin 소스에는 일반적인 개발 코드보다 조금 자세한 주석을 작성한다. 주요 책임, 처리 이유, 상태 변경, 주의할 동작을 설명하되 모든 줄에 주석을 붙이지 않는다.
- 상세 줄별 해설은 해당 `feature/<기능>/`의 Markdown에 작성한다. package/import는 생략할 수 있으며 나머지 코드 줄·로직·흐름은 코드 블록 안의 주석으로 설명한다.
- 메서드 바로 위에는 책임, 호출 상황, 처리 순서, 상태 변화와 결과를 묶어서 설명한다. 메서드 내부의 각 로직도 이해할 수 있게 해설한다.
- 설명 코드는 실제 구현의 동작과 순서를 유지한다. 소스 수정 시 대응하는 설명도 함께 수정한다.
- 파일별 책임, 의존 관계, 파일 간 함수 호출, Flow 구독과 상태 전달, 사용자 이벤트부터 UI 반영까지를 설명한다. 실제 호출과 관찰·구독은 구분한다.
- Kotlin·Compose·Coroutine 문법은 정의, 동작 시점·생명주기, 비교 대상, 이 프로젝트에서 사용하는 이유를 설명한다. 아키텍처 선택 이유와 현재 한계, 테스트의 검증 대상도 포함한다.
- 필요하면 실제 파일·함수 이름을 사용한 화살표 흐름도를 넣는다. 계획 중인 연결은 현재 흐름과 구분한다.
- 제목은 `이해를 위해 스스로 설명해 볼 질문`처럼 중립적인 표현을 사용한다.
- 사용자가 코드를 보내 설명만 요청하면 상세 주석을 붙인 코드를 응답한다. 설명 요청을 실제 파일 수정이나 리팩터링 권한으로 해석하지 않는다.

기능 문서는 기존 이름을 우선하며 다음 구성을 기본으로 한다.

| 문서 | 내용 |
| --- | --- |
| `README.md` | 기능 개요, 파일 역할, 호출·상태 흐름, 문법·아키텍처 설명, 이해 질문 |
| `<기능>_STATE_NOTES.md` | UiState·ViewModel의 줄별 해설과 메서드 흐름 |
| `<기능>_UI_NOTES.md` | Screen·Content·Preview의 줄별 해설 |
| `<기능>_TEST_NOTES.md` | 테스트와 보조 도구의 줄별 해설, 검증 의도 |
| `VALIDATION.md` | 작성한 테스트, 실행 절차, 기대 결과, 실제 실행 기록과 미검증 범위 |

예를 들어 auth는 `LOGIN_*_NOTES.md`, history는 `HISTORY_*_NOTES.md`를 사용한다. 기능 문서 완성 후 커밋하며, 요청과 무관한 다른 기능 문서를 일괄 변경하지 않는다.

## 6. 검증은 사용자가 직접 실행

- 별도 실행 요청이 없으면 Gradle 테스트·APK 빌드, 에뮬레이터 시작, APK 설치를 실행하지 않는다.
- 읽기 전용 소스 확인, Git diff 점검, 설명 코드와 실제 소스의 일치 확인은 수행한다.
- 완료 시 실행 위치, 명령, 프로젝트가 요구하는 JDK·Android SDK와 기기 조건, 기대 결과, 보고서 위치를 안내한다. 대상 테스트 클래스는 실제 파일에서 확인한다.
- 테스트 작성 완료, 과거 실행 성공, 이번 변경 후 미실행을 구분한다. PR의 검증 체크박스는 해당 실행 근거가 있는 경우에만 체크한다.
- APK 빌드 성공과 기기 테스트 성공은 별도 결과다. 일부 Gradle 작업 성공을 전체 빌드 성공으로 표현하지 않는다.

다음은 PowerShell 안내용 템플릿이다. `<테스트 클래스 전체 이름>`은 실제 패키지와 클래스명으로 바꿔 제공한다.

```powershell
cd D:\Dev\attendance-app
# 대상 JVM 단위 테스트
.\gradlew.bat :app:testDebugUnitTest --tests "<테스트 클래스 전체 이름>"
# 전체 JVM 단위 테스트가 필요한 경우
.\gradlew.bat :app:testDebugUnitTest
# 앱 APK와 UI 테스트 APK 빌드; 기기 테스트 실행과 별개
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest
# UI 테스트 전 연결 상태 확인: 대상 기기가 device 상태여야 함
adb devices
# 대상 계측·Compose UI 테스트
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=<테스트 클래스 전체 이름>"
```

`adb`가 PATH에 없으면 Android SDK의 `platform-tools/adb.exe` 경로를 안내한다. 테스트는 기대한 개수, 실패 0건, 최종 `BUILD SUCCESSFUL`을 확인한다. JVM 보고서는 `app/build/reports/tests/testDebugUnitTest/index.html`, 기기 보고서는 `app/build/reports/androidTests/connected/debug/index.html`이 기본 위치다. 생성 경로가 다르면 Gradle 출력의 실제 보고서 링크를 따른다.

## 7. 커밋과 PR

- 커밋은 사용자가 요청한 경우에 수행한다. 파일 수정만 요청하면 수정 결과만 남긴다.
- 단계별 구현은 `LEARNING_GUIDE.md`의 해당 절에 있는 파일별 커밋 메시지와 묶음을 따른다. 메시지는 실제 변경 범위를 정확히 나타내야 한다.
- 설명 MD는 적절한 `docs(<기능>): ...` 메시지로 커밋할 수 있다. 공통 작업 지침은 `docs: ...`를 사용한다.
- 사용자가 파일별 커밋을 요청하면 각 파일을 따로 커밋한다. 다른 요청에서는 가이드의 묶음을 따른다.
- 각 커밋 전에 정확한 파일만 스테이징하고 `git diff --cached --check`, `git diff --cached --stat`, 실제 staged diff를 확인한다. 기존 staged 변경이 있으면 현재 작업의 커밋에 섞이지 않도록 확인한다.
- 무관한 변경을 일괄 스테이징·커밋하지 않는다. 커밋 후 내역과 남은 변경을 확인해 알린다.
- PR 본문은 해당 단계의 가이드 형식을 참고하되 실제 변경·문서·검증 결과·미연결 범위에 맞춘다. 가이드의 성공 체크나 구현 주장을 그대로 복사하지 않는다.
- 브랜치 생성·전환, 푸시와 PR 제출은 사용자가 직접 진행한다.

## 8. 문서 동기화와 작업 종료

- 기능 변경 시 실제 앱의 관련 README·줄별 설명·VALIDATION.md를 함께 갱신한다.
- 단계 진행 상태가 바뀌면 이 문서의 스냅샷도 확인 날짜와 근거에 맞춰 갱신한다. 현재 브랜치를 미래 세션의 필수 브랜치로 고정하지 않는다.
- 두 저장소 문서 동기화가 작업 범위에 포함되면 참고 저장소의 IMPLEMENTATION_STATUS.md와 관련 LEARNING_GUIDE.md·LEARNING_GUIDE_TODO.md·CONTEXT.md·ARCHITECTURE.md를 확인하고 실제로 영향받은 내용만 수정한다. 개념 설명이 바뀌는 경우 android-project-concepts.md도 확인한다.
- 파일 하나만 수정하라는 요청 등 범위 제한이 있으면 이를 우선한다. 다른 문서의 불일치는 알리고 임의로 수정 범위를 넓히지 않는다.
- 기능 해설의 원본은 실제 앱의 feature 문서다. 참고 저장소에는 링크와 진행 요약을 두어 전체 해설의 중복 복사본을 만들지 않는다.
- 최종 응답에는 변경 결과와 파일, 확인한 사항, 미검증 범위, 필요한 사용자 실행 명령을 안내한다. 커밋했다면 해시·메시지와 남은 변경을, 파일만 수정했다면 미커밋 상태를 알린다.

- 테스트 안내는 문서 링크만 제공하지 않고 답변 본문에도 실행 위치와 전체 명령을 한 번에 제시한다. 새 테스트만 실행하는 명령과 전체 회귀 테스트 명령을 구분한다.
