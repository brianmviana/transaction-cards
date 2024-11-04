package br.com.caju.card.adapter.output.dynamoDB

import br.com.caju.card.adapter.output.dynamoDB.model.CardModel
import br.com.caju.card.adapter.output.dynamoDB.model.toApplicationModel
import br.com.caju.card.adapter.output.dynamoDB.model.toModel
import br.com.caju.card.application.port.output.CardData
import br.com.caju.card.application.port.output.CardOutputPort
import br.com.caju.card.common.exceptions.InternalErrorException
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import org.springframework.stereotype.Component

@Component
class CardOutputAdapter(
    private val mapper: DynamoDBMapper
): CardOutputPort {

    override fun findCardByAccountId(accountId: String): CardData = runCatching {
        val expression = DynamoDBQueryExpression<CardModel>()
            .withKeyConditionExpression("#pk = :pk")
            .withExpressionAttributeNames(
                mapOf("#pk" to CardModel.PK_FIELD_NAME)
            ).withExpressionAttributeValues(
                mapOf(":pk" to AttributeValue().withS(accountId))
            )
        mapper.query(CardModel::class.java, expression).singleOrNull()
            ?: throw InternalErrorException("Mais de um cartão encontrado para o ID da conta: $accountId")
    }.map {
        it.toApplicationModel()
    }.getOrThrow()

    override fun updateBalance(cardData: CardData) = runCatching {
        val dynamoDBMapperconfig = DynamoDBMapperConfig.Builder()
            .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
            .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .build()

        val model = cardData.toModel()

        mapper.save(model, dynamoDBMapperconfig)
    }.getOrElse {
        throw InternalErrorException("Erro interno ao atualizar saldo do cartão")
    }
}
