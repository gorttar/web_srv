package accounting

/**
 * representation of controller's operation response
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2018-03-20)
 */
data class Response(
        val operation: String,
        val arguments: Map<String, Any>,
        val result: OperationResult,
        val message: String)

enum class OperationResult {
    FAILURE, SUCCESS
}