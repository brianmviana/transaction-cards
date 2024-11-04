package br.com.caju.card.application.port.input

import br.com.caju.card.domain.Transaction
import java.math.BigDecimal

interface AuthorizerInputPort {

    fun authorizerSimple(inputData: TransactionInputData) : TransactionResult
    fun authorizerWithFallback(inputData: TransactionInputData) : TransactionResult
    fun authorizerDependingMerchant(inputData: TransactionInputData) : TransactionResult
    fun authorizerWithLock(inputData: TransactionInputData) : TransactionResult

}

data class TransactionInputData(
    val account: String,
    val amount: BigDecimal,
    val mcc: Int,
    val merchant: String,
)

enum class TransactionResult(val code: String) {
    APPROVED(code = "00"),
    REJECTED(code = "51"),
    PROCESSING_ERROR(code = "07")
}

fun TransactionInputData.toDomain() = Transaction(
    account = account,
    amount = amount,
    mcc = mcc,
    merchant = merchant
)
