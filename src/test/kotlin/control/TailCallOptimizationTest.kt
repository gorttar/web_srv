package control

import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2018-04-09)
 */
private fun evenM(n: Int): TCResult<Int, Boolean> = if (n == 0) true.ret() else ::oddM applyTo n - 1

private fun oddM(n: Int): TCResult<Int, Boolean> = if (n == 0) false.ret() else ::evenM applyTo n - 1

private val evenF: TCFunction<Int, Boolean> = { n -> if (n == 0) true.ret() else oddF applyTo n - 1 }

private val oddF: TCFunction<Int, Boolean> = { n -> if (n == 0) false.ret() else evenF applyTo n - 1 }

class TailCallOptimizationTest {
    @DataProvider
    fun `data for "even" tests`() = arrayOf(
            // small numbers
            arrayOf(0, true),
            arrayOf(1, false),
            // large numbers
            arrayOf(10_000_000, true),
            arrayOf(9_999_999, false))

    @Test(dataProvider = "data for \"even\" tests")
    fun `"evenM" should work properly for positive numbers`(n: Int, expected: Boolean) {
        assertEquals(evenM(n)(), expected)
    }

    @Test(dataProvider = "data for \"even\" tests")
    fun `"evenF" should work properly for positive numbers`(n: Int, expected: Boolean) {
        assertEquals(evenF(n)(), expected)
    }

    @DataProvider
    fun `data for "odd" tests`() = `data for "even" tests`()
            .map { arrayOf(it[0], !(it[1] as Boolean)) }
            .toTypedArray()

    @Test(dataProvider = "data for \"odd\" tests")
    fun `"oddM" should work properly for positive numbers`(n: Int, expected: Boolean) {
        assertEquals(oddM(n)(), expected)
    }

    @Test(dataProvider = "data for \"odd\" tests")
    fun `"oddF" should work properly for positive numbers`(n: Int, expected: Boolean) {
        assertEquals(oddF(n)(), expected)
    }
}

fun main(args: Array<String>) {
    fun infinite(a: Unit): TCResult<Unit, Nothing> = ::infinite applyTo a
    infinite(Unit)()
}