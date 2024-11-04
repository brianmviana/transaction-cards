package br.com.caju.card.application.usecase

import br.com.caju.card.application.port.input.AuthorizerInputPort
import br.com.caju.card.application.port.input.TransactionInputData
import br.com.caju.card.application.port.input.TransactionResult
import br.com.caju.card.application.port.input.toDomain
import br.com.caju.card.application.port.output.CardOutputPort
import br.com.caju.card.application.port.output.DistributedLockOutputPort
import br.com.caju.card.application.port.output.MerchantOutputPort
import br.com.caju.card.application.port.output.TransactionOutputPort
import br.com.caju.card.application.port.output.toApplicationModel
import br.com.caju.card.application.port.output.toDomain
import br.com.caju.card.common.exceptions.InsufficientFundsException
import br.com.caju.card.common.exceptions.UnableToAcquireLockException
import br.com.caju.card.common.extensions.andThen
import br.com.caju.card.common.extensions.onFalse
import br.com.caju.card.domain.Card
import br.com.caju.card.domain.Transaction
import org.springframework.stereotype.Service

@Service
class AuthorizerUseCase(
    private val cardOutputPort: CardOutputPort,
    private val transactionOutputPort: TransactionOutputPort,
    private val merchantOutputPort: MerchantOutputPort,
    private val lockOutputPort: DistributedLockOutputPort
): AuthorizerInputPort {

    override fun authorizerSimple(inputData: TransactionInputData): TransactionResult =
        createContext(inputData = inputData)
            .andThen(::debit)
            .andThen(::persistTransaction)
            .map {
                TransactionResult.APPROVED
            }.getOrElse {
                TransactionResult.REJECTED
            }

    override fun authorizerWithFallback(inputData: TransactionInputData): TransactionResult =
        createContext(inputData = inputData)
            .andThen(::debitWithFallback)
            .andThen(::persistTransaction)
            .map {
                TransactionResult.APPROVED
            }.getOrElse {
                handleFailure(exception = it)
            }

    override fun authorizerDependingMerchant(inputData: TransactionInputData): TransactionResult =
        createContext(inputData = inputData)
            .andThen(::debitDependingMerchant)
            .andThen(::persistTransaction)
            .map {
                TransactionResult.APPROVED
            }.getOrElse {
                handleFailure(exception = it)
            }

    override fun authorizerWithLock(inputData: TransactionInputData): TransactionResult =
        lock(inputData)
            .andThen {
                createContext(it)
                    .andThen(::debitDependingMerchant)
                    .andThen(::persistTransaction)
            }.map {
                unlock(accountId = inputData.account)
                TransactionResult.APPROVED
            }.getOrElse {
                unlock(accountId = inputData.account)
                handleFailure(exception = it)
            }

    private fun createContext(inputData: TransactionInputData) = runCatching {
        AuthorizerContext(
            transaction = inputData.toDomain(),
            card = cardOutputPort.findCardByAccountId(
                accountId = inputData.account
            ).toDomain()
        )
    }

    private fun debit(authorizerContext: AuthorizerContext) = runCatching {
        with(authorizerContext) {
            card.debitByCategory(
                amount = transaction.amount,
                category = transaction.getCategoryByMcc()
            )
        }
    }.map { authorizerContext }

    private fun debitWithFallback(authorizerContext: AuthorizerContext) = runCatching {
        with(authorizerContext) {
            card.debitWithFallback(
                amount = transaction.amount,
                category = transaction.getCategoryByMcc()
            )
        }
    }.map { authorizerContext }

    private fun debitDependingMerchant(authorizerContext: AuthorizerContext) = runCatching {
        with(authorizerContext) {
            val merchant = merchantOutputPort.findMerchantByName(transaction.merchant)
            val updatedTransaction = merchant?.let {
                transaction.copy(mcc = it.mcc)
            } ?: transaction

            card.debitWithFallback(
                amount = transaction.amount,
                category = updatedTransaction.getCategoryByMcc()
            )
            updatedTransaction
        }
    }.map { updatedTransaction ->
        authorizerContext.copy(
            transaction = updatedTransaction
        )
    }

    private fun persistTransaction(authorizerContext: AuthorizerContext) = runCatching {
        with(authorizerContext) {
            transactionOutputPort.saveTransaction(
                transactionData = transaction.toApplicationModel()
            )
            cardOutputPort.updateBalance(
                cardData = card.toApplicationModel()
            )
        }
    }.map {
        authorizerContext
    }

    private fun handleFailure(exception: Throwable): TransactionResult =
        when(exception) {
            is InsufficientFundsException -> TransactionResult.REJECTED
            else -> TransactionResult.PROCESSING_ERROR
        }

    private fun lock(inputData: TransactionInputData) = runCatching {
        lockOutputPort.acquireLock(accountId = inputData.account)
            .onFalse {
                throw UnableToAcquireLockException(accountId = inputData.account)
            }
    }.map {
        inputData
    }
    private fun unlock(accountId: String) =
        lockOutputPort.releaseLock(accountId = accountId)

}

data class AuthorizerContext(
    val transaction: Transaction,
    val card: Card,
)
