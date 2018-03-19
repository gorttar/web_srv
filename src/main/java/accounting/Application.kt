package accounting

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2018-03-18)
 */
@SpringBootApplication
open class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(arrayOf(Application::class.java), args)
        }
    }
}