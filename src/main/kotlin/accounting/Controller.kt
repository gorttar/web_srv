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
import javax.persistence.EntityManager
import javax.persistence.Persistence.createEntityManagerFactory

const val accountingUnitName = "accountingUnit"

/**
 * controller of accounting operations
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2018-03-18)
 */
@RestController
class Controller {
    private val sessionManager = SessionManager(createEntityManagerFactory(accountingUnitName))

    /**
     * withdraw specified [amount] of money from [Account] identified by [accountId]
     * should return [Response] with [Response.result] equals to [FAILURE] in following cases
     * [amount] is negative
     * there is no [Account] identified by [accountId] in database
     * [amount] is more than [Account.balance]
     */
    @RequestMapping("/withdraw", method = [POST])
    fun withdraw(@RequestParam accountId: BigInteger,
                 @RequestParam amount: BigDecimal): Response {
        val operation = "withdraw"
        val arguments = mapOf("accountId" to accountId, "amount" to amount)
        return if (amount < BigDecimal.ZERO) {
            Response(
                    operation,
                    arguments, FAILURE,
                    "Can't $operation negative amount of money")
        } else {
            sessionManager.withTransaction { it.doWithdraw(accountId, amount, operation, arguments) }
        }
    }

    /**
     * deposit specified [amount] of money to [Account] identified by [accountId]
     * should create new [Account] with [Account.balance] = [amount] if there is no [Account] identified by [accountId] in database
     * should return [Response] with [Response.result] equals to [FAILURE] in following cases
     * [amount] is negative
     */
    @RequestMapping("/deposit", method = [POST])
    fun deposit(@RequestParam accountId: BigInteger,
                @RequestParam amount: BigDecimal): Response {
        val operation = "deposit"
        val arguments = mapOf("accountId" to accountId, "amount" to amount)
        return if (amount < BigDecimal.ZERO) {
            Response(
                    operation,
                    arguments, FAILURE,
                    "Can't $operation negative amount of money")
        } else {
            sessionManager.withTransaction { it.doDeposit(accountId, amount, operation, arguments) }
        }
    }

    /**
     * transfer specified [amount] of money from [Account] identified by [senderId] to [Account] identified by [recipientId]
     * should create new [Account] with [Account.balance] = [amount] if there is no [Account] identified by [recipientId] in database
     * should return [Response] with [Response.result] equals to [FAILURE] in following cases
     * [amount] is negative
     * there is no [Account] identified by [senderId] in database
     * [amount] is more than [Account.balance] on [Account] identified by [senderId]
     */
    @RequestMapping("/transfer", method = [POST])
    fun transfer(@RequestParam senderId: BigInteger,
                 @RequestParam recipientId: BigInteger,
                 @RequestParam amount: BigDecimal): Response {
        val operation = "transfer"
        val arguments = mapOf("senderId" to senderId, "recipientId" to recipientId, "amount" to amount)
        return if (amount < BigDecimal.ZERO) {
            Response(
                    operation,
                    arguments, FAILURE,
                    "Can't $operation negative amount of money")
        } else {
            class TransactionException(val response: Response) : Exception()

            fun Response.breakTransactionOnFailure() =
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
                                     arguments: Map<String, Any>): Response {
    val accountIterator = this
            .createQuery("select a from Account a where a.id = :accountId", Account::class.java)
            .setParameter("accountId", accountId)
            .resultList
            .iterator()

    return if (accountIterator.hasNext()) {
        val account = accountIterator.next()
        if (account.balance < amount) {
            Response(
                    operation,
                    arguments, FAILURE,
                    "We need more gold")
        } else {
            account.balance = account.balance - amount
            this.merge(account)
            Response(
                    operation,
                    arguments, SUCCESS,
                    "Ok")
        }
    } else {
        Response(
                operation,
                arguments, FAILURE,
                "Account not found")
    }
}

private fun EntityManager.doDeposit(accountId: BigInteger,
                                    amount: BigDecimal,
                                    operation: String,
                                    arguments: Map<String, Any>): Response {
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

    return Response(
            operation,
            arguments, SUCCESS,
            "Ok")
}

