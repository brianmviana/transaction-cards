package br.com.caju.card.common.extensions

fun <T> T.toSuccess() = Result.success(this)

fun <T> Throwable.toFailure() = Result.failure<T>(this)

inline fun <R, T> Result<T>.andThen(block: (value: T) -> Result<R>): Result<R> =
    when {
        isSuccess -> block(getOrNull()!!)
        else -> this.exceptionOrNull()!!.toFailure()
    }