package br.com.caju.card.adapter.output.redis

import br.com.caju.card.application.port.output.DistributedLockOutputPort
import java.util.concurrent.TimeUnit
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class DistributedLockOutputAdapter(
    private val redisTemplate: RedisTemplate<String, Any>
): DistributedLockOutputPort {
    override fun acquireLock(accountId: String, timeout: Long, timeUnit: TimeUnit): Boolean =
        redisTemplate
            .opsForValue()
            .setIfAbsent("lockAccount:$accountId", true, timeout, timeUnit) ?: false


    override fun releaseLock(accountId: String): Boolean = runCatching {
        redisTemplate.delete("lockAccount:${accountId}")
    }.isSuccess
}