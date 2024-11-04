package br.com.caju.card.adapter.output.dynamoDB

import br.com.caju.card.adapter.output.dynamoDB.model.CardModel
import br.com.caju.card.adapter.output.dynamoDB.model.toModel
import br.com.caju.card.application.port.output.CardData
import br.com.caju.card.common.exceptions.InternalErrorException
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.math.BigDecimal
import org.junit.Test

class CardOutputAdapterTest{

    private val dynamoDBMapperMock: DynamoDBMapper = mockk()
    private val adapter: CardOutputAdapter = CardOutputAdapter(
        mapper = dynamoDBMapperMock
    )

    private val accountIdFake: String = "123"

    private val cardDataMock = CardData(
        accountId = accountIdFake,
        foodBalance = BigDecimal(100),
        mealBalance = BigDecimal(100),
        cashBalance = BigDecimal(100),
    )

    private val cardModelMock = cardDataMock.toModel()

    @Test
    fun `it should query element by accountId`(){
        val expressionSlot = slot<DynamoDBQueryExpression<CardModel>>()

        setupQueryResultWithExpressionSlot(
            dynamoDBMapperMock,
            expressionSlot,
            listOf(cardModelMock)
        )

        adapter.findCardByAccountId(
            accountId = accountIdFake
        )

        verify (exactly = 1) {
            dynamoDBMapperMock.query(
                CardModel::class.java,
                any<DynamoDBQueryExpression<CardModel>>()
            )
        }

        expressionSlot.captured.indexName shouldBe null
        expressionSlot.captured.keyConditionExpression shouldBe "#pk = :pk"
        expressionSlot.captured.filterExpression shouldBe null
        expressionSlot.captured.expressionAttributeNames shouldBe mapOf(
            "#pk" to CardModel.PK_FIELD_NAME
        )
        expressionSlot.captured.expressionAttributeValues shouldBe mapOf(
            ":pk" to AttributeValue().withS(accountIdFake)
        )
    }

    @Test
    fun `it should call dynamodb save correctly`() {
        val configSlot = slot<DynamoDBMapperConfig>()

        every { dynamoDBMapperMock.save(cardModelMock, capture(configSlot)) } returns Unit

        adapter.updateBalance(cardDataMock)

        verify(exactly = 1) { dynamoDBMapperMock.save(cardModelMock, any<DynamoDBMapperConfig>()) }

        configSlot.captured.consistentReads shouldBe DynamoDBMapperConfig.ConsistentReads.CONSISTENT
        configSlot.captured.saveBehavior shouldBe DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES

    }

    @Test
    fun `should throw InternalErrorException when save fails`() {
        val model = cardDataMock.toModel()

        every { dynamoDBMapperMock.save(model, any<DynamoDBMapperConfig>()) } throws RuntimeException("Simulated save error")

        val exception = shouldThrow<InternalErrorException> {
            adapter.updateBalance(cardDataMock)
        }

        exception.message shouldBe "Erro interno ao atualizar saldo do cartão"
    }

}