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

    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        unmockkStatic(Log::class)
    }

}