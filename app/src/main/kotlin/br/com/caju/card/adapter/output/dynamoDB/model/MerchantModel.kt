package br.com.caju.card.adapter.output.dynamoDB.model

import br.com.caju.card.application.port.output.MerchantData
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable

@DynamoDBTable(tableName = "Merchants")
data class MerchantModel(
    @DynamoDBHashKey(attributeName = PK_FIELD_NAME)
    var name: String? = null,

    @get:DynamoDBAttribute(attributeName = "mcc")
    var mcc: Int? = null,
){
    companion object {
        const val PK_FIELD_NAME = "merchantName"
    }
}

fun MerchantData.toModel(): MerchantModel = MerchantModel(
    name = name,
    mcc = mcc
)

fun MerchantModel.toApplicationModel(): MerchantData? =
    name?.let {
        MerchantData(
            name = it,
            mcc = checkNotNull(mcc) { "O campo mcc não pode ser null" }
        )
    }
