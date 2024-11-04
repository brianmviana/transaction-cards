package br.com.caju.card.application.usecase

import br.com.caju.card.application.port.input.TransactionInputData
import br.com.caju.card.application.port.input.TransactionResult
import br.com.caju.card.application.port.output.CardOutputPort
import br.com.caju.card.application.port.output.DistributedLockOutputPort
import br.com.caju.card.application.port.output.MerchantData
import br.com.caju.card.application.port.output.MerchantOutputPort
import br.com.caju.card.application.port.output.TransactionOutputPort
import br.com.caju.card.application.port.output.toApplicationModel
import br.com.caju.card.domain.Card
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthorizerUseCaseTest {

    private val cardOutputPort: CardOutputPort = mockk()
    private val transactionOutputPort: TransactionOutputPort = mockk()
    private val merchantOutputPort: MerchantOutputPort = mockk()
    private val lockOutputPort: DistributedLockOutputPort = mockk()
    private lateinit var authorizerUseCase: AuthorizerUseCase

    private val accountIdFake = "123"
    private val merchantNameFake = "PADARIA DO ZE               SAO PAULO BR"
    private val transactionInputData = TransactionInputData(
        account = accountIdFake,
        merchant = merchantNameFake,
        mcc = 5811,
        amount = BigDecimal(50),
    )

    private fun cardMock(balance: BigDecimal = BigDecimal(100)): Card = Card(
        accountId = accountIdFake,
        balance = Card.CardBalance(
            foodBalance = balance,
            mealBalance = balance,
            cashBalance = balance,
        )
    )

    val merchantDataMock = MerchantData(
        name = merchantNameFake,
        mcc = 1234
    )

    @BeforeEach
    fun setUp() {
        authorizerUseCase = AuthorizerUseCase(
            cardOutputPort = cardOutputPort,
            transactionOutputPort = transactionOutputPort,
            merchantOutputPort = merchantOutputPort,
            lockOutputPort = lockOutputPort
        )
    }

    @Test
    fun `should approve transaction in authorizerSimple when debit is successful`() {
        every { cardOutputPort.findCardByAccountId(accountIdFake) } returns cardMock().toApplicationModel()
        every { transactionOutputPort.saveTransaction(any()) } returns Unit
        every { cardOutputPort.updateBalance(any()) } returns Unit

        val result = authorizerUseCase.authorizerSimple(transactionInputData)

        result shouldBe TransactionResult.APPROVED
    }

    @Test
    fun `should reject transaction in authorizerSimple when debit fails due to insufficient funds`() {
        every { cardOutputPort.findCardByAccountId(accountIdFake) } returns cardMock(BigDecimal.TEN).toApplicationModel()
        every { transactionOutputPort.saveTransaction(any()) } returns Unit
        every { cardOutputPort.updateBalance(any()) } returns Unit

        val result = authorizerUseCase.authorizerSimple(transactionInputData)

        result shouldBe TransactionResult.REJECTED
    }

    @Test
    fun `should approve transaction in authorizerWithFallback when debit with fallback is successful`() {
        every { cardOutputPort.findCardByAccountId(accountIdFake) } returns cardMock().toApplicationModel()
        every { transactionOutputPort.saveTransaction(any()) } returns Unit
        every { cardOutputPort.updateBalance(any()) } returns Unit

        val result = authorizerUseCase.authorizerWithFallback(transactionInputData)

        result shouldBe TransactionResult.APPROVED
    }

    @Test
    fun `should reject transaction in authorizerWithFallback when debit with fallback fails due to insufficient funds`() {
        every { cardOutputPort.findCardByAccountId(accountIdFake) } returns cardMock(BigDecimal.TEN).toApplicationModel()
        every { transactionOutputPort.saveTransaction(any()) } returns Unit
        every { cardOutputPort.updateBalance(any()) } returns Unit

        val result = authorizerUseCase.authorizerWithFallback(transactionInputData)

        result shouldBe TransactionResult.REJECTED
    }

    @Test
    fun `should approve transaction in authorizerDependingMerchant when debit depending merchant is successful`() {


        every { merchantOutputPort.findMerchantByName(merchantNameFake) } returns merchantDataMock
        every { cardOutputPort.findCardByAccountId(accountIdFake) } returns cardMock().toApplicationModel()
        every { transactionOutputPort.saveTransaction(any()) } returns Unit
        every { cardOutputPort.updateBalance(any()) } returns Unit

        val result = authorizerUseCase.authorizerDependingMerchant(transactionInputData)

        result shouldBe TransactionResult.APPROVED
    }

    @Test
    fun `should reject transaction in authorizerDependingMerchant when merchant not found`() {
        every { merchantOutputPort.findMerchantByName(merchantNameFake) } returns null
        every { cardOutputPort.findCardByAccountId(accountIdFake) } returns cardMock(BigDecimal.TEN).toApplicationModel()
        every { transactionOutputPort.saveTransaction(any()) } returns Unit
        every { cardOutputPort.updateBalance(any()) } returns Unit

        val result = authorizerUseCase.authorizerDependingMerchant(transactionInputData)

        result shouldBe TransactionResult.REJECTED
    }

    @Test
    fun `should approve transaction in authorizerWithLock when lock is acquired and debit is successful`() {
        every { lockOutputPort.acquireLock(accountIdFake) } returns true
        every { lockOutputPort.releaseLock(accountIdFake) } returns true
        every { merchantOutputPort.findMerchantByName(merchantNameFake) } returns merchantDataMock
        every { cardOutputPort.findCardByAccountId(accountIdFake) } returns cardMock().toApplicationModel()
        every { transactionOutputPort.saveTransaction(any()) } returns Unit
        every { cardOutputPort.updateBalance(any()) } returns Unit

        val result = authorizerUseCase.authorizerWithLock(transactionInputData)

        result shouldBe TransactionResult.APPROVED

        verify(exactly = 1) { lockOutputPort.acquireLock(accountIdFake) }
        verify(exactly = 1) { lockOutputPort.releaseLock(accountIdFake) }
    }

    @Test
    fun `should throw UnableToAcquireLockException when lock acquisition fails`() {
        every { lockOutputPort.acquireLock(accountIdFake) } returns false
        every { lockOutputPort.releaseLock(accountIdFake) } returns false

        val result = authorizerUseCase.authorizerWithLock(transactionInputData)

        result shouldBe TransactionResult.PROCESSING_ERROR
    }
}
