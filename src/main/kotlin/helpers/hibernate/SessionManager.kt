package helpers.hibernate

import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.EntityTransaction
import javax.transaction.Transactional

/**
 * helper to deal with [EntityManager] sessions both transactional and not
 * without using of [Transactional] annotation and interceptors
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2017-02-28)
 */
class SessionManager(private val entityManagerFactory: EntityManagerFactory) {
    /**
     * evaluates transactional function represented by [transactionBody] inside of opened [EntityTransaction] and returns it's result
     * currently the only way to rollback transaction is to throw an exception from inside of [transactionBody]
     * [T] - type of result
     */
    fun <T> withTransaction(transactionBody: (EntityManager) -> T): T =
            withSession {
                val tx = it.transaction
                try {
                    tx.begin()
                    val result = transactionBody(it)
                    tx.commit()
                    result
                } catch (e: Throwable) {
                    tx.rollback()
                    throw e
                }
            }


    /**
     * evaluates function represented by [body] inside of opened [EntityManager] session and returns it's result
     * [T] - type of result
     */
    fun <T> withSession(body: (EntityManager) -> T): T {
        val em = entityManagerFactory.createEntityManager()
        return try {
            body(em)
        } finally {
            em.close()
        }
    }
}