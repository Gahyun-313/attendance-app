# 정정 요청 테스트 줄별 해설

## 책임과 실행 흐름

SavedStateHandle과 테스트 Repository를 생성자에 주입한다. StandardTestDispatcher로 launch 시작 전 상태와 가상 시간 이후 결과를 각각 검증한다.

실제 파일: `app/src/test/java/com/example/attendance/feature/history/presentation/CorrectionViewModelTest.kt`. package/import는 생략하고 나머지 코드의 실행 순서와 동작을 유지했다. 실제 소스의 주석은 주요 책임 중심이며, 아래는 학습을 위한 줄별 해설이다.

```kotlin
// 가상 시간 테스트에 사용하는 실험적 코루틴 API 사용을 명시한다.
@OptIn(ExperimentalCoroutinesApi::class)
// Android 화면 없이 ViewModel의 입력·상태·Repository 호출 계약을 검증한다.
class CorrectionViewModelTest {
    // Kotlin 프로퍼티의 getter에 JUnit Rule 어노테이션을 적용한다.
    @get:Rule
    // launch가 바로 실행되지 않게 해 코루틴 시작 전 중복 제출 방지도 검증한다.
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    /** 전달한 Long ID가 실제 조회 호출과 화면 기록에 반영되는지 확인한다. */
    // JUnit이 이 메서드를 독립된 테스트로 실행하도록 표시한다.
    @Test
    // 테스트 스케줄러가 코루틴 가상 시간을 관리한다.
    fun SavedStateHandle의_recordId로_기록을_조회한다() = runTest {
        // 호출 기록을 수집하는 테스트 전용 Repository다.
        val repository = RecordingRepository()
        // SampleData 기록 ID를 SavedStateHandle에 넣어 생성한다.
        val viewModel = createViewModel(repository)
        // 아직 조회 코루틴을 실행하지 않아 초기 로딩 상태다.
        assertThat(viewModel.uiState.value.isLoading).isTrue()
        // 조회가 끝날 때까지 테스트 스케줄러를 진행한다.
        advanceUntilIdle()
        // SavedStateHandle의 값이 조회 인자로 정확히 전달됐는지 확인한다.
        assertThat(repository.queriedIds).containsExactly(repository.record.id)
        // 조회 결과가 UI 상태에 들어갔는지 확인한다.
        assertThat(viewModel.uiState.value.record).isEqualTo(repository.record)
        // 조회 후 로딩이 해제되어야 한다.
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }

    /** 인자가 누락되면 조회와 제출을 모두 차단하고 오류를 표시한다. */
    // JUnit이 이 메서드를 독립된 테스트로 실행하도록 표시한다.
    @Test
    // 잘못된 화면 진입에 대한 방어를 검증한다.
    fun 인자가_없으면_조회와_제출을_하지_않는다() = runTest {
        // 조회·제출 호출 여부를 관찰한다.
        val repository = RecordingRepository()
        // recordId가 없는 저장 상태를 주입한다.
        val viewModel = CorrectionViewModel(SavedStateHandle(), repository)
        // 입력이 유효해도 대상 부재 검증이 우선되어야 한다.
        viewModel.onReasonChange("NFC 오류")
        // 대상 없는 제출을 시도한다.
        viewModel.submit()
        // 예약된 작업까지 처리해 숨은 호출이 없는지 확인한다.
        advanceUntilIdle()
        // 누락된 ID로 조회하지 않는다.
        assertThat(repository.queriedIds).isEmpty()
        // 제출도 하지 않는다.
        assertThat(repository.requests).isEmpty()
        // 대상이 없는 이유를 표시한다.
        assertThat(viewModel.uiState.value.errorMessage).isNotNull()
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }

    /** 양수 ID라도 Repository에 기록이 없으면 제출을 허용하지 않는다. */
    // JUnit이 이 메서드를 독립된 테스트로 실행하도록 표시한다.
    @Test
    // 형식이 유효한 ID와 실제 기록 존재 여부를 구분한다.
    fun 없는_기록이면_오류를_표시하고_제출하지_않는다() = runTest {
        // 한 개의 테스트 기록만 제공한다.
        val repository = RecordingRepository()
        // 존재하지 않는 ID를 전달한다.
        val viewModel = CorrectionViewModel(SavedStateHandle(mapOf(CorrectionViewModel.ARG_RECORD_ID to Long.MAX_VALUE)), repository)
        // null 조회 결과를 상태에 반영한다.
        advanceUntilIdle()
        // 필수 입력은 채운다.
        viewModel.onReasonChange("NFC 오류")
        // 기록 없는 상태에서 제출을 시도한다.
        viewModel.submit()
        // 비동기 작업을 모두 처리한다.
        advanceUntilIdle()
        // 없는 기록을 임의의 샘플로 대체하지 않는다.
        assertThat(viewModel.uiState.value.record).isNull()
        // 조회는 끝난 상태다.
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        // 대상 부재를 설명한다.
        assertThat(viewModel.uiState.value.errorMessage).isNotNull()
        // 잘못된 대상에 접수하지 않는다.
        assertThat(repository.requests).isEmpty()
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }

    /** 공백 사유를 거절한 뒤 새 입력으로 오류가 해제되고 정확한 인자가 제출되는지 확인한다. */
    // JUnit이 이 메서드를 독립된 테스트로 실행하도록 표시한다.
    @Test
    // 입력 검증과 오류 해제, 성공 상태를 한 흐름으로 검증한다.
    fun 공백_사유는_차단하고_수정하면_입력값으로_제출한다() = runTest {
        // 성공하는 제출 응답을 사용한다.
        val repository = RecordingRepository()
        // 유효한 대상을 준비한다.
        val viewModel = createViewModel(repository)
        // 초기 기록 조회를 끝낸다.
        advanceUntilIdle()
        // 단순 빈 문자열보다 넓은 isBlank 검증을 자극한다.
        viewModel.onReasonChange("   ")
        // 입력 검증에서 멈춰야 한다.
        viewModel.submit()
        // 필수 입력 오류를 확인한다.
        assertThat(viewModel.uiState.value.isReasonError).isTrue()
        // 잘못된 입력으로 Repository를 호출하지 않는다.
        assertThat(repository.requests).isEmpty()
        // 유효한 사유 입력으로 바꾼다.
        viewModel.onReasonChange("NFC 오류")
        // 선택 입력도 요청에 포함되는지 확인한다.
        viewModel.onDetailChange("태그 인식이 되지 않았습니다.")
        // 이전 오류가 해제되어야 한다.
        assertThat(viewModel.uiState.value.isReasonError).isFalse()
        // 유효한 요청을 시작한다.
        viewModel.submit()
        // 지연된 접수 응답까지 처리한다.
        advanceUntilIdle()
        // 대상·사유·상세가 정확히 전달되었는지 검증한다.
        assertThat(repository.requests).containsExactly(Triple(repository.record.id, "NFC 오류", "태그 인식이 되지 않았습니다."))
        // 성공 상태가 화면에 전달된다.
        assertThat(viewModel.uiState.value.isSubmitted).isTrue()
        // 진행 표시가 끝난다.
        assertThat(viewModel.uiState.value.isSubmitting).isFalse()
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }

    /** launch 실행 전 연속 클릭과 성공 후 재제출을 차단하고 클릭 당시 입력값을 유지한다. */
    // JUnit이 이 메서드를 독립된 테스트로 실행하도록 표시한다.
    @Test
    // 중복 요청과 요청 도중 입력 변경의 경계를 검증한다.
    fun 연속_제출과_성공_후_재제출을_막고_클릭_시점_입력을_사용한다() = runTest {
        // 호출 횟수와 인자를 수집한다.
        val repository = RecordingRepository()
        // 기본 기록 ID로 생성한다.
        val viewModel = createViewModel(repository)
        // 대상 조회를 끝낸다.
        advanceUntilIdle()
        // 첫 클릭의 입력값이다.
        viewModel.onReasonChange("첫 사유")
        // 코루틴이 실행되기 전에 isSubmitting을 잠가야 한다.
        viewModel.submit()
        // StandardTestDispatcher를 진행하지 않아도 잠겨 있어야 한다.
        assertThat(viewModel.uiState.value.isSubmitting).isTrue()
        // 같은 프레임의 두 번째 클릭을 재현한다.
        viewModel.submit()
        // 요청 시작 뒤 입력을 바꾸어도 전송 인자는 고정된다.
        viewModel.onReasonChange("바뀐 사유")
        // 실제 요청과 지연 응답을 처리한다.
        advanceUntilIdle()
        // 성공 상태에서 다시 요청하는 것도 차단한다.
        viewModel.submit()
        // 추가 작업이 없는지 확인한다.
        advanceUntilIdle()
        // 정확히 한 번만 클릭 당시 값으로 제출된다.
        assertThat(repository.requests).containsExactly(Triple(repository.record.id, "첫 사유", ""))
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }

    /** 서버가 false를 반환하면 오류를 남기고 다음 제출에서 오류를 지운 뒤 재시도한다. */
    // JUnit이 이 메서드를 독립된 테스트로 실행하도록 표시한다.
    @Test
    // Boolean 실패와 성공 재시도의 상태 전이를 검증한다.
    fun 제출_실패_후_다시_시도할_수_있다() = runTest {
        // 첫 요청을 실패하도록 설정한다.
        val repository = RecordingRepository().apply { succeeds = false }
        // 대상을 주입한다.
        val viewModel = createViewModel(repository)
        // 조회를 끝낸다.
        advanceUntilIdle()
        // 필수 사유를 채운다.
        viewModel.onReasonChange("NFC 오류")
        // 실패 응답을 받을 요청이다.
        viewModel.submit()
        // 응답을 기다린다.
        advanceUntilIdle()
        // 실패를 성공으로 표시하지 않는다.
        assertThat(viewModel.uiState.value.isSubmitted).isFalse()
        // 다시 제출할 수 있도록 잠금이 해제된다.
        assertThat(viewModel.uiState.value.isSubmitting).isFalse()
        // 실패 이유를 사용자에게 안내한다.
        assertThat(viewModel.uiState.value.errorMessage).isNotNull()
        // 두 번째 요청은 성공하게 설정한다.
        repository.succeeds = true
        // 재시도를 시작한다.
        viewModel.submit()
        // 이전 실패 문구를 즉시 지운다.
        assertThat(viewModel.uiState.value.errorMessage).isNull()
        // 재시도 응답까지 진행한다.
        advanceUntilIdle()
        // 재시도는 성공한다.
        assertThat(viewModel.uiState.value.isSubmitted).isTrue()
        // 서로 다른 두 번의 시도를 확인한다.
        assertThat(repository.requests).hasSize(2)
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }

    /** 조회 예외를 화면 오류로 바꾸고 로딩을 종료하는지 확인한다. */
    // JUnit이 이 메서드를 독립된 테스트로 실행하도록 표시한다.
    @Test
    // 네트워크 등 일반 조회 오류를 가정한다.
    fun 조회_예외는_오류로_표시한다() = runTest {
        // 테스트용 예외를 주입한다.
        val repository = RecordingRepository().apply { lookupFailure = IllegalStateException("조회 실패") }
        // 초기 조회에서 예외가 발생한다.
        val viewModel = createViewModel(repository)
        // catch 처리까지 실행한다.
        advanceUntilIdle()
        // 무한 로딩에 남지 않는다.
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        // 오류가 표시되어야 한다.
        assertThat(viewModel.uiState.value.errorMessage).isNotNull()
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }

    /** 제출 예외를 성공으로 처리하지 않고 입력을 보존해 재시도를 허용한다. */
    // JUnit이 이 메서드를 독립된 테스트로 실행하도록 표시한다.
    @Test
    // Boolean 반환 이전에 요청이 예외로 끝나는 경로를 검증한다.
    fun 제출_예외는_오류로_표시하고_입력을_보존한다() = runTest {
        // 요청 예외를 설정한다.
        val repository = RecordingRepository().apply { submitFailure = IllegalStateException("제출 실패") }
        // 유효한 기록을 사용한다.
        val viewModel = createViewModel(repository)
        // 조회를 끝낸다.
        advanceUntilIdle()
        // 재시도 시 보존되어야 하는 입력이다.
        viewModel.onReasonChange("NFC 오류")
        // 예외를 발생시키는 요청이다.
        viewModel.submit()
        // 오류 처리를 끝낸다.
        advanceUntilIdle()
        // 제출 잠금을 풀어야 한다.
        assertThat(viewModel.uiState.value.isSubmitting).isFalse()
        // 완료 콜백이 발생하면 안 된다.
        assertThat(viewModel.uiState.value.isSubmitted).isFalse()
        // 재입력을 강요하지 않도록 사유를 보존한다.
        assertThat(viewModel.uiState.value.reason).isEqualTo("NFC 오류")
        // 실패 문구가 있어야 한다.
        assertThat(viewModel.uiState.value.errorMessage).isNotNull()
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }

    /** 코루틴 취소를 일반 실패나 성공으로 바꾸지 않는지 확인한다. */
    // JUnit이 이 메서드를 독립된 테스트로 실행하도록 표시한다.
    @Test
    // CancellationException을 별도로 다루는 계약을 검증한다.
    fun 제출_취소는_실패_메시지로_변환하지_않는다() = runTest {
        // 요청 코루틴의 취소를 재현한다.
        val repository = RecordingRepository().apply { submitFailure = CancellationException("취소") }
        // 유효한 기록으로 시작한다.
        val viewModel = createViewModel(repository)
        // 조회를 마친다.
        advanceUntilIdle()
        // 입력 검증을 통과시킨다.
        viewModel.onReasonChange("NFC 오류")
        // 취소되는 요청을 실행한다.
        viewModel.submit()
        // 취소 처리를 마친다.
        advanceUntilIdle()
        // 진행 표시를 정리한다.
        assertThat(viewModel.uiState.value.isSubmitting).isFalse()
        // 취소는 성공이 아니다.
        assertThat(viewModel.uiState.value.isSubmitted).isFalse()
        // 취소를 일반 서버 실패처럼 알리지 않는다.
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }

    /** 모든 정상 진입 테스트에서 같은 인자 계약으로 ViewModel을 만든다. */
    // 중복 생성 코드를 한곳에 둔다.
    private fun createViewModel(repository: RecordingRepository): CorrectionViewModel =
        // 수동 생성자 주입이므로 Android Application 없이 실행한다.
        CorrectionViewModel(
            // Long ID를 실제 기능의 키에 넣는다.
            SavedStateHandle(mapOf(CorrectionViewModel.ARG_RECORD_ID to repository.record.id)),
            // 호출을 관찰할 테스트 대역을 전달한다.
            repository
        // 앞에서 지정한 생성자 또는 함수 호출의 인자 목록을 닫는다.
        )

    /** 정정 관련 메서드만 제어하고 나머지 Repository 계약은 기존 Fake에 위임한다. */
    // Kotlin의 by 위임으로 무관한 메서드를 반복 구현하지 않는다.
    private class RecordingRepository : AttendanceRepository by FakeAttendanceRepository() {
        // 실제 샘플 모델 한 건을 조회 결과로 사용한다.
        val record = SampleData.historyRecords[1]
        // 조회 인자와 횟수를 기록한다.
        val queriedIds = mutableListOf<Long>()
        // 제출 ID·사유·상세의 스냅샷을 기록한다.
        val requests = mutableListOf<Triple<Long, String, String>>()
        // false 응답과 성공 재시도를 제어한다.
        var succeeds = true
        // null이면 정상 조회, 예외가 있으면 실패시킨다.
        var lookupFailure: Exception? = null
        // 일반 예외 또는 취소를 주입한다.
        var submitFailure: Exception? = null

        /** 조회 호출을 기록한 뒤 예외 또는 ID에 해당하는 기록을 반환한다. */
        // 실제 Repository의 suspend 계약을 유지한다.
        override suspend fun getRecord(recordId: Long): AttendanceRecord? {
            // 받은 ID를 기록한다.
            queriedIds += recordId
            // 설정한 예외가 있으면 조회 실패를 재현한다.
            lookupFailure?.let { throw it }
            // 요청 ID와 일치할 때만 기록을 제공한다.
            return record.takeIf { it.id == recordId }
        // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
        }

        /** 요청 인자를 기록하고 가상 시간 지연 후 설정한 응답을 돌려준다. */
        // 정정 요청 동작만 기존 Fake 대신 구현한다.
        override suspend fun requestCorrection(recordId: Long, reason: String, detail: String): Boolean {
            // 호출 당시 인자를 변경되지 않는 값 묶음으로 보관한다.
            requests += Triple(recordId, reason, detail)
            // 실제 대기 없이 테스트 스케줄러가 진행할 수 있는 suspend 지연이다.
            delay(500)
            // 설정한 실패 또는 취소를 재현한다.
            submitFailure?.let { throw it }
            // Boolean 응답으로 성공과 실패를 선택한다.
            return succeeds
        // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
        }
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }
// 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
}
```

---

## Compose UI 테스트 줄별 해설

## 책임과 실행 흐름

Content에 상태·콜백을 직접 넣고 사용자 입력과 스크롤·클릭을 검증한다.

실제 파일: `app/src/androidTest/java/com/example/attendance/feature/history/presentation/CorrectionRequestScreenTest.kt`. package/import는 생략하고 나머지 코드의 실행 순서와 동작을 유지했다. 실제 소스의 주석은 주요 책임 중심이며, 아래는 학습을 위한 줄별 해설이다.

```kotlin
// Content의 표시와 이벤트 연결을 기기에서 검증한다. 실제 Navigation 테스트는 아니다.
class CorrectionRequestScreenTest {
    // Kotlin 프로퍼티의 getter에 JUnit Rule 어노테이션을 적용한다.
    @get:Rule
    // Compose 콘텐츠를 설치하고 UI 스레드와 테스트를 동기화한다.
    val composeTestRule = createComposeRule()
    // 조회가 끝난 폼 상태를 각 테스트의 기준으로 사용한다.
    private val loadedState = CorrectionUiState(record = SampleData.historyRecords[1], isLoading = false)

    /** 작은 기기에서도 제출 영역으로 스크롤한 뒤 콜백을 검증한다. */
    // JUnit이 이 메서드를 독립된 테스트로 실행하도록 표시한다.
    @Test
    // 가이드의 제출 클릭 요구사항이다.
    fun 제출_버튼이_콜백을_호출한다() {
        // 실제 호출 횟수를 센다.
        var submitCount = 0
        // 클릭 이벤트를 관찰 가능한 람다로 연결한다.
        showContent(onSubmit = { submitCount++ })
        // 스크롤 아래에 있는 버튼을 노출하고 누른다.
        composeTestRule.onNodeWithText("제출").performScrollTo().performClick()
        // UI 동작이 끝난 뒤 정확히 한 번 호출됐는지 확인한다.
        composeTestRule.runOnIdle { assertThat(submitCount).isEqualTo(1) }
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }

    /** 제출 진행 중의 문구와 비활성 상태를 함께 검증한다. */
    // JUnit이 이 메서드를 독립된 테스트로 실행하도록 표시한다.
    @Test
    // 문구 변경만이 아니라 클릭 가능 여부도 확인한다.
    fun 제출_중에는_버튼이_비활성화된다() {
        // 진행 중인 상태를 직접 주입한다.
        showContent(state = { loadedState.copy(isSubmitting = true) })
        // 작은 화면에서도 노출·문구·비활성을 검증한다.
        composeTestRule.onNodeWithText("제출 중...").performScrollTo().assertIsDisplayed().assertIsNotEnabled()
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }

    /** 입력 콜백의 값을 상태로 돌려주어 controlled TextField의 실제 사용 방식을 재현한다. */
    // JUnit이 이 메서드를 독립된 테스트로 실행하도록 표시한다.
    @Test
    // 이전 로그인 테스트처럼 고정 빈 상태를 유지하지 않는다.
    fun 사유와_상세_입력이_각_콜백으로_전달된다() {
        // 테스트가 상태 소유자가 되어 입력 뒤 재구성을 발생시킨다.
        var state by mutableStateOf(loadedState)
        // 실제 Content에 상태와 갱신 함수를 주입한다.
        showContent(
            // Compose에서 읽힐 때 변경을 관찰할 수 있는 상태 제공 함수다.
            state = { state },
            // 사유 입력을 다시 UI에 돌려준다.
            onReasonChange = { state = state.copy(reason = it) },
            // 상세 입력을 별도 필드에 반영한다.
            onDetailChange = { state = state.copy(detail = it) }
        // 앞에서 지정한 생성자 또는 함수 호출의 인자 목록을 닫는다.
        )
        // 첫 번째 입력 필드인 사유에 입력한다.
        composeTestRule.onAllNodes(hasSetTextAction())[0].performScrollTo().performTextInput("NFC 오류")
        // 두 번째 입력 필드인 상세에 입력한다.
        composeTestRule.onAllNodes(hasSetTextAction())[1].performScrollTo().performTextInput("태그 인식 실패")
        // 상세 입력 뒤에도 사유가 유지되어야 한다.
        composeTestRule.onAllNodes(hasSetTextAction())[0].assertTextContains("NFC 오류")
        // UI 입력 처리 후 상태를 확인한다.
        composeTestRule.runOnIdle {
            // 사유 콜백으로 정확한 값이 전달됐다.
            assertThat(state.reason).isEqualTo("NFC 오류")
            // 상세 콜백으로 정확한 값이 전달됐다.
            assertThat(state.detail).isEqualTo("태그 인식 실패")
        // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
        }
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }

    /** 입력 검증 오류와 서버 실패 안내가 화면에 나타나는지 확인한다. */
    // JUnit이 이 메서드를 독립된 테스트로 실행하도록 표시한다.
    @Test
    // Content는 전달된 오류 상태를 사용자에게 보여야 한다.
    fun 사유_오류와_제출_오류가_표시된다() {
        // 두 오류 표시 경로를 직접 주입한다.
        showContent(state = { loadedState.copy(isReasonError = true, errorMessage = "정정 요청에 실패했습니다. 다시 시도해주세요.") })
        // 스낵바 없이도 지속적인 필수 입력 안내가 보인다.
        composeTestRule.onNodeWithText("정정 사유를 입력해주세요.").performScrollTo().assertIsDisplayed()
        // 요청 실패 문구가 보인다.
        composeTestRule.onNodeWithText("정정 요청에 실패했습니다. 다시 시도해주세요.").performScrollTo().assertIsDisplayed()
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }

    /** 대상이 없는 상태에서는 제출을 비활성화한다. */
    // JUnit이 이 메서드를 독립된 테스트로 실행하도록 표시한다.
    @Test
    // null 기록으로 UI에서 제출이 허용되지 않아야 한다.
    fun 대상이_없으면_제출할_수_없다() {
        // 조회가 끝났지만 기록이 없는 상태다.
        showContent(state = { CorrectionUiState(isLoading = false, errorMessage = "정정 대상 기록을 찾을 수 없습니다.") })
        // 대상 부재 이유를 표시한다.
        composeTestRule.onNodeWithText("정정 대상 기록을 찾을 수 없습니다.").assertIsDisplayed()
        // 제출을 막는다.
        composeTestRule.onNodeWithText("제출").performScrollTo().assertIsNotEnabled()
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }

    /** 취소는 외부에서 정한 뒤로가기 동작을 실행한다. */
    // JUnit이 이 메서드를 독립된 테스트로 실행하도록 표시한다.
    @Test
    // 화면 내부에 Navigation이 없어도 이벤트 계약을 검증한다.
    fun 취소_버튼이_뒤로가기_콜백을_호출한다() {
        // 뒤로가기 호출 횟수를 기록한다.
        var backCount = 0
        // 취소 콜백을 주입한다.
        showContent(onBack = { backCount++ })
        // 하단 취소 버튼을 누른다.
        composeTestRule.onNodeWithText("취소").performScrollTo().performClick()
        // 외부로 한 번 전달되는지 확인한다.
        composeTestRule.runOnIdle { assertThat(backCount).isEqualTo(1) }
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }

    /** 앱 테마와 스낵바 상태를 준비하고 테스트별 상태·콜백을 Content에 전달한다. */
    // 각 테스트의 반복 렌더링 준비를 모은 도우미다.
    private fun showContent(
        // 상태를 함수로 받아 입력 테스트의 변경 가능한 State도 읽을 수 있게 한다.
        state: () -> CorrectionUiState = { loadedState },
        // 필요 없는 이벤트는 기본 빈 콜백을 사용한다.
        onBack: () -> Unit = {},
        // 사유 입력을 검증할 테스트가 갱신 함수를 제공한다.
        onReasonChange: (String) -> Unit = {},
        // 상세 입력 콜백이다.
        onDetailChange: (String) -> Unit = {},
        // 제출 콜백이다.
        onSubmit: () -> Unit = {}
    // 매개변수 선언을 마치고 이 함수의 본문을 시작한다.
    ) {
        // 테스트 Activity에 Compose 화면을 설치한다.
        composeTestRule.setContent {
            // 앱과 같은 테마를 사용한다.
            AttendanceTheme {
                // ViewModel·Navigation 없이 실제 폼을 렌더링한다.
                CorrectionContent(
                    // Composition 안에서 현재 State 값을 읽는다.
                    uiState = state(),
                    // 재구성 시 동일한 스낵바 상태를 유지한다.
                    snackbarHostState = remember { SnackbarHostState() },
                    // 뒤로가기와 사유 콜백을 연결한다.
                    onBack = onBack, onReasonChange = onReasonChange,
                    // 상세와 제출 콜백을 연결한다.
                    onDetailChange = onDetailChange, onSubmit = onSubmit
                // 앞에서 지정한 생성자 또는 함수 호출의 인자 목록을 닫는다.
                )
            // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
            }
        // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
        }
    // 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
    }
// 현재 함수·클래스 또는 람다의 실행 범위를 닫고 바깥 범위로 돌아간다.
}
```
