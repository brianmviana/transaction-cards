package br.com.caju.card.adapter.output.dynamoDB

import br.com.caju.card.adapter.output.dynamoDB.model.MerchantModel
import br.com.caju.card.adapter.output.dynamoDB.model.toModel
import br.com.caju.card.application.port.output.MerchantData
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Test

class MerchantOutputAdapterTest {
    private val dynamoDBMapperMock: DynamoDBMapper = mockk()
    private val adapter = MechantOutputAdapter(mapper = dynamoDBMapperMock)

    private val merchantNameFake = "PADARIA DO ZE               SAO PAULO BR"

    private val merchantDataMock = MerchantData(
        name = merchantNameFake,
        mcc = 5811
    )

    private val merchantModelMock = merchantDataMock.toModel()

    @Test
    fun `should find merchant by name successfully`() {
        val expressionSlot = slot<DynamoDBQueryExpression<MerchantModel>>()
        setupQueryResultWithExpressionSlot(
            dynamoMapperLock = dynamoDBMapperMock,
            slot = expressionSlot,
            items = listOf(merchantModelMock)
        )

        val result = adapter.findMerchantByName(merchantNameFake)

        verify(exactly = 1) {
            dynamoDBMapperMock.query(MerchantModel::class.java, any<DynamoDBQueryExpression<MerchantModel>>())
        }

        expressionSlot.captured.keyConditionExpression shouldBe "#pk = :pk"
        expressionSlot.captured.expressionAttributeNames shouldBe mapOf("#pk" to MerchantModel.PK_FIELD_NAME)
        expressionSlot.captured.expressionAttributeValues shouldBe mapOf(":pk" to AttributeValue().withS(merchantNameFake))

        result shouldBe merchantDataMock
    }

    @Test
    fun `should return null if merchant by name is not found`() {
        val expressionSlot = slot<DynamoDBQueryExpression<MerchantModel>>()
        setupQueryResultWithExpressionSlot(
            dynamoMapperLock = dynamoDBMapperMock,
            slot = expressionSlot,
            items = listOf()
        )

        val result = adapter.findMerchantByName(merchantNameFake)

        result shouldBe null
    }

    @Test
    fun `should throw InternalErrorException when finding merchant by name fails`() {
        every {
            dynamoDBMapperMock.query(MerchantModel::class.java, any<DynamoDBQueryExpression<MerchantModel>>())
        } throws RuntimeException("Simulated query error")

        val exception = shouldThrow<RuntimeException> {
            adapter.findMerchantByName(merchantNameFake)
        }

        exception.message shouldBe "Simulated query error"
    }
}