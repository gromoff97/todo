package functional
import BaseTests
import assertk.all
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import db.TodoRepository.insertTodos
import db.models.TodoEntity
import http.api.GetTodoApi.getTodos
import http.models.response.TodoGetResponseEntry
import utils.general.RandomPojoProvider.randomValidTodoEntities
import utils.general.containsExactlyElementsOf
import utils.general.hasBody
import utils.general.hasBodyContainingListOf
import utils.general.hasStatusCode
import utils.general.hasStringContent
import utils.general.named
import utils.general.toTodoGetEntry
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigInteger.ONE
import java.math.BigInteger.TWO

@DisplayName("GET /todos")
class GetTodoTests: BaseTests() {

    @ParameterizedTest
    @MethodSource("initialEntities")
    @DisplayName("No query parameters")
    fun `get without offset and limit`(initialEntities: List<TodoEntity>) {
        val expectedEntries = insertTodos(initialEntities).map(TodoEntity::toTodoGetEntry)
        getTodos { response ->
            assertThat(response).all {
                hasStatusCode(200)
                hasBodyContainingListOf(TodoGetResponseEntry::class).containsExactlyElementsOf(expectedEntries)
            }
        }
    }

    private fun initialEntities() = listOf(listOf(), randomValidTodoEntities(5), randomValidTodoEntities(128)).map {
        it.named("entities")
    }

    @ParameterizedTest
    @MethodSource("dataForOffsetTesting")
    @DisplayName("Offset testing")
    fun `get with offset only`(initialEntities: List<TodoEntity>, offset: Int) {
        val expectedEntries = insertTodos(initialEntities).drop(offset).map(TodoEntity::toTodoGetEntry)
        getTodos(queryParams = mapOf("offset" to offset.toString())) { response ->
            assertThat(response).all {
                hasStatusCode(200)
                hasBodyContainingListOf(TodoGetResponseEntry::class).containsExactlyElementsOf(expectedEntries)
            }
        }
    }

    private fun dataForOffsetTesting() = randomValidTodoEntities(128).let { entities -> listOf(
        arguments(listOf<TodoEntity>().named("entities"), 0.named("offset")),
        arguments(entities.named("entities"), 0.named("offset")),
        arguments(entities.named("entities"), 10.named("offset")),
        arguments(entities.named("entities"), 127.named("offset")),
        arguments(entities.named("entities"), 128.named("offset")),
        arguments(entities.named("entities"), 129.named("offset")),
    )}


    @Test
    @DisplayName("Last available offset")
    fun `get with last available offset number`() {
        insertTodos(randomValidTodoEntities(128))
        getTodos(queryParams = mapOf("offset" to TWO.pow(64).minus(ONE).toString())) { response ->
            assertThat(response).all {
                hasStatusCode(200)
                hasBodyContainingListOf(TodoGetResponseEntry::class).isEmpty()
            }
        }
    }

    @ParameterizedTest
    @MethodSource("entitiesWithLimit")
    @DisplayName("Limit testing")
    fun `get with limit only`(initialEntities: List<TodoEntity>, limit: Int) {
        val expectedEntries = insertTodos(initialEntities).take(limit).map(TodoEntity::toTodoGetEntry)
        getTodos(queryParams = mapOf("limit" to limit.toString())) { response ->
            assertThat(response).all {
                hasStatusCode(200)
                hasBodyContainingListOf(TodoGetResponseEntry::class).containsExactlyElementsOf(expectedEntries)
            }
        }
    }

    private fun entitiesWithLimit() = randomValidTodoEntities(128).let { entities -> listOf(
        arguments(listOf<TodoEntity>().named("entities"), 0.named("limit")),
        arguments(listOf<TodoEntity>().named("entities"), 0.named("limit")),
        arguments(entities.named("entities"), 10.named("limit")),
        arguments(entities.named("entities"), 128.named("limit")),
        arguments(entities.named("entities"), 129.named("limit"))
    )}

    @Test
    @DisplayName("Last available limit")
    fun `get with last available limit number`() {
        val expectedEntries = insertTodos(randomValidTodoEntities(128)).map(TodoEntity::toTodoGetEntry)
        getTodos(queryParams = mapOf("limit" to TWO.pow(64).minus(ONE).toString())) { response ->
            assertThat(response).all {
                hasStatusCode(200)
                hasBodyContainingListOf(TodoGetResponseEntry::class).containsExactlyElementsOf(expectedEntries)
            }
        }
    }

    @ParameterizedTest
    @MethodSource("entitiesWithLimitAndOffset")
    @DisplayName("Limit with offset testing")
    fun `get with limit and offset`(initialEntities: List<TodoEntity>, offset: Int, limit: Int) {
        val expectedEntries = insertTodos(initialEntities).drop(offset).take(limit).map(TodoEntity::toTodoGetEntry)
        getTodos(queryParams = mapOf("offset" to offset.toString(), "limit" to limit.toString())) { response ->
            assertThat(response).all {
                hasStatusCode(200)
                hasBodyContainingListOf(TodoGetResponseEntry::class).containsExactlyElementsOf(expectedEntries)
            }
        }
    }

    private fun entitiesWithLimitAndOffset() = randomValidTodoEntities(128).let { entities -> listOf(
        arguments(listOf<TodoEntity>().named("entities"), 0.named("offset"), 0.named("limit")),
        arguments(entities.named("entities"), 1.named("offset"), 5.named("limit")),
        arguments(entities.named("entities"), 55.named("offset"), 5.named("limit")),
        arguments(entities.named("entities"), 70.named("offset"), 5.named("limit")),
        arguments(entities.named("entities"), 71.named("offset"), 5.named("limit")),
        arguments(entities.named("entities"), 127.named("offset"), 5.named("limit")),
        arguments(entities.named("entities"), 128.named("offset"), 5.named("limit")),
        arguments(entities.named("entities"), 129.named("offset"), 5.named("limit")),
    )}

    @ParameterizedTest
    @MethodSource("invalidQueryParams")
    @DisplayName("Invalid query parameters")
    fun `get with invalid query parameters`(queryParameters: Map<String, String?>) {
        getTodos(queryParams = queryParameters) { response ->
            assertThat(response).all {
                hasStatusCode(400)
                hasBody().isNotNull().hasStringContent().isEqualTo("Invalid query string")
            }
        }
    }

    private fun invalidQueryParams() = listOf(
        mapOf("offset" to null).named(),
        mapOf("offset" to " ").named(),
        mapOf("offset" to "a").named(),
        mapOf("offset" to "!#@").named(),
        mapOf("offset" to "-0").named(),
        mapOf("offset" to "-1").named(),
        mapOf("offset" to TWO.pow(64).toString()).named(),

        mapOf("limit" to null).named(),
        mapOf("limit" to " ").named(),
        mapOf("limit" to "a").named(),
        mapOf("limit" to "!#@").named(),
        mapOf("limit" to "-0").named(),
        mapOf("limit" to "-1").named(),
        mapOf("limit" to TWO.pow(64).toString()).named(),

        mapOf("offset" to "-1", "limit" to "10").named(),
        mapOf("offset" to "3", "limit" to "a").named(),
    )
}