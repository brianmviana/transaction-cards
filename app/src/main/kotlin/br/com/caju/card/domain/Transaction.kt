package br.com.caju.card.domain

import java.math.BigDecimal
import java.util.UUID

data class Transaction(
    val id: UUID = UUID.randomUUID(),
    val account: String,
    val amount: BigDecimal,
    val mcc: Int,
    val merchant: String,
) {

    fun getCategoryByMcc(): Category = when(this.mcc) {
        5411, 5412 -> Category.FOOD
        5811, 5812 -> Category.MEAL
        else -> Category.CASH
    }
}