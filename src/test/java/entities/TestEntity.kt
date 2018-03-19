package entities

import javax.persistence.Entity
import javax.persistence.Id

/**
 * @author Andrey Antipov (andrey.antipov@cxense.com) (2018-03-19 18:16)
 */
@Entity
data class TestEntity(@Id var name: String?) {
    // for hibernate compatibility
    @Suppress("unused")
    private constructor() : this(null)
}