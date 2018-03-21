package entities

import java.math.BigDecimal
import java.math.BigInteger
import javax.persistence.Basic
import javax.persistence.Entity
import javax.persistence.Id

/**
 * [Entity] representing accounts identified by [id] with [balance]
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2018-03-20)
 */
@Entity
data class Account(@Id var id: BigInteger, @Basic var balance: BigDecimal) {
    // for hibernate compatibility
    @Suppress("unused")
    private constructor() : this(BigInteger.ZERO, BigDecimal.ZERO)
}