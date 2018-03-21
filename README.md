# Accounting web application test task
## Specification
1. There should be the following **RESTful** operations available:
    1. Transfer money from one account to another
    1. Deposit money to account
    1. Withdraw money from account
1. Any SQL in-memory database can be used as data storage
1. Gradle or maven can be used as project build system
1. Application should be built into single executable jar file
1. Java or Kotlin should be used as programming language
## Assumptions
1. There is no authentication in application
1. There is no SSL or TLS encryption on HTTP traffic between application and client
1. Deposit operation should create new account if there isn't one already in database
1. Transfer operation should create new recipient account if there isn't one already in database
1. There should be no possibility to perform operations with negative amount of money as argument
## Decisions
1. Kotlin is used as programming language because it concise and funny compared to Java.
1. Gradle is used because it's less verbose than maven
1. Custom hibernate session manager is used in order to illustrate possibility of concise and clean approach to session and transaction handling
without using not so transparent annotations and interceptors 
## How to build and run
* run gradle task bootJar in order to build single executable jar file.
Resulting file is located at **${PROJECT_ROOT}/build/libs/web-rest-service-0.1.0.jar**
* you can run application by one of the following ways:
    * run **accounting.ApplicationKt.main** directly from IntelliJ IDEA
    * run gradle task bootRun
    * run **java -jar ${PROJECT_ROOT}/build/libs/web-rest-service-0.1.0.jar**