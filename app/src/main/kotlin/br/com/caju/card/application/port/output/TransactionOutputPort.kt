package br.com.caju.card.application.port.output

import br.com.caju.card.domain.Transaction
import java.math.BigDecimal
import java.util.UUID

interface TransactionOutputPort {
    fun saveTransaction(transactionData: TransactionData)
    fun findByTransactionId(transactionId: String): TransactionData?
    fun findByAccountId(accountId: String): List<TransactionData>
}

data class TransactionData(
    val id: UUID = UUID.randomUUID(),
    val account: String,
    val amount: BigDecimal,
    val mcc: Int,
    val merchant: String,
)

fun Transaction.toApplicationModel(): TransactionData = TransactionData(
    id = this.id,
    account = this.account,
    amount = this.amount,
    mcc = this.mcc,
    merchant = this.merchant
)

fun TransactionData.toDomain(): Transaction = Transaction(
    id = this.id,
    account = this.account,
    amount = this.amount,
    mcc = this.mcc,
    merchant = this.merchant
)