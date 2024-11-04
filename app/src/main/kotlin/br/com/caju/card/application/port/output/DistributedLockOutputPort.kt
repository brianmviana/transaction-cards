package br.com.caju.card.application.port.output

import br.com.caju.card.common.exceptions.UnableToAcquireLockException
import br.com.caju.card.common.extensions.onFalse
import br.com.caju.card.common.extensions.onTrue
import java.util.concurrent.TimeUnit

interface DistributedLockOutputPort {
    fun acquireLock(accountId: String, timeout: Long = 1000, timeUnit: TimeUnit = TimeUnit.MILLISECONDS): Boolean
    fun releaseLock(accountId: String): Boolean
}

inline fun DistributedLockOutputPort.runLocking(accountId: String, timeout: Long, crossinline block: () -> Unit) {
    acquireLock(
        accountId = accountId,
        timeout = timeout,
    ).onTrue {
        runCatching {
            block()
        }.also { result ->
            releaseLock(accountId)
            result.getOrThrow()
        }
    }.onFalse {
        throw UnableToAcquireLockException(accountId = accountId)
    }
}