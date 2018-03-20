package accounting

import helpers.hibernate.SessionManager
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.POST
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.atomic.AtomicLong
import javax.persistence.Persistence.createEntityManagerFactory

@RestController
class AccountingController {
    private val counter = AtomicLong()
    private val sessionManager = SessionManager(createEntityManagerFactory("accountingUnit"))

    @RequestMapping("/greeting")
    fun greeting(@RequestParam(defaultValue = "World") name: String) =
            Greeting(counter.incrementAndGet(), "Hello, $name")

    @RequestMapping("/withdraw", method = [POST])
    fun withdraw(@RequestParam accountId: BigInteger,
                 @RequestParam amount: BigDecimal): AccountingResponse = TODO()

    @RequestMapping("/deposit", method = [POST])
    fun deposit(@RequestParam accountId: BigInteger,
                @RequestParam amount: BigDecimal): AccountingResponse = TODO()

    @RequestMapping("/transfer", method = [POST])
    fun transfer(@RequestParam senderId: BigInteger,
                 @RequestParam recipientId: BigInteger,
                 @RequestParam amount: BigDecimal): AccountingResponse = TODO()
}