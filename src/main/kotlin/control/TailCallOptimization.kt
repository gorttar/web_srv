package control

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2018-04-09)
 */
typealias TCFunction<A, B> = (A) -> TCResult<A, B>

@Suppress("unused")
sealed class TCResult<A, out B> : () -> B

private class Call<A, out B>(private val a: A, private val nextStep: TCFunction<A, B>) : TCResult<A, B>() {
    override fun invoke(): B {
        var step = nextStep(a)
        while (step is Call<A, B>) {
            step = step.nextStep(step.a)
        }
        return step()
    }
}

private class Ret<A, out B>(private val b: B) : TCResult<A, B>() {
    override fun invoke(): B = b
}

infix fun <A, B> TCFunction<A, B>.callOn(a: A): TCResult<A, B> = Call(a, this)

fun <A, B> B.ret(): TCResult<A, B> = Ret(this)