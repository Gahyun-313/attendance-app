# STEP 3-2 검증 기록

검증일: 2026-09-08. 대상: `attendance-app`, `feat/screen-login`.

## 실행 결과

| 검증 | 결과 |
| --- | --- |
| `testDebugUnitTest` | 성공. 로그인 Fake 테스트 5개, MockK 테스트 3개, 기존 예제 1개. 실패·오류·건너뜀 0개. |
| `assembleDebug` | 성공. 앱 APK 빌드 완료. |
| `assembleDebugAndroidTest` | 성공. 로그인 UI 테스트를 포함한 계측 테스트 APK 빌드 완료. |
| `connectedDebugAndroidTest` | 미실행. 에뮬레이터 테스트 실행 권한 요청이 거절되어 실행하지 않음. |
| 문서와 실제 코드 대조 | 7개 파일, import/package와 주석을 제외한 코드 340줄이 설명 문서 코드와 동일함을 확인. |
| `git diff --check` | 통과. |

Gradle은 기존 캐시 경로 접근 권한을 허용받아 실행했다. `BUILD SUCCESSFUL`, 73 tasks 중 56개 실행·17개 최신 상태로 보고됐다. 네이티브 라이브러리 `libandroidx.graphics.path.so`의 심볼을 제거하지 못해 그대로 패키징한다는 경고가 있었지만 빌드 실패는 없었다.

## 테스트 범위

- Fake 상태 테스트: 입력 반영, 성공 상태, 실패 상태, 로딩 전환, 실패 후 재시도.
- MockK 테스트: 정확한 요청 인자·횟수, 코루틴 시작 전 연속 클릭과 클릭 시점 입력 유지, null 실패 메시지의 기본 문구.
- UI 테스트 코드: 입력된 학번 표시, 로딩 문구·버튼 비활성화, 오류 표시·클릭 콜백, 두 입력 콜백 전달. 컴파일만 확인했으며 실제 기기에서 통과했다고 주장하지 않는다.

Navigation 연결, 실제 인증 서버, 세션 저장, 앱 실행 후 로그인 진입, 화면 회전 및 성공 후 재진입은 이 검증에 포함되지 않는다. 현재 MainActivity는 홈 프로토타입을 표시한다.

추후 연결된 기기에서 `./gradlew.bat connectedDebugAndroidTest`를 실행하여 UI 테스트 결과를 확인한다.
