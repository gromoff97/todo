package utils.general

import db.models.TodoEntity
import http.models.request.TodoCreationRequest
import http.models.request.TodoUpdateRequest
import utils.general.RandomStringUtils.randomString
import java.math.BigInteger
import kotlin.random.Random
import kotlin.random.nextULong

object RandomPojoProvider {
    private fun randomBigInteger() = BigInteger(Random.nextULong().toString())

    fun randomValidTodoCreationRequest() = TodoCreationRequest(
        id = randomBigInteger(),
        text = randomString(20),
        completed = Random.nextBoolean()
    )

    fun randomValidTodoCreationRequest(
        id: BigInteger = randomBigInteger(),
        text: String = randomString(20),
        completed: Boolean = Random.nextBoolean()
    ) = TodoCreationRequest(id, text, completed)

    fun randomValidTodoEntity() = TodoEntity(
        id = randomBigInteger(),
        text = randomString(20),
        completed = Random.nextBoolean()
    )

    fun randomValidTodoEntities(count: Int) = (0..<count).shuffled().map {
        randomValidTodoEntity().copy(id = it.toBigInteger())
    }

    fun randomUpdateRequest() = TodoUpdateRequest(
        id = randomBigInteger(),
        text = randomString(20),
        completed = Random.nextBoolean()
    )
}