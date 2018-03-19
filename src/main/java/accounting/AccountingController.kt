package accounting

import helpers.hibernate.SessionManager
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.POST
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicLong
import javax.persistence.Persistence.createEntityManagerFactory

@RestController
class AccountingController {
    private val counter = AtomicLong()
    private val sessionManager = SessionManager(createEntityManagerFactory("accounting"))

    @RequestMapping("/greeting")
    fun greeting(@RequestParam(defaultValue = "World") name: String) =
            Greeting(counter.incrementAndGet(), "Hello, $name")

    @RequestMapping("/withdraw", method = [POST])
    fun withdraw(@RequestParam accountId: String,
                 @RequestParam amount: BigDecimal): Any = TODO()

    @RequestMapping("/deposit", method = [POST])
    fun deposit(@RequestParam accountId: String,
                @RequestParam amount: BigDecimal): Any = TODO()

    @RequestMapping("/transfer", method = [POST])
    fun transfer(@RequestParam senderId: String,
                 @RequestParam recipientId: String,
                 @RequestParam amount: BigDecimal): Any = TODO()
}