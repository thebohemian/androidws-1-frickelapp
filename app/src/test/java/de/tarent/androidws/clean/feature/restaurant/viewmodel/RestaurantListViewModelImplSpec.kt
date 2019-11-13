package de.tarent.androidws.clean.feature.restaurant.viewmodel

import android.util.Log
import de.tarent.androidws.clean.feature.restaurant.mapper.RestaurantItemMapper
import de.tarent.androidws.clean.feature.restaurant.usecase.GetRestaurantUseCase
import de.tarent.androidws.clean.feature.restaurant.viewmodel.RestaurantListViewModel.State
import de.tarent.androidws.clean.repository.common.extension.flagIoError
import de.tarent.androidws.clean.test.InstantExecutorTestListener
import io.kotlintest.IsolationMode
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.extensions.TestListener
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import java.io.IOException
import java.lang.IllegalArgumentException

internal class RestaurantListViewModelImplSpec : WordSpec() {

    private lateinit var vm: RestaurantListViewModelImpl

    private val getRestaurantUseCase: GetRestaurantUseCase = mockk()

    private val restaurantItemMapper: RestaurantItemMapper = mockk()

    override fun isolationMode(): IsolationMode? = IsolationMode.InstancePerTest

    // Provides LiveData updates on JVM main thread
    override fun listeners(): List<TestListener> = listOf(InstantExecutorTestListener())

    private val dispatcher = TestCoroutineDispatcher()

    override fun beforeTest(testCase: TestCase) {
        // Provides coroutines on JVM main
        Dispatchers.setMain(dispatcher)

        // mocks platform class and silences invocations
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        vm = RestaurantListViewModelImpl(
                getRestaurantUseCase = getRestaurantUseCase,
                restaurantItemMapper = restaurantItemMapper)
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        unmockkStatic(Log::class)
    }

    init {
        "RestaurantListViewModelImpl" should {
            "be in Initial state after creation" {
                vm.state.value shouldBe State.Initial
            }
        }

        "loadData(false)" should {
            "set Loading state" {
                // given:
                every { getRestaurantUseCase() } returns emptyFlow()

                // when:
                vm.load(false)

                // then
                vm.state.value shouldBe State.Loading(isRetryOrInitial = true)
            }

            "set Content state when data emitted" {
                // given:
                every { getRestaurantUseCase() } returns flow {
                    emit(emptyList())
                }
                every { restaurantItemMapper(any()) } returns mockk()

                // when:
                vm.load(false)

                // then
                vm.state.value shouldBe State.Content(list = emptyList())
            }

            "set NetworkError state when IO error flagged" {
                // given:
                every { getRestaurantUseCase() } returns flow {
                    flagIoError("Test IO Error")
                }
                every { restaurantItemMapper(any()) } returns mockk()

                // when:
                vm.load(false)

                // then
                vm.state.value shouldBe State.NetworkError
            }

            "set GeneralError state when non-RepositoryException thrown" {
                // given:
                every { getRestaurantUseCase() } returns flow {
                    throw IllegalArgumentException("Just for testing purposes")
                }
                every { restaurantItemMapper(any()) } returns mockk()

                // when:
                vm.load(false)

                // then
                vm.state.value shouldBe State.GeneralError
            }

            "set GeneralError state when non-exception thrown" {
                // given:
                every { getRestaurantUseCase() } returns flow {
                    throw Error("Test very grave issue")
                }
                every { restaurantItemMapper(any()) } returns mockk()

                // when:
                vm.load(false)

                // then
                vm.state.value shouldBe State.GeneralError
            }
        }

    }
}