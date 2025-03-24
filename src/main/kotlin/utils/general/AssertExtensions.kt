package utils.general

import assertk.Assert
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.extracting
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isZero
import assertk.assertions.prop
import assertk.assertions.support.expected
import assertk.fail
import com.fasterxml.jackson.databind.JsonNode
import db.TodoRepository.selectAllTodos
import db.models.TodoEntity
import okhttp3.Response
import okhttp3.ResponseBody
import utils.general.JsonUtils.decodeNowTo
import utils.general.JsonUtils.decodeTo
import utils.general.JsonUtils.decodeToListOf
import utils.general.JsonUtils.toJsonNode
import utils.general.JsonUtils.toJsonNodeNow
import kotlin.reflect.KClass

val successfulStatusCodesRange = (200..299)

val assertTodosListInDb: Assert<List<TodoEntity>>
    get() = assertThat(selectAllTodos(), "TODOs in database")

fun Assert<Response>.hasStatusCode(expectedStatusCode: Int) =
    prop("Response status code") { it.code }.isEqualTo(expectedStatusCode)

fun Assert<Response>.hasUnsuccessfulStatusCode() =
    prop("Response status code") { it.code }.isNotInRange(successfulStatusCodesRange)

fun Assert<Int>.isNotInRange(range: IntRange) = given {
    if (!range.contains(it)) return
    expected("to not be in following range: $range")
}

fun Assert<Response>.hasBody() =
    prop("Response body") { it.body }

fun Assert<Response>.hasBodyAsString() =
    hasBody().isNotNull().hasStringContent()

fun Assert<ResponseBody>.hasStringContent() =
    prop("Response body content length") { it.string() }

fun Assert<ResponseBody>.hasZeroContent() =
    prop("Response body content length", ResponseBody::contentLength).isZero()

fun Assert<Response>.hasBlankBody() =
    prop("ResponseBody") { it.body }.given { actualBody ->
        if (actualBody != null && actualBody.contentLength() > 0) {
            expected("to be null or have zero content length")
        }
    }

private fun Assert<Response>.nonBlankBody() = transform("Response") {
    val body = it.body ?: expected("to have non null body")
    if (body.contentLength() <= 0L) {
        expected("to have body with known content and length greater than zero")
    }
    body
}

fun Assert<ResponseBody>.bodyStr(): Assert<String> =
    prop("ResponseBody", ResponseBody::string)

fun Assert<String>.jsonContent(): Assert<JsonNode> = transform("String") {
    val conversionResult = it.toJsonNode()
    if (conversionResult.isFailure) {
        fail(
            message = "Expected $name to represent JsonNode",
            cause = conversionResult.exceptionOrNull()
        )
    }
    conversionResult.getOrThrow()
}

fun <T : Any> Assert<JsonNode>.represents(clazz: KClass<T>): Assert<T> =
    transform("JsonNode") {
        val decodeResult = it.decodeTo(clazz)
        if (decodeResult.isFailure) {
            fail(
                message = "Expected $name to represent ${clazz.simpleName} as JSON",
                cause = decodeResult.exceptionOrNull()
            )
        }
        decodeResult.getOrThrow()
    }

fun <T : Any> Assert<JsonNode>.representsJsonArrayOf(clazz: KClass<T>): Assert<List<T>> =
    transform("JsonNode") {
        val decodeResult = it.decodeToListOf(clazz)
        if (decodeResult.isFailure) {
            fail(
                message = "Expected $name to represent List of ${clazz.simpleName} as JSON",
                cause = decodeResult.exceptionOrNull()
            )
        }
        decodeResult.getOrThrow()
    }

fun <T : Any> Assert<List<String>>.asJsonListOf(clazz: KClass<T>): Assert<List<T>> =
    extracting { it.toJsonNodeNow().decodeNowTo(clazz) }

fun <T : Any> Assert<Response>.hasBodyContainingListOf(kClass: KClass<T>) =
    hasBody().isNotNull().bodyStr().jsonContent().representsJsonArrayOf(kClass)

inline fun <reified T> Assert<List<T>>.containsExactlyInAnyOrderElementsOf(expected: List<T>) =
    containsExactlyInAnyOrder(*expected.toTypedArray())

inline fun <reified T> Assert<List<T>>.containsExactlyElementsOf(expected: List<T>) =
    containsExactly(*expected.toTypedArray())
