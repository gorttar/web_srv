package entities

import helpers.hibernate.SessionManagerTest
import javax.persistence.Entity
import javax.persistence.Id

/**
 * simple test [Entity] used by [SessionManagerTest]
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2018-03-19)
 */
@Entity
data class TestEntity(@Id var name: String?) {
    // for hibernate compatibility
    @Suppress("unused")
    private constructor() : this(null)
}