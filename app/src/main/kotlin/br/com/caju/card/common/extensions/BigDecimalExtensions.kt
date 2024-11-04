package br.com.caju.card.common.extensions

import java.math.BigDecimal

fun BigDecimal?.orZero(): BigDecimal = this ?: BigDecimal.ZERO