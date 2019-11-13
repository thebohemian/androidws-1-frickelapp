package de.tarent.androidws.clean.test

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.extensions.TestListener

/**
 * Forces [LiveData] instances to update on a JVM thread instead of calling into Android
 * platform code.
 */
class InstantExecutorTestListener : TestListener {

    @SuppressLint("RestrictedApi")
    override fun beforeTest(testCase: TestCase) {
        ArchTaskExecutor.getInstance()
                .setDelegate(object : TaskExecutor() {
                    override fun executeOnDiskIO(runnable: Runnable) = runnable.run()

                    override fun postToMainThread(runnable: Runnable) = runnable.run()

                    override fun isMainThread(): Boolean = true
                })
    }

    @SuppressLint("RestrictedApi")
    override fun afterTest(testCase: TestCase, result: TestResult) {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }

}
