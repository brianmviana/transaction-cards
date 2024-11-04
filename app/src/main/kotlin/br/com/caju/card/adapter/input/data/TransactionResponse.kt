package br.com.caju.card.adapter.input.data

import br.com.caju.card.application.port.input.TransactionResult
import io.swagger.v3.oas.annotations.media.Schema

class TransactionResponse(
    @Schema(description = "Codigo do resultado da transação", example = "00 || 51 || 07")
    val code: String
)

fun TransactionResult.toResponse() = TransactionResponse(
    code = this.code
)