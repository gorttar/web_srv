import kotlin.reflect.KFunction

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2018-04-07)
 */
fun main(args: Array<String>) = sequenceOf(
        "simple recursive function" to ::`simple recursive function` to false,
        "simple tail recursive function" to ::`simple tail recursive function` to false,
        "first of two mutially recursive functions" to ::`first of two mutially recursive functions` to false,
        "second of two mutially recursive functions" to ::`second of two mutially recursive functions` to false,
        "function with two tail recursive calls" to ::`function with two tail recursive calls` to false,
        "curried recursive function" to ::`curried recursive function` to true,
        "curried tail recursive function" to ::`curried tail recursive function` to true)
        .forEach {
            println("${it.first.first} ${
            try {
                val fn: KFunction<Any> = it.first.second
                if (it.second) (fn as (Long) -> (Long) -> String)(1_000_000)(0)
                else (fn as (Long, Long) -> String)(1_000_000, 0)
            } catch (e: Throwable) {
                "failed with ${e::class.simpleName}"
            }
            }")
        }

fun `simple recursive function`(limit: Long, counter: Long): String =
        if (counter >= limit) "successfully reached $counter"
        else `simple recursive function`(limit, counter + 1)

fun `curried recursive function`(limit: Long): (Long) -> String {
    fun inner(counter: Long): String =
            if (counter >= limit) "successfully reached $counter"
            else inner(counter + 1)
    return ::inner
}

fun `curried tail recursive function`(limit: Long): (Long) -> String {
    tailrec fun inner(counter: Long): String =
            if (counter >= limit) "successfully reached $counter"
            else inner(counter + 1)
    return ::inner
}

tailrec fun `simple tail recursive function`(limit: Long, counter: Long): String =
        if (counter >= limit) "successfully reached $counter"
        else `simple tail recursive function`(limit, counter + 1)

tailrec fun `first of two mutially recursive functions`(limit: Long, counter: Long): String =
        if (counter >= limit) "successfully reached $counter"
        else `second of two mutially recursive functions`(limit, counter + 1)

tailrec fun `second of two mutially recursive functions`(limit: Long, counter: Long): String =
        if (counter >= limit) "successfully reached $counter"
        else `first of two mutially recursive functions`(limit, counter + 1)

tailrec fun `function with two tail recursive calls`(limit: Long, counter: Long): String =
        when {
            counter >= limit -> "successfully reached $counter"
            counter % 3 == 0L -> `function with two tail recursive calls`(limit + 2, counter + 1)
            else -> `function with two tail recursive calls`(limit, counter + 1)
        }
