package entities

import java.math.BigDecimal
import java.math.BigInteger
import javax.persistence.Basic
import javax.persistence.Entity
import javax.persistence.Id

/**
 * @author Andrey Antipov (andrey.antipov@cxense.com) (2018-03-20 19:58)
 */
@Entity
data class Account(@Id var id: BigInteger, @Basic var balance: BigDecimal) {
    // for hibernate compatibility
    @Suppress("unused")
    private constructor() : this(BigInteger.ZERO, BigDecimal.ZERO)
}