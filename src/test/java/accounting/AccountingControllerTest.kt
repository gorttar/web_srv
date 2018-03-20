package accounting

import entities.Account
import helpers.hibernate.SessionManager
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.math.BigDecimal
import java.math.BigInteger
import javax.persistence.Persistence.createEntityManagerFactory

/**
 * @author Andrey Antipov (andrey.antipov@cxense.com) (2018-03-20 19:57)
 */

private val accountingEMF = createEntityManagerFactory("accountingUnit")
private val accountingSM = SessionManager(accountingEMF)

class AccountingControllerTest {

    @BeforeMethod
    fun setUp() = accountingSM.withTransaction {
        it.createNativeQuery("delete from Account").executeUpdate()
        it.persist(Account(BigInteger.ONE, BigDecimal.valueOf(500)))
        it.persist(Account(BigInteger.valueOf(2), BigDecimal.valueOf(500)))
    }

    @AfterClass
    fun tearDown() = accountingEMF.close()

    @Test
    fun testWithdraw() {
    }

    @Test
    fun testDeposit() {
    }

    @Test
    fun testTransfer() {
    }
}