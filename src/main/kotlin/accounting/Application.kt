package accounting

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * Entry class to start Spring boot application
 * @author Andrey Antipov (gorttar@gmail.com) (2018-03-18)
 */
@SpringBootApplication
open class Application

fun main(args: Array<String>) {
    SpringApplication.run(arrayOf(Application::class.java), args)
}
