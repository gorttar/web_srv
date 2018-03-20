package accounting

/**
 * @author Andrey Antipov (andrey.antipov@cxense.com) (2018-03-20 19:45)
 */
data class AccountingResponse(
        val operation: String,
        val arguments: Map<String, Any>,
        val result: OperationResult,
        val message: String)

enum class OperationResult {
    FAILURE, SUCCESS
}