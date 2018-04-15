package cdf

import org.testng.Assert.fail
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2018-04-14)
 */
class CDFHelperTest {
    @DataProvider
    fun `data for test cdfProvider negative cases`() = arrayOf(
            arrayOf(mapOf<Double, Double>(), IllegalArgumentException("There should be at least two reference points but found {}")),
            arrayOf(mapOf(.0 to .0), IllegalArgumentException("There should be at least two reference points but found {0.0=0.0}")),
            arrayOf(mapOf(.0 to null, 1.0 to 1.0), NullPointerException()),
            arrayOf(mapOf(null to .0, 1.0 to 1.0), NullPointerException()),
            arrayOf(mapOf(.0 to .5, 1.0 to 1.0), IllegalArgumentException("Minimal reference point value should be 0 but found 0.5")))

    @Test(dataProvider = "data for test cdfProvider negative cases")
    fun `test cdfProvider negative cases`(referencePoints: Map<Double?, Double?>, expected: Throwable) {
        { CDFHelper.cdfProvider(referencePoints) } `should throw` expected
    }
}

infix fun (() -> Any).`should throw`(expected: Throwable) {
    try {
        this()
        fail("Exception should be thrown")
    } catch (actual: Throwable) {
        if (actual::class != expected::class) throw actual
        if (actual.message != expected.message) throw actual
    }
}

