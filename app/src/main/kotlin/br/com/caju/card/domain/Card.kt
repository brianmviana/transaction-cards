package br.com.caju.card.domain

import br.com.caju.card.common.exceptions.InsufficientFundsException
import java.math.BigDecimal

class Card(
    val accountId: String,
    val balance: CardBalance
) {
    class CardBalance(
        var foodBalance: BigDecimal,
        var mealBalance: BigDecimal,
        var cashBalance: BigDecimal,
    )

    fun debitByCategory(amount: BigDecimal, category: Category) =
        if (hasSufficientFunds(amount, category)) {
            updateBalance(amount, category)
        } else {
            throw InsufficientFundsException()
        }

    fun debitWithFallback(amount: BigDecimal, category: Category) =
        when (category) {
            Category.FOOD -> debitWithFallbackCategory(amount, Category.FOOD, Category.CASH)
            Category.MEAL -> debitWithFallbackCategory(amount, Category.MEAL, Category.CASH)
            Category.CASH -> debitByCategory(amount, Category.CASH)
        }

    private fun debitWithFallbackCategory(amount: BigDecimal, primaryBalance: Category, fallbackBalance: Category) {
        if (hasSufficientFunds(amount, primaryBalance)) {
            updateBalance(amount, primaryBalance)
        } else {
            debitByCategory(amount, fallbackBalance)
        }
    }

    private fun hasSufficientFunds(amount: BigDecimal, category: Category): Boolean {
        return when (category) {
            Category.FOOD -> this.balance.foodBalance >= amount
            Category.MEAL -> this.balance.mealBalance >= amount
            Category.CASH -> this.balance.cashBalance >= amount
            else -> this.balance.cashBalance >= amount
        }
    }

    private fun updateBalance(amount: BigDecimal, category: Category) {
        when (category) {
            Category.FOOD -> this.balance.foodBalance -= amount
            Category.MEAL -> this.balance.mealBalance -= amount
            Category.CASH -> this.balance.cashBalance -= amount
            else -> this.balance.cashBalance -= amount
        }
    }
}