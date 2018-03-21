package helpers.hibernate

import entities.TestEntity
import org.testng.Assert.*
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import javax.persistence.EntityManager
import javax.persistence.Persistence.createEntityManagerFactory

private val testEMF = createEntityManagerFactory("testPersistence")
private val testObject = SessionManager(testEMF)
private val testEntity1 = TestEntity("e1")
private const val testErrorMessage = "test error"

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2017-02-28)
 */
class SessionManagerTest {
    @BeforeMethod
    fun setUp() {
        val em = testEMF.createEntityManager()
        try {
            val tx = em.transaction
            tx.begin()
            em.createNativeQuery("delete from TestEntity").executeUpdate()
            em.persist(testEntity1)
            tx.commit()
        } finally {
            em.close()
        }
    }

    @AfterClass
    fun tearDown() = testEMF.close()

    @Test
    fun `withTransaction should successfully persist new entity and close entity manager`() {
        val testEntity = TestEntity("e2")
        var exposedEm: EntityManager? = null
        testObject.withTransaction {
            exposedEm = it
            it.persist(testEntity)
        }
        assertEquals(
                testEMF.createEntityManager().createQuery("select e from TestEntity e order by e.name", TestEntity::class.java).resultList,
                listOf(testEntity1, testEntity))
        assertFalse(exposedEm!!.isOpen)
    }

    @Test
    fun `withTransaction should fail to persist new entity but close entity manager on exception thrown from body code`() {
        val testEntity = TestEntity("e2")
        var actual: List<TestEntity>? = null
        var exposedEm: EntityManager? = null
        try {
            testObject.withTransaction<Any> {
                exposedEm = it
                it.persist(testEntity)
                actual = it.createQuery("select e from TestEntity e", TestEntity::class.java).resultList
                throw TestException(testErrorMessage)
            }
            fail("Should throw RuntimeException")
        } catch (e: TestException) {
            assertEquals(e.message, testErrorMessage)
            assertEquals(
                    testEMF.createEntityManager().createQuery("select e from TestEntity e", TestEntity::class.java).resultList,
                    listOf(testEntity1))
            assertEquals(
                    actual,
                    listOf(testEntity1, testEntity))
            assertFalse(exposedEm!!.isOpen)
        }
    }

    @Test
    fun `withSession should successfully return result and close entity manager`() {
        var exposedEm: EntityManager? = null
        assertEquals(
                testObject.withSession {
                    exposedEm = it
                    it.createQuery("select e from TestEntity e", TestEntity::class.java).resultList
                },
                listOf(testEntity1))
        assertFalse(exposedEm!!.isOpen)
    }

    @Test
    fun `withSession should fail but close entity manager on exception thrown from body code`() {
        var exposedEm: EntityManager? = null
        var actual: List<TestEntity>? = null
        try {
            testObject.withSession<Any> {
                exposedEm = it
                actual = it.createQuery("select e from TestEntity e", TestEntity::class.java).resultList
                throw TestException(testErrorMessage)
            }
            fail("Should throw RuntimeException")
        } catch (e: TestException) {
            assertEquals(e.message, testErrorMessage)
            assertFalse(exposedEm!!.isOpen)
            assertEquals(actual, listOf(testEntity1))
        }

    }
}