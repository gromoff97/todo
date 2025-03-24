package functional
import BaseTests
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.single
import assertk.assertions.startsWith
import db.TodoRepository.insertTodo
import http.api.PostTodoApi.postTodo
import http.models.request.TodoCreationRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import utils.general.JsonUtils.EMPTY_ARRAY
import utils.general.JsonUtils.EMPTY_OBJECT
import utils.general.JsonUtils.removeAllFieldsNamed
import utils.general.JsonUtils.toJsonNodeNow
import utils.general.JsonUtils.updateAllFieldsNamed
import utils.general.RandomPojoProvider.randomValidTodoCreationRequest
import utils.general.RandomPojoProvider.randomValidTodoEntity
import utils.general.assertTodosListInDb
import utils.general.hasBody
import utils.general.hasStatusCode
import utils.general.hasStringContent
import utils.general.named
import utils.general.toEntity
import java.math.BigInteger.ONE
import java.math.BigInteger.TWO
import java.math.BigInteger.ZERO


@DisplayName("POST /todos")
class PostTodoTests : BaseTests() {

    @ParameterizedTest
    @MethodSource("createRequestToExpectedEntity")
    @DisplayName("Happy path TODO creation")
    fun `creating single entity happy path`(creationRequest: TodoCreationRequest) {
        postTodo(creationRequest)
        val expectedEntity = creationRequest.toEntity()
        assertTodosListInDb.single().isEqualTo(expectedEntity)
    }

    private fun createRequestToExpectedEntity() = listOf(
        TodoCreationRequest(ZERO, "Simple", false),
        TodoCreationRequest(ONE, " Task with  spaces  ", true),
        TodoCreationRequest(TWO, "@#$%^&", false),
        TodoCreationRequest(TWO.pow(64).minus(ONE), "Что-то на русском", true),
    )

    @ParameterizedTest(name = "text is ''{0}''")
    @ValueSource(strings = ["", "  "])
    @DisplayName("Blank text")
    fun `creation with blank text leads to bad request error`(invalidText: String) {
        postTodo(randomValidTodoCreationRequest().copy(text = invalidText)) { response ->
            assertThat(response).hasStatusCode(400)
        }
        assertTodosListInDb.isEmpty()
    }

    @Test
    @DisplayName("Id Duplication")
    fun `duplicating id-field leads to bad request error`() {
        val initialEntity = insertTodo(randomValidTodoEntity())
        val duplicateRequest = randomValidTodoCreationRequest().copy(id = initialEntity.id)
        postTodo(duplicateRequest) { response ->
            assertThat(response).hasStatusCode(400)
        }
        assertTodosListInDb.containsExactly(initialEntity)
    }

    @Test
    @DisplayName("text duplication")
    fun `duplicating name-field leads to bad request`() {
        val initialEntity = insertTodo(randomValidTodoEntity())
        val duplicateRequest = randomValidTodoCreationRequest().copy(text = initialEntity.text)
        postTodo(duplicateRequest) { response ->
            assertThat(response).hasStatusCode(400)
        }
        assertTodosListInDb.containsExactly(initialEntity)
    }

    @Test
    @DisplayName("completed duplication")
    fun `duplicating completed-field does not lead to bad request`() {
        val initialBody = insertTodo(randomValidTodoEntity())
        val createRequest = randomValidTodoCreationRequest().copy(completed = initialBody.completed).also(::postTodo)
        assertTodosListInDb.containsExactly(initialBody, createRequest.toEntity())
    }

    @ParameterizedTest
    @MethodSource("requestsWithInvalidStructure")
    @DisplayName("Invalid body")
    fun `creating with invalid request body structure leads to bad request`(creationRequest: Any) {
        postTodo(creationRequest) { response ->
            assertThat(response).all {
                hasStatusCode(400)
                hasBody().isNotNull().hasStringContent().startsWith("Request body deserialize error:")
            }
        }
        assertTodosListInDb.isEmpty()
    }

    private fun requestsWithInvalidStructure() = randomValidTodoCreationRequest().let { validReq ->
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