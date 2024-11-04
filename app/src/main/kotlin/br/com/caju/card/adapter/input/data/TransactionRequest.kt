package br.com.caju.card.adapter.input.data

import br.com.caju.card.application.port.input.TransactionInputData
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

data class TransactionRequest(
    @Schema(description = "Um identificador para a conta", example = "123")
    val accountId: String,

    @Schema(description = "O valor a ser debitado de um saldo", example = "100.00")
    val totalAmount: BigDecimal,

    @Schema(description = """Um código numérico de 4 dígitos que classifica os estabelecimentos
                            comerciais de acordo com o tipo de produto vendido ou serviço prestado""", example = "5811")
    val mcc: Int,

    @Schema(description = "O nome do estabelecimento", example = "PADARIA DO ZE SAO PAULO BR")
    val merchant: String
)

fun TransactionRequest.toTransactionInputData(): TransactionInputData = TransactionInputData(
    account = accountId,
    amount = totalAmount,
    mcc = mcc,
    merchant = merchant
)