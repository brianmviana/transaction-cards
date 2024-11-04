package br.com.caju.card.adapter.input

import br.com.caju.card.application.port.output.CardOutputPort
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cards")
@Tag(name = "Transactions", description = "API para gerenciar transações")
class CartInputAdapter(
    private val cardOutputPort: CardOutputPort
) {

    @GetMapping("{accountId}")
    @Operation(summary = "Busca os saldos de um cartão", description = "Busca os saldos de um cartão pelo id da conta")
    fun getCardBalances(
        @PathVariable("accountId", required = true) accountId : String
    ) = cardOutputPort.findCardByAccountId(accountId = accountId)
}