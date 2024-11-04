package br.com.caju.card.adapter.output.dynamoDB

import br.com.caju.card.adapter.output.dynamoDB.model.TransactionModel
import br.com.caju.card.adapter.output.dynamoDB.model.toApplicationModel
import br.com.caju.card.adapter.output.dynamoDB.model.toModel
import br.com.caju.card.application.port.output.TransactionData
import br.com.caju.card.application.port.output.TransactionOutputPort
import br.com.caju.card.common.exceptions.InternalErrorException
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import org.springframework.stereotype.Component

@Component
class TransactionOutputAdapter(
    private val mapper: DynamoDBMapper
): TransactionOutputPort {

    override fun saveTransaction(transactionData: TransactionData) = runCatching {
        val model = transactionData.toModel()
        mapper.save(model)
    }.getOrElse {
        throw InternalErrorException("Erro interno ao salvar transação")
    }

    override fun findByTransactionId(transactionId: String): TransactionData? = runCatching {
        val expression = DynamoDBQueryExpression<TransactionModel>()
            .withKeyConditionExpression("#pk = :pk")
            .withExpressionAttributeNames(
                mapOf("#pk" to TransactionModel.PK_FIELD_NAME)
            ).withExpressionAttributeValues(
                mapOf(":pk" to AttributeValue().withS(transactionId))
            )
        mapper.query(TransactionModel::class.java, expression).singleOrNull()
    }.map {
        it?.toApplicationModel()
    }.getOrElse {
        throw InternalErrorException("Erro ao buscar transação com ID: $transactionId")
    }

    override fun findByAccountId(accountId: String): List<TransactionData> = runCatching {
        val expression = DynamoDBQueryExpression<TransactionModel>()
            .withIndexName(TransactionModel.GSI_ACCOUNT_INDEX)
            .withKeyConditionExpression("#accountId = :accountId")
            .withExpressionAttributeNames(
                mapOf("#accountId" to TransactionModel.SK_FIELD_NAME)
            ).withExpressionAttributeValues(
                mapOf(":accountId" to AttributeValue().withS(accountId))
            )
            .withConsistentRead(false)

        mapper.query(TransactionModel::class.java, expression)
    }.map { results ->
        results.map { it.toApplicationModel() }
    }.getOrElse {
        throw InternalErrorException("Erro ao buscar transações para a conta: $accountId")
    }
}