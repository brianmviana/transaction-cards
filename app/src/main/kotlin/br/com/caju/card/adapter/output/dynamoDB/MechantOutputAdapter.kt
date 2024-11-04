package br.com.caju.card.adapter.output.dynamoDB

import br.com.caju.card.adapter.output.dynamoDB.model.MerchantModel
import br.com.caju.card.adapter.output.dynamoDB.model.toApplicationModel
import br.com.caju.card.application.port.output.MerchantData
import br.com.caju.card.application.port.output.MerchantOutputPort
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import org.springframework.stereotype.Component

@Component
class MechantOutputAdapter(
    private val mapper: DynamoDBMapper
): MerchantOutputPort {

    override fun findMerchantByName(name: String): MerchantData? = runCatching {
        val expression = DynamoDBQueryExpression<MerchantModel>()
            .withKeyConditionExpression("#pk = :pk")
            .withExpressionAttributeNames(
                mapOf("#pk" to MerchantModel.PK_FIELD_NAME)
            ).withExpressionAttributeValues(
                mapOf(":pk" to AttributeValue().withS(name))
            )
        mapper.query(MerchantModel::class.java, expression).singleOrNull()
    }.map {
        it?.toApplicationModel()
    }.getOrThrow()

}