package br.com.caju.card.adapter.output.dynamoDB.model

import br.com.caju.card.application.port.output.TransactionData
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import java.math.BigDecimal
import java.util.UUID

@DynamoDBTable(tableName = "Transactions")
data class TransactionModel(
    @DynamoDBHashKey(attributeName = PK_FIELD_NAME)
    var id: String = UUID.randomUUID().toString(),

    @DynamoDBIndexHashKey(
        attributeName = SK_FIELD_NAME,
        globalSecondaryIndexName = GSI_ACCOUNT_INDEX
    )
    var account: String? = null,

    @DynamoDBAttribute(attributeName = "amount")
    var amount: BigDecimal? = null,

    @DynamoDBAttribute(attributeName = "mcc")
    var mcc: Int? = null,

    @DynamoDBAttribute(attributeName = "merchantName")
    var merchant: String? = null,
) {
    companion object {
        const val PK_FIELD_NAME = "transactionId"
        const val SK_FIELD_NAME = "accountId"
        const val GSI_ACCOUNT_INDEX = "AccountIndex"
    }
}

fun TransactionData.toModel() = TransactionModel(
    id = this.id.toString(),
    account = this.account,
    amount = this.amount,
    mcc = this.mcc,
    merchant = this.merchant
)

fun TransactionModel.toApplicationModel() = TransactionData(
    id = UUID.fromString(this.id),
    account = this.account ?: "",
    amount = this.amount ?: BigDecimal.ZERO,
    mcc = this.mcc ?: 0,
    merchant = this.merchant ?: ""
)