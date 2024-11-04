package br.com.caju.card.common.extensions

inline fun Boolean.onTrue(block: () -> Unit): Boolean {
    if (this) block()
    return this
}

inline fun Boolean.onFalse(block: () -> Unit): Boolean {
    if (!this) block()
    return this
}