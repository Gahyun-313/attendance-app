# ScreenRoute.kt 줄별 해설

실제 파일: `app/src/main/java/com/example/attendance/core/navigation/ScreenRoute.kt`. package/import는 생략했다. 함수 위의 설명은 책임을, 각 줄 위의 주석은 실행 순서와 문법의 의미를 설명한다.

```kotlin
/** 문자열 경로와 인자 계약을 한곳에 모은다. Main은 로그인 이후 화면들의 상위 그래프다. */
// sealed 계층으로 가능한 목적지 종류를 제한한다.
sealed class ScreenRoute(val route: String) {
    // 앱 시작 시 잠시 표시할 목적지다.
    data object Splash : ScreenRoute("splash")
    // 로그인 입력 목적지다.
    data object Login : ScreenRoute("login")
    // 로그인 후 화면을 묶어 로그아웃 때 함께 제거할 그래프다.
    data object Main : ScreenRoute("main")
    // 메인 그래프의 시작 목적지이자 하단 홈 탭이다.
    data object Home : ScreenRoute("home")
    // 알림 하단 탭의 목적지다.
    data object Notification : ScreenRoute("notification")
    // 내 출석 기록 하단 탭의 목적지다.
    data object History : ScreenRoute("history")
    // 정적 프로필과 로그아웃 확인 UI의 목적지다.
    data object MyPage : ScreenRoute("mypage")
    // 홈에서 여는 출석 체크 상세 목적지다.
    data object Attendance : ScreenRoute("attendance")
    // 중괄호 부분을 실제 Long ID로 채우는 상세 경로다.
    data object Correction : ScreenRoute("correction/{recordId}") {
        // 3-7 ViewModel과 같은 키를 사용해 SavedStateHandle 계약을 유지한다.
        const val ARG_RECORD_ID = CorrectionViewModel.ARG_RECORD_ID
        /** 기록의 ID를 실제 이동 가능한 경로로 변환한다. */
        // 예를 들어 2L은 correction/2가 된다. NavType.LongType이 다시 Long으로 해석한다.
        fun createRoute(recordId: Long) = "correction/$recordId"
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }
// 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
}
```

# BottomNavigationBar.kt 줄별 해설

실제 파일: `app/src/main/java/com/example/attendance/core/navigation/BottomNavigationBar.kt`. package/import는 생략했다. 함수 위의 설명은 책임을, 각 줄 위의 주석은 실행 순서와 문법의 의미를 설명한다.

```kotlin
/** 하단 탭 노출 여부와 저장된 탭 상태 정리에 함께 사용하는 경로 목록이다. */
// 같은 목록을 탭 표시 조건과 로그아웃 시 저장 상태 제거에 재사용한다.
val mainTabRoutes = listOf(
    // 홈과 알림 목적지다.
    ScreenRoute.Home.route, ScreenRoute.Notification.route,
    // 기록과 마이페이지 목적지다.
    ScreenRoute.History.route, ScreenRoute.MyPage.route
// 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
)

/**
 * 같은 탭 재선택은 무시하고 홈을 기준으로 탭 백스택을 정리한다.
 * 떠나는 탭의 상태를 저장하고 다시 선택할 때 복원해 ViewModel과 스크롤 상태를 유지한다.
 */
// 홈 더보기와 하단 탭이 같은 이동 정책을 쓰는 확장 함수다.
fun NavHostController.navigateToTab(route: String) {
    // 상세 화면을 탭 이동 정책으로 잘못 호출하지 않도록 제한한다.
    require(route in mainTabRoutes)
    // 이미 선택된 목적지를 중복 생성하지 않는다.
    if (currentDestination?.route == route) return
    // 목적지 이동 옵션을 함께 구성한다.
    navigate(route) {
        // 홈 위에 쌓인 탭을 저장 후 제거한다. 홈 자체는 유지한다.
        popUpTo(ScreenRoute.Home.route) { saveState = true }
        // 목적지가 맨 위에 있으면 새 인스턴스를 쌓지 않는다.
        launchSingleTop = true
        // 이전에 저장한 동일 목적지의 상태가 있으면 복원한다.
        restoreState = true
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }
// 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
}

/** 현재 목적지를 관찰하며 홈·알림·기록·마이 탭과 선택 상태를 표시한다. */
// Compose가 화면 구성 함수로 처리하도록 표시한다.
@Composable
// 앱 그래프의 Scaffold에서 한 번만 배치하는 하단 바다.
fun BottomNavigationBar(navController: NavHostController) {
    // 백스택 변경을 Compose State로 관찰한다.
    val entry by navController.currentBackStackEntryAsState()
    // 화면에 표시할 탭 이름이다.
    val labels = listOf("홈", "알림", "기록", "마이")
    // 라벨과 같은 순서로 아이콘을 지정한다.
    val icons = listOf(Icons.Default.Home, Icons.Default.Notifications, Icons.AutoMirrored.Filled.List, Icons.Default.Person)
    // Material 3의 하단 탐색 영역이다. 시스템 하단 여백도 처리한다.
    NavigationBar {
        // 네 목적지를 순서대로 렌더링한다.
        mainTabRoutes.forEachIndexed { index, route ->
            // 선택 상태와 클릭 동작을 가진 탭 하나다.
            NavigationBarItem(
                // 현재 목적지와 일치하는 탭만 선택 표시한다.
                selected = entry?.destination?.route == route,
                // 공통 탭 이동 정책으로 이동한다.
                onClick = { navController.navigateToTab(route) },
                // 라벨이 접근성 이름을 제공하므로 장식 아이콘은 중복 설명하지 않는다.
                icon = { Icon(icons[index], contentDescription = null) },
                // 테스트와 사용자가 읽을 탭 제목이다.
                label = { Text(labels[index]) }
            // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
            )
        // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
        }
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }
// 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
}
```

# AppNavGraph.kt 줄별 해설

실제 파일: `app/src/main/java/com/example/attendance/core/navigation/AppNavGraph.kt`. package/import는 생략했다. 함수 위의 설명은 책임을, 각 줄 위의 주석은 실행 순서와 문법의 의미를 설명한다.

```kotlin
/**
 * 앱의 여덟 화면을 연결하고 탭 화면에만 하단 바를 배치한다.
 * Main 하위 그래프는 로그아웃 시 제거되며, 인증 Repository 처리는 3-8 연습 과제로 남긴다.
 */
// Compose가 화면 구성 함수로 처리하도록 표시한다.
@Composable
// UI 내용이 아니라 목적지 구성과 화면 사이 이동 정책을 담당한다.
fun AppNavGraph(
    // MainActivity에서 생성한 화면 이동 관리 객체다.
    navController: NavHostController,
    // 기본은 스플래시이며 테스트는 Login 또는 Main에서 시작할 수 있다.
    startDestination: String = ScreenRoute.Splash.route
// 함수의 매개변수 선언을 끝내고 처리 본문을 시작한다.
) {
    // 현재 경로에 따라 하단 바 표시를 바꾼다.
    val entry by navController.currentBackStackEntryAsState()
    // 탭 바를 개별 Screen에 중복 배치하지 않고 한곳에서 관리한다.
    Scaffold(
        // 상단·상세 화면 시스템 여백은 각 화면이 처리한다.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        // 상세 화면과 로그인·스플래시에는 하단 탭을 표시하지 않는다.
        bottomBar = {
            // 네 탭 경로에서만 바를 그린다.
            if (entry?.destination?.route in mainTabRoutes) BottomNavigationBar(navController)
        // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
        }
    // 탭 바가 차지한 공간을 본문에서 제외한다.
    ) { padding ->
        // 현재 백스택 목적지에 해당하는 Screen을 표시한다.
        NavHost(
            // 같은 controller로 탭과 상세 이동을 처리한다.
            navController = navController,
            // 최초 진입 목적지를 설정한다.
            startDestination = startDestination,
            // 하단 여백을 적용하고 자식 Scaffold가 중복 적용하지 않게 소비한다.
            modifier = Modifier.padding(padding).consumeWindowInsets(padding)
        // 함수의 매개변수 선언을 끝내고 처리 본문을 시작한다.
        ) {
            // 스플래시 목적지를 등록한다.
            composable(ScreenRoute.Splash.route) {
                // 화면의 타이머가 종료되면 로그인으로 이동한다.
                SplashScreen(onTimeout = {
                    // 로그인 화면을 새 목적지로 올린다.
                    navController.navigate(ScreenRoute.Login.route) {
                        // 뒤로가기로 스플래시에 돌아가지 않게 제거한다.
                        popUpTo(ScreenRoute.Splash.route) { inclusive = true }
                        // 중복 이동에 의한 같은 화면 생성을 제한한다.
                        launchSingleTop = true
                    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
                    }
                // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
                })
            // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
            }
            // 로그인 화면과 성공 콜백을 연결한다.
            composable(ScreenRoute.Login.route) {
                // 기존 LoginViewModel의 성공 상태에 따라 호출된다.
                LoginScreen(onLoginSuccess = {
                    // 중첩 Main 그래프의 시작점인 홈으로 이동한다.
                    navController.navigate(ScreenRoute.Main.route) {
                        // 이전 로그인 ViewModel도 백스택과 함께 제거한다.
                        popUpTo(ScreenRoute.Login.route) { inclusive = true }
                        // 같은 그래프를 불필요하게 반복 진입하지 않는다.
                        launchSingleTop = true
                    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
                    }
                // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
                })
            // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
            }
            // 로그인 이후 화면을 하나의 제거 가능한 그래프로 묶는다.
            navigation(startDestination = ScreenRoute.Home.route, route = ScreenRoute.Main.route) {
                // 홈 화면의 세 이동 이벤트를 연결한다.
                composable(ScreenRoute.Home.route) {
                    // 기존 수동 Factory가 해당 back stack entry 범위의 ViewModel을 생성한다.
                    HomeScreen(
                        // 출석 체크 상세 화면을 연다.
                        onCheckAttendance = { navController.navigate(ScreenRoute.Attendance.route) { launchSingleTop = true } },
                        // 알림 더보기도 하단 탭과 같은 정책으로 이동한다.
                        onMoreNotifications = { navController.navigateToTab(ScreenRoute.Notification.route) },
                        // 기록 더보기를 기록 탭으로 연결한다.
                        onMoreRecords = { navController.navigateToTab(ScreenRoute.History.route) }
                    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
                    )
                // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
                }
                // 알림 화면은 기존 controller 기반 뒤로가기 동작을 재사용한다.
                composable(ScreenRoute.Notification.route) {
                    // 같은 AppContainer의 알림 Repository를 홈과 공유한다.
                    NotificationScreen(navController = navController)
                // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
                }
                // 기록 탭에서 선택한 기록을 정정 화면으로 전달한다.
                composable(ScreenRoute.History.route) {
                    // 행의 기록 ID가 콜백을 따라 올라온다.
                    AttendanceHistoryScreen(onRequestCorrection = { recordId ->
                        // Long ID를 포함한 실제 경로로 이동한다.
                        navController.navigate(ScreenRoute.Correction.createRoute(recordId)) { launchSingleTop = true }
                    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
                    })
                // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
                }
                // ViewModel 없이 정적 UI와 로그아웃 이동만 제공한다.
                composable(ScreenRoute.MyPage.route) {
                    // Repository.logout 호출은 연습 과제로 남겨 현재는 화면 이동만 수행한다.
                    MyPageScreen(onLogout = {
                        // 저장된 탭 상태까지 비워 다음 로그인 때 이전 ViewModel을 복원하지 않는다.
                        mainTabRoutes.forEach { navController.clearBackStack(it) }
                        // 새 로그인 화면으로 이동한다.
                        navController.navigate(ScreenRoute.Login.route) {
                            // 현재 Main 그래프와 하위 화면을 모두 제거한다.
                            popUpTo(ScreenRoute.Main.route) { inclusive = true }
                            // 로그인 목적지를 중복 생성하지 않는다.
                            launchSingleTop = true
                        // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
                        }
                    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
                    })
                // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
                }
                // 상세 화면에서는 상위 조건에 의해 탭 바가 숨겨진다.
                composable(ScreenRoute.Attendance.route) {
                    // 기존 출석 모달과 뒤로가기 동작을 그대로 연결한다.
                    AttendanceScreen(navController = navController)
                // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
                }
                // 인자를 받는 정정 목적지를 등록한다.
                composable(
                    // 실제 ID를 받는 경로 패턴이다.
                    route = ScreenRoute.Correction.route,
                    // 문자열 경로 값을 Long으로 해석해 SavedStateHandle에 제공한다.
                    arguments = listOf(navArgument(ScreenRoute.Correction.ARG_RECORD_ID) { type = NavType.LongType })
                // 함수의 매개변수 선언을 끝내고 처리 본문을 시작한다.
                ) {
                    // 기본 Factory가 이 목적지 소유자의 SavedStateHandle을 생성한다.
                    CorrectionRequestScreen(
                        // 취소 또는 뒤로가기는 기록 화면으로 복귀한다.
                        onBack = { navController.popBackStack() },
                        // 접수 성공 후 정정 목적지를 제거해 성공 상태가 재실행되지 않게 한다.
                        onSubmitted = { navController.popBackStack() }
                    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
                    )
                // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
                }
            // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
            }
        // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
        }
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }
// 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
}
```

# MainActivity.kt 줄별 해설

실제 파일: `app/src/main/java/com/example/attendance/MainActivity.kt`. package/import는 생략했다. 함수 위의 설명은 책임을, 각 줄 위의 주석은 실행 순서와 문법의 의미를 설명한다.

```kotlin
/** 하나의 Activity가 Navigation 그래프를 호스팅한다. 홈 초안은 Preview용 파일로 유지한다. */
// Android 런처가 시작하는 실제 Activity다.
class MainActivity : ComponentActivity() {
    /** Activity 생성 시 테마와 화면 이동 컨트롤러를 구성한다. */
    // 시스템이 전달한 복원 상태를 받는 생명주기 진입점이다.
    override fun onCreate(savedInstanceState: Bundle?) {
        // 기본 Activity의 복원·초기화 처리를 먼저 수행한다.
        super.onCreate(savedInstanceState)
        // 시스템 바 아래까지 그리되 실제 콘텐츠 여백은 각 화면과 Scaffold가 처리한다.
        enableEdgeToEdge()
        // Activity의 콘텐츠를 Compose 트리로 구성한다.
        setContent {
            // 앱 테마를 전체 화면에 적용한다.
            AttendanceTheme {
                // 앱 전체 배경과 크기를 지정한다.
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // 재구성·저장 상태 복원에 참여하는 controller를 생성한다.
                    val navController = rememberNavController()
                    // 기본 목적지인 스플래시부터 전체 흐름을 시작한다.
                    AppNavGraph(navController = navController)
                // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
                }
            // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
            }
        // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
        }
    // 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
    }
// 현재 호출의 인자 목록 또는 코드 블록을 닫고 바깥 범위로 돌아간다.
}
```
