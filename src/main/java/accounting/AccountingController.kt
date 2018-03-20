package accounting

import accounting.OperationResult.FAILURE
import accounting.OperationResult.SUCCESS
import entities.Account
import helpers.hibernate.SessionManager
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.POST
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.atomic.AtomicLong
import javax.persistence.Persistence.createEntityManagerFactory

const val accountingUnitName = "accountingUnit"

@RestController
class AccountingController {
    private val counter = AtomicLong()
    private val sessionManager = SessionManager(createEntityManagerFactory(accountingUnitName))

    @RequestMapping("/greeting")
    fun greeting(@RequestParam(defaultValue = "World") name: String) =
            Greeting(counter.incrementAndGet(), "Hello, $name")

    @RequestMapping("/withdraw", method = [POST])
    fun withdraw(@RequestParam accountId: BigInteger,
                 @RequestParam amount: BigDecimal): AccountingResponse {
        val arguments = mapOf("accountId" to accountId, "amount" to amount)
        return if (amount < BigDecimal.ZERO) {
            AccountingResponse(
                    "withdraw",
                    arguments, FAILURE,
                    "Can't withdraw negative amount of money")
        } else {
            sessionManager.withTransaction {
                val accountIterator = it.createQuery("select a from Account a where a.id = :accountId", Account::class.java)
                        .setParameter("accountId", accountId)
                        .resultList
                        .iterator()

                if (!accountIterator.hasNext()) {
                    AccountingResponse(
                            "withdraw",
                            arguments, FAILURE,
                            "Account not found")
                } else {
                    val account = accountIterator.next()
                    if (account.balance < amount) {
                        AccountingResponse(
                                "withdraw",
                                arguments, FAILURE,
                                "We need more gold")
                    } else {
                        account.balance = account.balance - amount
                        it.merge(account)
                        AccountingResponse(
                                "withdraw",
                                arguments, SUCCESS,
                                "Ok")
                    }
                }
            }
        }
    }

    @RequestMapping("/deposit", method = [POST])
    fun deposit(@RequestParam accountId: BigInteger,
                @RequestParam amount: BigDecimal): AccountingResponse = TODO()

    @RequestMapping("/transfer", method = [POST])
    fun transfer(@RequestParam senderId: BigInteger,
                 @RequestParam recipientId: BigInteger,
                 @RequestParam amount: BigDecimal): AccountingResponse = TODO()
}