# 💡 Implementation Notes

[README로 돌아가기](../README.md)

이 문서는 현재 코드에서 확인되는 설계 판단과 구현 경계를 정리합니다. 운영 장애 해결 실적이나 측정하지 않은 성능 개선 수치를 의미하지 않습니다.

## Repository와 Fake 분리

화면마다 샘플 데이터를 직접 관리하면 동일 데이터의 공유와 테스트 대체가 어려워집니다. 현재 앱은 Repository 인터페이스를 ViewModel 생성자로 전달하고 AppContainer에서 Fake 구현을 제공합니다.

이 구조로 UI·상태 로직을 서버 없이 개발할 수 있습니다. 실제 API 도입 시에는 인증, 오류 응답, 캐시와 데이터 갱신 정책을 추가로 설계해야 합니다.

## 달력 상태의 일괄 변경

월과 선택 날짜를 별개의 Flow로 순차 변경하면 combine이 새 월과 이전 날짜를 중간 상태로 조합할 수 있습니다. HistoryViewModel은 CalendarSelection에 두 값을 묶고, 월 이동 시 날짜를 초기화한 새 객체를 한 번에 반영합니다.

이는 상태 일관성을 위한 설계입니다. 현재 기록 자체의 연월별 필터링은 별도 미구현 사항입니다.

## 정정 요청 중복 제출 방지

CorrectionViewModel은 조회 중·제출 중·제출 완료 상태에서는 새 제출을 막습니다. 필수 사유를 검사한 뒤 코루틴 시작 전에 isSubmitting을 설정하므로 연속 클릭이 같은 요청을 중복 시작하지 않도록 구성했습니다.

실패 시 제출 상태를 해제하고 오류를 표시해 재시도를 허용합니다. 성공 후에는 isSubmitted로 추가 제출을 막습니다.

## 코루틴 취소와 오류 구분

정정 대상 조회와 제출은 일반 예외를 화면 오류로 변환하지만 CancellationException은 다시 던집니다. ViewModel이 제거되어 작업이 취소되는 상황을 일반적인 요청 실패와 구분하기 위한 처리입니다.

## Navigation 상태 정리

탭 이동은 같은 목적지를 중복 생성하지 않도록 제어하고 이전 탭 상태를 저장·복원합니다. 로그아웃 시에는 Main 그래프뿐 아니라 저장된 탭 백스택도 정리합니다.

현재 목적은 화면 흐름 정리입니다. 실제 인증 세션 삭제는 MyPageViewModel·Repository 연결 이후에 구현해야 합니다.

## Fake와 실제 동작의 차이

| 항목 | 현재 구현 | 후속 작업 |
| --- | --- | --- |
| 로그인 | 지연 후 Fake 성공·설정된 실패 반환 | 실제 인증과 토큰 관리 |
| 출석 체크 | 지연 후 PRESENT 반환 | 태그 수신, 서버 검증, 기록 갱신 |
| 정정 요청 | 지연 후 true 반환 | 접수, 중복 정책, 상태 변경 |
| 기록 | 샘플 Flow, 날짜의 일 기준 필터 | 연월별 조회와 실제 날짜 처리 |
| 통계 | 고정 샘플 값 | 출석 데이터 기반 재집계 |
| 알림 | 메모리 내 읽음 상태 | API·푸시와 영구 반영 |
| 로그아웃 | Navigation 상태 정리 | 세션 삭제와 Repository 연결 |

특히 MutableStateFlow에 기록을 보관한다는 사실과 제출 후 기록을 실제로 변경한다는 사실은 다릅니다. 현재 Fake의 출석·정정 함수는 기록 목록과 통계를 갱신하지 않습니다.

## 구현 단계

| 순서 | 작업 |
| --- | --- |
| 1 | 프로젝트 초기 생성 |
| 2 | 공통 디자인 시스템 |
| 3 | SampleData 기반 홈 초안 |
| 4 | Repository와 수동 DI |
| 5 | 스플래시 |
| 6 | 로그인과 테스트 도구 |
| 7 | 홈 |
| 8 | 출석 체크 |
| 9 | 알림 |
| 10 | 출석 기록 |
| 11 | 정정 요청과 테스트 |
| 12 | Navigation 연결과 마이페이지 기본 UI |

문서의 기준 커밋은 `12c3388`입니다. 이후 MyPageViewModel·로그아웃 Repository 호출·단위 테스트는 이 문서의 완료 범위에 포함하지 않습니다.

## 관련 코드

- [기록 상태 처리](../app/src/main/java/com/example/attendance/feature/history/presentation/HistoryViewModel.kt)
- [정정 요청 처리](../app/src/main/java/com/example/attendance/feature/history/presentation/CorrectionViewModel.kt)
- [Fake 출석 데이터](../app/src/main/java/com/example/attendance/core/data/repository/AttendanceRepository.kt)
- [탭 이동 처리](../app/src/main/java/com/example/attendance/core/navigation/BottomNavigationBar.kt)
