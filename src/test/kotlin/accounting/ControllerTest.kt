package accounting

import accounting.OperationResult.FAILURE
import accounting.OperationResult.SUCCESS
import entities.Account
import helpers.hibernate.SessionManager
import org.testng.Assert.assertEquals
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.math.BigDecimal
import java.math.BigInteger
import javax.persistence.Persistence.createEntityManagerFactory

private val accountingEMF = createEntityManagerFactory(accountingUnitName)
private val accountingSM = SessionManager(accountingEMF)

private val testObject = Controller()

private val account1Id = BigInteger.ONE
private val account1Balance = BigDecimal.ZERO

private val account2Id = BigInteger.valueOf(2)
private val account2Balance = BigDecimal.valueOf(500)

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2018-03-20)
 */
class ControllerTest {
    @BeforeMethod
    fun setUp() = accountingSM.withTransaction {
        it.createNativeQuery("delete from Account").executeUpdate()
        it.persist(Account(account1Id, account1Balance))
        it.persist(Account(account2Id, account2Balance))
    }

    @AfterClass
    fun tearDown() = accountingEMF.close()

    @DataProvider(name = "data for testWithdraw")
    fun `data for testWithdraw`() = arrayOf(
            // positive cases
            // zero withdraw
            arrayOf(account1Id, BigDecimal.ZERO, SUCCESS, "Ok", account1Balance),
            arrayOf(account2Id, BigDecimal.ZERO, SUCCESS, "Ok", account2Balance),
            // non zero withdraw
            arrayOf(account2Id, BigDecimal.ONE, SUCCESS, "Ok", account2Balance - BigDecimal.ONE),
            arrayOf(account2Id, BigDecimal.TEN, SUCCESS, "Ok", account2Balance - BigDecimal.TEN),
            // full withdraw
            arrayOf(account1Id, account1Balance, SUCCESS, "Ok", BigDecimal.ZERO),
            arrayOf(account2Id, account2Balance, SUCCESS, "Ok", BigDecimal.ZERO),

            // negative cases
            // account not found
            arrayOf(BigInteger.ZERO, BigDecimal.ZERO, FAILURE, "Account not found", null),
            // withdraw more than balance
            arrayOf(account2Id, account2Balance + BigDecimal.ONE, FAILURE, "We need more gold", account2Balance),
            // withdraw negative amount
            arrayOf(account2Id,
                    BigDecimal.valueOf(-1),
                    FAILURE,
                    "Can't withdraw negative amount of money",
                    account2Balance))

    @Test(dataProvider = "data for testWithdraw")
    fun testWithdraw(accountId: BigInteger,
                     amount: BigDecimal,
                     expectedResult: OperationResult,
                     expectedMessage: String,
                     expectedBalance: BigDecimal?) {
        val (operation, arguments, result, message) = testObject.withdraw(accountId, amount)

        assertEquals(operation, "withdraw")
        assertEquals(arguments, mapOf("accountId" to accountId, "amount" to amount))
        assertEquals(result, expectedResult)
        assertEquals(message, expectedMessage)

        if (expectedBalance != null) {
            assertEquals(getAccountBalance(accountId).toDouble(), expectedBalance.toDouble(), 1e-6)
        }
    }

    @DataProvider(name = "data for testDeposit")
    fun `data for testDeposit`() = arrayOf(
            // positive cases
            // zero deposit
            arrayOf(account1Id, BigDecimal.ZERO, SUCCESS, "Ok", account1Balance),
            arrayOf(account2Id, BigDecimal.ZERO, SUCCESS, "Ok", account2Balance),
            // non zero deposit
            arrayOf(account1Id, BigDecimal.ONE, SUCCESS, "Ok", account1Balance + BigDecimal.ONE),
            arrayOf(account1Id, BigDecimal.TEN, SUCCESS, "Ok", account1Balance + BigDecimal.TEN),
            arrayOf(account2Id, BigDecimal.ONE, SUCCESS, "Ok", account2Balance + BigDecimal.ONE),
            arrayOf(account2Id, BigDecimal.TEN, SUCCESS, "Ok", account2Balance + BigDecimal.TEN),
            // deposit to new account
            arrayOf(BigInteger.valueOf(3), BigDecimal.ONE, SUCCESS, "Ok", BigDecimal.ONE),

            // negative cases
            // deposit negative amount
            arrayOf(account2Id,
                    BigDecimal.valueOf(-1),
                    FAILURE,
                    "Can't deposit negative amount of money",
                    account2Balance))

    @Test(dataProvider = "data for testDeposit")
    fun testDeposit(accountId: BigInteger,
                    amount: BigDecimal,
                    expectedResult: OperationResult,
                    expectedMessage: String,
                    expectedBalance: BigDecimal) {
        val (operation, arguments, result, message) = testObject.deposit(accountId, amount)

        assertEquals(operation, "deposit")
        assertEquals(arguments, mapOf("accountId" to accountId, "amount" to amount))
        assertEquals(result, expectedResult)
        assertEquals(message, expectedMessage)

        assertEquals(getAccountBalance(accountId).toDouble(), expectedBalance.toDouble(), 1e-6)
    }

    @DataProvider(name = "data for testTransfer")
    fun `data for testTransfer`() = arrayOf(
            // positive cases
            // zero transfer
            arrayOf(account1Id, account2Id, BigDecimal.ZERO, SUCCESS, "Ok", account1Balance, account2Balance),
            arrayOf(account2Id, account1Id, BigDecimal.ZERO, SUCCESS, "Ok", account2Balance, account1Balance),
            // non zero transfer
            arrayOf(account2Id, account1Id,
                    BigDecimal.ONE,
                    SUCCESS, "Ok",
                    account2Balance - BigDecimal.ONE, account1Balance + BigDecimal.ONE),
            arrayOf(account2Id, account1Id,
                    BigDecimal.TEN,
                    SUCCESS, "Ok",
                    account2Balance - BigDecimal.TEN, account1Balance + BigDecimal.TEN),
            // full transfer
            arrayOf(account2Id, account1Id,
                    account2Balance,
                    SUCCESS, "Ok", BigDecimal.ZERO,
                    account1Balance + account2Balance),

            // negative cases
            // account not found
            arrayOf(BigInteger.ZERO, account1Id,
                    BigDecimal.ZERO,
                    FAILURE, "Account not found",
                    null, account1Balance),
            // transfer more than sender balance
            arrayOf(account2Id, account1Id,
                    account2Balance + BigDecimal.ONE,
                    FAILURE, "We need more gold",
                    account2Balance, account1Balance),
            // transfer negative amount
            arrayOf(account2Id, account1Id,
                    BigDecimal.valueOf(-1),
                    FAILURE, "Can't transfer negative amount of money",
                    account2Balance, account1Balance))

    @Test(dataProvider = "data for testTransfer")
    fun testTransfer(senderId: BigInteger,
                     recipientId: BigInteger,
                     amount: BigDecimal,
                     expectedResult: OperationResult,
                     expectedMessage: String,
                     expectedSenderBalance: BigDecimal?,
                     expectedRecipientBalance: BigDecimal) {
        val (operation, arguments, result, message) = testObject.transfer(senderId, recipientId, amount)

        assertEquals(operation, "transfer")
        assertEquals(arguments, mapOf("senderId" to senderId, "recipientId" to recipientId, "amount" to amount))
        assertEquals(result, expectedResult)
        assertEquals(message, expectedMessage)

        assertEquals(getAccountBalance(recipientId).toDouble(), expectedRecipientBalance.toDouble(), 1e-6)
        if (expectedSenderBalance != null) {
            assertEquals(getAccountBalance(senderId).toDouble(), expectedSenderBalance.toDouble(), 1e-6)
        }
    }
}

private fun getAccountBalance(accountId: BigInteger) = accountingSM.withSession {
    it.createQuery("select a.balance from Account a where a.id = :accountId", BigDecimal::class.java)
            .setParameter("accountId", accountId)
            .singleResult
}