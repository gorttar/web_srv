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

/**
 * @author Andrey Antipov (andrey.antipov@cxense.com) (2018-03-20 19:57)
 */

private val accountingEMF = createEntityManagerFactory(accountingUnitName)
private val accountingSM = SessionManager(accountingEMF)

private val testObject = AccountingController()

private val account1Id = BigInteger.ONE
private val account1Balance = BigDecimal.ZERO

private val account2Id = BigInteger.valueOf(2)
private val account2Balance = BigDecimal.valueOf(500)

class AccountingControllerTest {

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
            arrayOf(account2Id, BigDecimal.ONE, SUCCESS, "Ok", account2Balance.subtract(BigDecimal.ONE)),
            arrayOf(account2Id, BigDecimal.TEN, SUCCESS, "Ok", account2Balance.subtract(BigDecimal.TEN)),
            // full withdraw
            arrayOf(account1Id, account1Balance, SUCCESS, "Ok", BigDecimal.ZERO),
            arrayOf(account2Id, account2Balance, SUCCESS, "Ok", BigDecimal.ZERO),

            // negative cases
            // account not found
            arrayOf(BigInteger.ZERO, BigDecimal.ZERO, FAILURE, "Account not found", null),
            // withdraw more than balance
            arrayOf(account2Id, account2Balance.add(BigDecimal.ONE), FAILURE, "Need more gold", account2Balance),
            // withdraw negative amount
            arrayOf(account2Id, BigDecimal.valueOf(-1), FAILURE, "Can't withdraw negative amount of money", account2Balance))

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

        val actualBalance = accountingSM.withSession {
            it.createQuery("select a.balance from Account a where a.id = :id", BigDecimal::class.java)
                    .setParameter("id", accountId)
                    .singleResult
        }

        if (expectedBalance != null) {
            assertEquals(actualBalance.toDouble(), expectedBalance.toDouble(), 1e-6)
        }
    }

    @Test
    fun testDeposit() {
    }

    @Test
    fun testTransfer() {
    }
}