package br.com.caju.card.adapter.output.dynamoDB

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk

inline fun <reified T> setupQueryResultWithExpressionSlot(
    dynamoMapperLock: DynamoDBMapper,
    slot: CapturingSlot<DynamoDBQueryExpression<T>>,
    items: List<T>
): PaginatedQueryList<T> {
    val dynamoResult: PaginatedQueryList<T> = mockk()

    every { dynamoResult.size } returns items.size
    every { dynamoResult.iterator() } returns items.toMutableList().iterator()
    every { dynamoResult.stream() } returns items.stream()
    every { dynamoResult.isEmpty() } returns items.isEmpty()
    every { dynamoMapperLock.query(T::class.java, capture(slot)) } returns dynamoResult

    items.forEachIndexed { i, el ->
        every { dynamoResult[i] } returns el
    }

    return dynamoResult
}