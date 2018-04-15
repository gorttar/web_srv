package control

import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import kotlin.system.measureTimeMillis

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2018-04-09)
 */
private fun evenM(n: Int): TCResult<Int, Boolean> = if (n == 0) true.ret() else ::oddM `^to` n - 1

private fun oddM(n: Int): TCResult<Int, Boolean> = if (n == 0) false.ret() else ::evenM `^to` n - 1

private val evenF: TCFunction<Int, Boolean> = { n -> if (n == 0) true.ret() else oddF `^to` n - 1 }

private val oddF: TCFunction<Int, Boolean> = { n -> if (n == 0) false.ret() else evenF `^to` n - 1 }

private const val recursionLimit = 1000_000_000L
private val deepRecursionTrampolinedF: TCFunction<Long, Unit> = {
    if (it == recursionLimit) {
        println("deepRecursionTrampolinedF passes $it recursive calls").ret()
    } else {
        deepRecursionTrampolinedFAlias `^to` it + 1
    }
}
private val deepRecursionTrampolinedFAlias: TCFunction<Long, Unit> = deepRecursionTrampolinedF

private fun deepRecursionTrampolinedM(it: Long): TCResult<Long, Unit> =
        if (it == recursionLimit) {
            println("deepRecursionTrampolinedF passes $it recursive calls").ret()
        } else {
            deepRecursionTrampolinedMAlias `^to` it + 1
        }

private val deepRecursionTrampolinedMAlias: TCFunction<Long, Unit> = ::deepRecursionTrampolinedM

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
    fun `"evenM" should work properly for positive numbers`(n: Int, expected: Boolean) =
            assertEquals(evenM(n)(), expected)

    @Test(dataProvider = "data for \"even\" tests")
    fun `"evenF" should work properly for positive numbers`(n: Int, expected: Boolean) =
            assertEquals(evenF(n)(), expected)

    @DataProvider
    fun `data for "odd" tests`() = `data for "even" tests`()
            .map { arrayOf(it[0], !(it[1] as Boolean)) }
            .toTypedArray()

    @Test(dataProvider = "data for \"odd\" tests")
    fun `"oddM" should work properly for positive numbers`(n: Int, expected: Boolean) =
            assertEquals(oddM(n)(), expected)

    @Test(dataProvider = "data for \"odd\" tests")
    fun `"oddF" should work properly for positive numbers`(n: Int, expected: Boolean) =
            assertEquals(oddF(n)(), expected)

    @Test(enabled = false)
    fun `benchmark test`() {
        val trampolineFMillis = measureTimeMillis { deepRecursionTrampolinedF(1)() }
        println("Trampolined function based version is executed for $trampolineFMillis milliseconds")

        val trampolineMMillis = measureTimeMillis { deepRecursionTrampolinedM(1)() }
        println("Trampolined method based version is executed for $trampolineMMillis milliseconds")

        tailrec fun deepRecursionTailrec(a: Long): Unit =
                if (a == recursionLimit) {
                    println("deepRecursionTailrec passes $a recursive calls")
                } else deepRecursionTailrec(a + 1)

        val tailrecMillis = measureTimeMillis { deepRecursionTailrec(1) }
        println("Tailrec version is executed for $tailrecMillis milliseconds")
        println("Tailrec version is ${trampolineFMillis.toDouble() / tailrecMillis} times faster than trampolined function based one")
        println("Tailrec version is ${trampolineMMillis.toDouble() / tailrecMillis} times faster than trampolined method based one")

        val iterationMillis = measureTimeMillis {
            var a: Long = 1
            while (a != recursionLimit) {
                a++
            }
            println("$a iterations passed")
        }
        println("Iteration version is executed for $iterationMillis milliseconds")
        println("Iteration version is ${tailrecMillis.toDouble() / iterationMillis} times faster than tailrec one")
    }
}