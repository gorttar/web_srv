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
import javax.persistence.EntityManager
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
        val operation = "withdraw"
        val arguments = mapOf("accountId" to accountId, "amount" to amount)
        return if (amount < BigDecimal.ZERO) {
            AccountingResponse(
                    operation,
                    arguments, FAILURE,
                    "Can't $operation negative amount of money")
        } else {
            sessionManager.withTransaction { it.doWithdraw(accountId, amount, operation, arguments) }
        }
    }

    @RequestMapping("/deposit", method = [POST])
    fun deposit(@RequestParam accountId: BigInteger,
                @RequestParam amount: BigDecimal): AccountingResponse {
        val operation = "deposit"
        val arguments = mapOf("accountId" to accountId, "amount" to amount)
        return if (amount < BigDecimal.ZERO) {
            AccountingResponse(
                    operation,
                    arguments, FAILURE,
                    "Can't $operation negative amount of money")
        } else {
            sessionManager.withTransaction { it.doDeposit(accountId, amount, operation, arguments) }
        }
    }

    @RequestMapping("/transfer", method = [POST])
    fun transfer(@RequestParam senderId: BigInteger,
                 @RequestParam recipientId: BigInteger,
                 @RequestParam amount: BigDecimal): AccountingResponse {
        val operation = "transfer"
        val arguments = mapOf("senderId" to senderId, "recipientId" to recipientId, "amount" to amount)
        return if (amount < BigDecimal.ZERO) {
            AccountingResponse(
                    operation,
                    arguments, FAILURE,
                    "Can't $operation negative amount of money")
        } else {
            class TransactionException(val response: AccountingResponse) : Exception()

            fun AccountingResponse.breakTransactionOnFailure() =
                    if (this.result != SUCCESS) throw TransactionException(this)
                    else this

            try {
                sessionManager.withTransaction {
                    it.doWithdraw(senderId, amount, operation, arguments).breakTransactionOnFailure()
                    it.doDeposit(recipientId, amount, operation, arguments).breakTransactionOnFailure()
                }
            } catch (e: TransactionException) {
                e.response
            }
        }
    }
}

private fun EntityManager.doWithdraw(accountId: BigInteger,
                                     amount: BigDecimal,
                                     operation: String,
                                     arguments: Map<String, Any>): AccountingResponse {
    val accountIterator = this
            .createQuery("select a from Account a where a.id = :accountId", Account::class.java)
            .setParameter("accountId", accountId)
            .resultList
            .iterator()

    return if (accountIterator.hasNext()) {
        val account = accountIterator.next()
        if (account.balance < amount) {
            AccountingResponse(
                    operation,
                    arguments, FAILURE,
                    "We need more gold")
        } else {
            account.balance = account.balance - amount
            this.merge(account)
            AccountingResponse(
                    operation,
                    arguments, SUCCESS,
                    "Ok")
        }
    } else {
        AccountingResponse(
                operation,
                arguments, FAILURE,
                "Account not found")
    }
}

private fun EntityManager.doDeposit(accountId: BigInteger,
                                    amount: BigDecimal,
                                    operation: String,
                                    arguments: Map<String, Any>): AccountingResponse {
    val accountIterator = this
            .createQuery("select a from Account a where a.id = :accountId", Account::class.java)
            .setParameter("accountId", accountId)
            .resultList
            .iterator()

    if (accountIterator.hasNext()) {
        val account = accountIterator.next()
        account.balance = account.balance + amount
        this.merge(account)
    } else {
        this.persist(Account(accountId, amount))
    }

    return AccountingResponse(
            operation,
            arguments, SUCCESS,
            "Ok")
}

