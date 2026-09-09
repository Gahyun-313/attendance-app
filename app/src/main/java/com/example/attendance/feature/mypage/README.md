# 마이페이지 기본 UI와 남겨둔 연습 과제

3-8의 기본 UI를 3-9 Navigation과 함께 먼저 작성했다. 정적 프로필, 계정 메뉴, 로그아웃 확인창과 화면 이동을 제공한다. **MyPageViewModel과 MyPageViewModelTest는 작성하지 않았다.**

## 파일과 상태 흐름

MyPageScreen은 rememberSaveable로 확인창 표시 여부와 설정 안내 제목을 소유한다. MyPageContent는 이름·학기 표시값과 메뉴 콜백을 받아 화면을 그린다. 프로필 기본값은 서버에서 읽은 사용자가 아닌 정적 표시값이다.

```text
MyPageContent 로그아웃 클릭
 → MyPageScreen.showLogoutDialog = true
 → ConfirmDialog
     ├─ 취소/뒤로 → 표시 여부만 false
     └─ 확인 → 표시 여부 false → onLogout
                                   → AppNavGraph: 저장된 탭 제거, Main 제거, Login 이동
```

프로필 관리·알림 설정은 현재 목적지 화면이 없어 준비 중 안내창을 표시한다. 알림 목록 탭과 알림 설정은 서로 다른 기능이다.

## 문법과 설계

remember는 재구성 사이 값을 기억하고 rememberSaveable은 저장 가능한 Boolean·String 값을 화면 재생성에도 복원할 수 있게 한다. 다이얼로그 표시 여부는 서버나 도메인 데이터가 아닌 UI 상태라 화면이 소유한다. nullable notice는 null이면 안내 없음, 문자열이면 해당 안내를 표시하는 모델이다.

Content는 이벤트만 보내고 Screen은 확인창을 결정하며 AppNavGraph는 화면 이동을 담당한다. 이 분리 덕분에 MyPageScreenTest는 Repository·NavController 없이도 확인/취소를 검증할 수 있다. Stateless Content라 해도 내부 스크롤 위치 같은 표시 상태까지 없어야 한다는 뜻은 아니다.

## 나중에 직접 할 연습 과제

- [ ] MyPageViewModel을 생성자 주입 방식으로 작성한다.
- [ ] AuthRepository.logout() 호출과 완료·실패 상태 처리를 ViewModel로 옮긴다.
- [ ] 로그아웃 완료 후 기존 onLogout 이동을 요청하도록 Screen을 연결한다.
- [ ] MainDispatcherRule 등을 활용해 MyPageViewModelTest를 작성한다.
- [ ] 확인창 표시 여부는 화면의 UI 상태로 유지한다.

현재 AuthRepository에는 프로필 조회 계약이 없으므로 정적 이름을 실제 로그인 사용자 정보라고 설명하지 않는다. 정답 예시는 가이드 STEP 6-4에 있지만 이번 작업에서는 구현하지 않는다.

## 해설과 검증

- [화면 코드 줄별 해설](MYPAGE_UI_NOTES.md)
- [UI 테스트 줄별 해설](MYPAGE_TEST_NOTES.md)
- [직접 검증 안내](VALIDATION.md)

## 이해를 위해 스스로 설명해 볼 질문

1. 확인창 상태와 로그아웃 요청 상태를 서로 다른 곳에 두는 이유는 무엇인가?
2. 확인 버튼을 눌렀을 때 현재 실행하는 동작과 연습 과제에서 추가할 동작은 무엇인가?
3. 프로필 표시값을 파라미터로 받으면 Preview와 테스트에서 무엇이 쉬워지는가?
