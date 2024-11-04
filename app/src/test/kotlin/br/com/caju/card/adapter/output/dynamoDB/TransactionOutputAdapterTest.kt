package br.com.caju.card.adapter.output.dynamoDB

import br.com.caju.card.adapter.output.dynamoDB.model.TransactionModel
import br.com.caju.card.adapter.output.dynamoDB.model.toModel
import br.com.caju.card.application.port.output.TransactionData
import br.com.caju.card.common.exceptions.InternalErrorException
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Test
import java.math.BigDecimal
import java.util.UUID

class TransactionOutputAdapterTest {

    private val dynamoDBMapperMock: DynamoDBMapper = mockk()
    private val adapter = TransactionOutputAdapter(mapper = dynamoDBMapperMock)

    private val transactionIdFake = UUID.randomUUID()
    private val accountIdFake = "123"

    private val transactionDataMock = TransactionData(
        id = transactionIdFake,
        account = accountIdFake,
        amount = BigDecimal(50.0),
        mcc = 5811,
        merchant = "PADARIA DO ZE               SAO PAULO BR"
    )

    private val transactionModelMock = transactionDataMock.toModel()

    @Test
    fun `should save transaction successfully`() {
        every { dynamoDBMapperMock.save(transactionModelMock) } returns Unit

        adapter.saveTransaction(transactionDataMock)

        verify(exactly = 1) { dynamoDBMapperMock.save(transactionModelMock) }
    }

    @Test
    fun `should throw InternalErrorException when save transaction fails`() {
        every { dynamoDBMapperMock.save(transactionModelMock) } throws RuntimeException("Simulated save error")

        val exception = shouldThrow<InternalErrorException> {
            adapter.saveTransaction(transactionDataMock)
        }

        exception.message shouldBe "Erro interno ao salvar transação"
    }

    @Test
    fun `should find transaction by ID successfully`() {
        val expressionSlot = slot<DynamoDBQueryExpression<TransactionModel>>()
        val dynamoResult = setupQueryResultWithExpressionSlot(
            dynamoMapperLock = dynamoDBMapperMock,
            slot = expressionSlot,
            items = listOf(transactionModelMock)
        )

        val result = adapter.findByTransactionId(transactionIdFake.toString())

        verify(exactly = 1) {
            dynamoDBMapperMock.query(TransactionModel::class.java, any<DynamoDBQueryExpression<TransactionModel>>())
        }

        expressionSlot.captured.keyConditionExpression shouldBe "#pk = :pk"
        expressionSlot.captured.expressionAttributeNames shouldBe mapOf("#pk" to TransactionModel.PK_FIELD_NAME)
        expressionSlot.captured.expressionAttributeValues shouldBe mapOf(":pk" to AttributeValue().withS(transactionIdFake.toString()))

        result shouldBe transactionDataMock
    }

    @Test
    fun `should return null if transaction by ID is not found`() {
        val expressionSlot = slot<DynamoDBQueryExpression<TransactionModel>>()
        val dynamoResult = setupQueryResultWithExpressionSlot(
            dynamoMapperLock = dynamoDBMapperMock,
            slot = expressionSlot,
            items = listOf()
        )

        val result = adapter.findByTransactionId(transactionIdFake.toString())

        result shouldBe null
    }

    @Test
    fun `should throw InternalErrorException when finding transaction by ID fails`() {
        every {
            dynamoDBMapperMock.query(TransactionModel::class.java, any<DynamoDBQueryExpression<TransactionModel>>())
        } throws RuntimeException("Simulated query error")

        val exception = shouldThrow<InternalErrorException> {
            adapter.findByTransactionId(transactionIdFake.toString())
        }

        exception.message shouldBe "Erro ao buscar transação com ID: $transactionIdFake"
    }

    @Test
    fun `should find transactions by account ID successfully`() {
        val expressionSlot = slot<DynamoDBQueryExpression<TransactionModel>>()
        val dynamoResult = setupQueryResultWithExpressionSlot(
            dynamoMapperLock = dynamoDBMapperMock,
            slot = expressionSlot,
            items = listOf(transactionModelMock)
        )

        val result = adapter.findByAccountId(accountIdFake)

        verify(exactly = 1) {
            dynamoDBMapperMock.query(TransactionModel::class.java, any<DynamoDBQueryExpression<TransactionModel>>())
        }

        expressionSlot.captured.indexName shouldBe TransactionModel.GSI_ACCOUNT_INDEX
        expressionSlot.captured.keyConditionExpression shouldBe "#accountId = :accountId"
        expressionSlot.captured.expressionAttributeNames shouldBe mapOf("#accountId" to TransactionModel.SK_FIELD_NAME)
        expressionSlot.captured.expressionAttributeValues shouldBe mapOf(":accountId" to AttributeValue().withS(accountIdFake))

        result shouldContainExactly listOf(transactionDataMock)
    }

    @Test
    fun `should throw InternalErrorException when finding transactions by account ID fails`() {
        every {
            dynamoDBMapperMock.query(TransactionModel::class.java, any<DynamoDBQueryExpression<TransactionModel>>())
        } throws RuntimeException("Simulated query error")

        val exception = shouldThrow<InternalErrorException> {
            adapter.findByAccountId(accountIdFake)
        }

        exception.message shouldBe "Erro ao buscar transações para a conta: $accountIdFake"
    }
}
