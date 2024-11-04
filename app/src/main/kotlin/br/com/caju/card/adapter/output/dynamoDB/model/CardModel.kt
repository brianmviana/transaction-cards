package br.com.caju.card.adapter.output.dynamoDB.model

import br.com.caju.card.application.port.output.CardData
import br.com.caju.card.common.extensions.orZero
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import java.math.BigDecimal

@DynamoDBTable(tableName = "Cards")
data class CardModel(
    @DynamoDBHashKey(attributeName = PK_FIELD_NAME)
    var accountId: String? = null,

    @get:DynamoDBAttribute(attributeName = "foodBalance")
    var foodBalance: BigDecimal? = null,

    @get:DynamoDBAttribute(attributeName = "mealBalance")
    var mealBalance: BigDecimal? = null,

    @get:DynamoDBAttribute(attributeName = "cashBalance")
    var cashBalance: BigDecimal? = null,
){
    companion object {
        const val PK_FIELD_NAME = "accountId"
    }
}

fun CardData.toModel() = CardModel(
    accountId = this.accountId,
    foodBalance = this.foodBalance,
    mealBalance = this.mealBalance,
    cashBalance = this.cashBalance,
)

fun CardModel.toApplicationModel() = CardData(
    accountId = checkNotNull(this.accountId) {"O valor do campo '${CardModel.PK_FIELD_NAME}' não pode ser nulo"},
    foodBalance = this.foodBalance.orZero(),
    mealBalance = this.mealBalance.orZero(),
    cashBalance = this.cashBalance.orZero(),
)