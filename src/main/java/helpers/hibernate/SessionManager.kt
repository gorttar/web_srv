/*
 * Copyright (c) 2018 Andrey Antipov. All Rights Reserved.
 */
package helpers.hibernate

import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.EntityTransaction

/**
 * helper to deal with [EntityManager] sessions both transactional and not
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2017-02-28)
 */
class SessionManager(private val entityManagerFactory: EntityManagerFactory) {
    /**
     * evaluates transactional function represented by transaction body and returns it's result
     *
     * @param transactionBody to be evaluated under [EntityTransaction]
     * @return transaction body evaluation result
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
     * evaluates function represented by body and returns it's result
     *
     * @param body to be evaluated with [EntityManager] as argument
     * @return body evaluation result
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