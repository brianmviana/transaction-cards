package br.com.caju.card.application.port.output

import br.com.caju.card.domain.Card
import java.math.BigDecimal

interface CardOutputPort {
    fun findCardByAccountId(accountId: String): CardData
    fun updateBalance(cardData: CardData)
}

class CardData(
    val accountId: String,
    val foodBalance: BigDecimal,
    val mealBalance: BigDecimal,
    val cashBalance: BigDecimal,
)

fun Card.toApplicationModel(): CardData = CardData(
    accountId = this.accountId,
    foodBalance = this.balance.foodBalance,
    mealBalance = this.balance.mealBalance,
    cashBalance = this.balance.cashBalance,
)

fun CardData.toDomain(): Card = Card(
    accountId = this.accountId,
    balance = Card.CardBalance(
        foodBalance = this.foodBalance,
        mealBalance = this.mealBalance,
        cashBalance = this.cashBalance,
    ),
)