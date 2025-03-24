package functional
import BaseTests
import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.single
import assertk.assertions.startsWith
import db.TodoRepository.insertTodo
import db.TodoRepository.insertTodos
import db.models.TodoEntity
import http.api.PutTodoApi.putTodo
import http.models.request.TodoUpdateRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import utils.general.TWO_POW_64
import utils.general.JsonUtils.EMPTY_ARRAY
import utils.general.JsonUtils.EMPTY_OBJECT
import utils.general.JsonUtils.removeAllFieldsNamed
import utils.general.JsonUtils.toJsonNodeNow
import utils.general.JsonUtils.updateAllFieldsNamed
import utils.general.RandomPojoProvider.randomUpdateRequest
import utils.general.RandomPojoProvider.randomValidTodoEntities
import utils.general.RandomPojoProvider.randomValidTodoEntity
import utils.general.assertTodosListInDb
import utils.general.hasBlankBody
import utils.general.hasBodyAsString
import utils.general.hasStatusCode
import utils.general.named
import utils.general.toTodoUpdateRequest
import java.math.BigInteger.ONE
import java.math.BigInteger.TWO
import java.math.BigInteger.ZERO

@DisplayName("PUT /todos/:id")
class PutTodoTests : BaseTests() {

    @ParameterizedTest(name = "{1}")
    @MethodSource("entities")
    fun `updating single entity`(initialEntity: TodoEntity, updateReq: TodoUpdateRequest, expectedEntity: TodoEntity) {
        insertTodo(initialEntity)
        putTodo(initialEntity.id.toString(), updateReq)
        assertTodosListInDb.single().isEqualTo(expectedEntity)
    }

    private fun entities() = listOf(
        arguments(
            TodoEntity(ZERO, "SomeText", true),
            TodoUpdateRequest(ZERO, "SomeText2", true).named("Update text"),
            TodoEntity(ZERO, "SomeText2", true)
        ),
        arguments(
            TodoEntity(ONE, "SomeText", true),
            TodoUpdateRequest(ONE, "SomeText", false).named("Update completed"),
            TodoEntity(ONE, "SomeText", false)
        )
    )

    @Test
    @DisplayName("Updating entity twice")
    fun `updating single entity twice`() {
        val initialEntity = insertTodo(randomValidTodoEntity())
        putTodo(initialEntity.id.toString(), initialEntity.toTodoUpdateRequest().copy(text = "first update"))
        putTodo(initialEntity.id.toString(), initialEntity.toTodoUpdateRequest().copy(text = "second update"))
        assertTodosListInDb.single().isEqualTo(initialEntity.copy(text = "second update"))
    }

    @Test
    @DisplayName("Updating with existing id")
    fun `duplicating id leads to bad request error`() {
        val (initialEntity, entityToUpdate) = insertTodos(randomValidTodoEntities(2))
        val updateRequest = randomUpdateRequest().copy(id = initialEntity.id)

        putTodo(entityToUpdate.id.toString(), updateRequest) { response ->
            assertThat(response).hasStatusCode(400)
        }

        assertTodosListInDb.single().isEqualTo(initialEntity)
    }

    @Test
    @DisplayName("Updating with existing text")
    fun `duplicating text leads to bad request error`() {
        val (initialEntity, entityToUpdate) = insertTodos(randomValidTodoEntities(2))
        val updateRequest = randomUpdateRequest().copy(text = initialEntity.text)

        putTodo(entityToUpdate.id.toString(), updateRequest) { response ->
            assertThat(response).hasStatusCode(400)
        }

        assertTodosListInDb.single().isEqualTo(initialEntity)
    }

    @Test
    @DisplayName("Updating nonexistent entity")
    fun `updating by id of nonexistent id leads to not found error`() {
        putTodo(123.toString(), randomUpdateRequest()) { response ->
            assertThat(response).all {
                hasStatusCode(404)
                hasBlankBody()
            }
        }
        assertTodosListInDb.isEmpty()
    }

    @ParameterizedTest
    @ValueSource(strings = ["a", " ", "!@#", "-3", TWO_POW_64])
    @DisplayName("Invalid id")
    fun `updating by invalid id leads to bad request error`(invalidId: String) {
        putTodo(invalidId, randomUpdateRequest()) { response ->
            assertThat(response).all {
                hasStatusCode(400)
                hasBodyAsString().all {
                    contains("invalid", ignoreCase = true)
                    contains("path", ignoreCase = true)
                }
            }
        }
        assertTodosListInDb.isEmpty()
    }

    @ParameterizedTest
    @MethodSource("requestsWithInvalidStructure")
    @DisplayName("Invalid body")
    fun `updating with invalid body leads to bad request error`(updateRequest: Any) {
        val anyEntity = insertTodo(randomValidTodoEntity())
        putTodo(anyEntity.id.toString(), updateRequest) { response ->
            assertThat(response).all {
                hasStatusCode(400)
                hasBodyAsString().startsWith("Request body deserialize error:")
            }
        }
        assertTodosListInDb.single().isEqualTo(anyEntity)
    }

    private fun requestsWithInvalidStructure() = randomUpdateRequest().let { validReq ->
        listOf(
            "".named("empty string"),
            "just a string".named("silly string"),
            "[]".named("empty array"),
            "{}".named("empty object"),

            validReq.toJsonNodeNow().removeAllFieldsNamed("id").named("no id fields"),
            validReq.toJsonNodeNow().removeAllFieldsNamed("text").named("no text field"),
            validReq.toJsonNodeNow().removeAllFieldsNamed("completed").named("no completed field"),

            validReq.copy(id = null).named("id is null"),
            validReq.copy(text = null).named("text is null"),
            validReq.copy(completed = null).named("completed is null"),

            validReq.toJsonNodeNow().updateAllFieldsNamed("id", "stringwth").named("id is string"),
            validReq.toJsonNodeNow().updateAllFieldsNamed("id", 23.54).named("id is double"),
            validReq.toJsonNodeNow().updateAllFieldsNamed("id", true).named("is is a boolean"),
            validReq.toJsonNodeNow().updateAllFieldsNamed("id", EMPTY_OBJECT).named("id is empty object"),
            validReq.toJsonNodeNow().updateAllFieldsNamed("id", EMPTY_ARRAY).named("is is empty array"),

            validReq.toJsonNodeNow().updateAllFieldsNamed("text", 123).named("text is int"),
            validReq.toJsonNodeNow().updateAllFieldsNamed("text", 23.54).named("text is double"),
            validReq.toJsonNodeNow().updateAllFieldsNamed("text", true).named("text is boolean"),
            validReq.toJsonNodeNow().updateAllFieldsNamed("text", EMPTY_OBJECT).named("text is an empty object"),
            validReq.toJsonNodeNow().updateAllFieldsNamed("text", EMPTY_ARRAY).named("text is an empty array"),

            validReq.toJsonNodeNow().updateAllFieldsNamed("completed", 123).named("completed is int"),
            validReq.toJsonNodeNow().updateAllFieldsNamed("completed", 23.54).named("completed is double"),
            validReq.toJsonNodeNow().updateAllFieldsNamed("completed", "stringwth").named("completed is string"),
            validReq.toJsonNodeNow().updateAllFieldsNamed("completed", EMPTY_OBJECT).named("completed is empty object"),
            validReq.toJsonNodeNow().updateAllFieldsNamed("completed", EMPTY_ARRAY).named("completed is empty array"),

            validReq.copy(id = ONE.negate()).named("Negative id"),
            validReq.copy(id = TWO.pow(64)).named("Too big id"),
        )
    }

}