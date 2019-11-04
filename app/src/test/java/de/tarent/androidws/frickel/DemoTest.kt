package de.tarent.androidws.frickel

import io.kotlintest.IsolationMode
import io.kotlintest.TestCase
import io.kotlintest.matchers.numerics.shouldBeExactly
import io.kotlintest.specs.WordSpec

internal class DemoTest : WordSpec() {

    override fun isolationMode() = IsolationMode.InstancePerTest

    private lateinit var sut: Demo

    override fun beforeTest(testCase: TestCase) {
        sut = Demo()
    }

    init {
        "calculate" should {
            "return the sum of its arguments" {
                val result = sut.calculate(A, B)

                result shouldBeExactly A + B
            }
        }
    }

    companion object {
        private const val A = 10
        private const val B = 20
    }
}