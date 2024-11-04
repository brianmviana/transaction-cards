package br.com.caju.card.adapter.input

import br.com.caju.card.adapter.input.data.TransactionRequest
import br.com.caju.card.adapter.input.data.TransactionResponse
import br.com.caju.card.adapter.input.data.toResponse
import br.com.caju.card.adapter.input.data.toTransactionInputData
import br.com.caju.card.application.port.input.AuthorizerInputPort
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "API para gerenciar transações")
class TransactionInputAdapter(
    private val authorizerInputPort: AuthorizerInputPort,
) {

    @PostMapping("/l1")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Cria uma nova transação", description = "Cria uma nova transação no cartão apenas para a categoria requisitada")
    fun createTransactionL1(@RequestBody transactionRequest: TransactionRequest): TransactionResponse {
        return authorizerInputPort.authorizerSimple(
            inputData = transactionRequest.toTransactionInputData()
        ).toResponse()
    }

    @PostMapping("/l2")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Cria uma nova transação", description = "Cria uma nova transação no cartão com fallback da categoria cash em caso de falta de saldo na categoria requisitada")
    fun createTransactionL2(@RequestBody transactionRequest: TransactionRequest): TransactionResponse {
        return authorizerInputPort.authorizerWithFallback(
            inputData = transactionRequest.toTransactionInputData()
        ).toResponse()
    }

    @PostMapping("/l3")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Cria uma nova transação", description = "Cria uma nova transação no cartão com fallback da categoria cash dependendo do nome do comerciante")
    fun createTransactionL3(@RequestBody transactionRequest: TransactionRequest): TransactionResponse {
        return authorizerInputPort.authorizerDependingMerchant(
            inputData = transactionRequest.toTransactionInputData()
        ).toResponse()
    }

    @PostMapping("/l4")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Cria uma nova transação", description = "Cria uma nova transação no cartão com fallback da categoria cash dependendo do nome do comerciante com LOCK de transação")
    fun createTransactionL4(@RequestBody transactionRequest: TransactionRequest): TransactionResponse {
        return authorizerInputPort.authorizerWithLock(
            inputData = transactionRequest.toTransactionInputData()
        ).toResponse()
    }


}