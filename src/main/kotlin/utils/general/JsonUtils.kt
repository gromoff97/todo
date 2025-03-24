package utils.general

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.reflect.KClass

object JsonUtils {
    private val objectMapper = jacksonObjectMapper().apply { enable(INDENT_OUTPUT) }

    val EMPTY_OBJECT: ObjectNode = objectMapper.createObjectNode()
    val EMPTY_ARRAY: ArrayNode = objectMapper.createArrayNode()

    private val DEFAULT_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    fun Any.toJsonNodeNow(): JsonNode = toJsonString().toJsonNodeNow()

    fun String.toJsonNodeNow(): JsonNode = objectMapper.readTree(this)

    fun String.toJsonNode() = runCatching {
        objectMapper.readTree(this)
    }

    fun <T: Any> JsonNode.decodeNowTo(clazz: KClass<T>): T = objectMapper.treeToValue(this, clazz.java)

    fun <T: Any> JsonNode.decodeTo(clazz: KClass<T>): Result<T> = runCatching {
        objectMapper.treeToValue(this, clazz.java)
    }

    fun <T: Any> JsonNode.decodeTo(typReference: TypeReference<T>): Result<T> = runCatching {
        objectMapper.treeToValue(this, typReference)
    }

    fun <T: Any> JsonNode.decodeToListOf(clazz: KClass<T>): Result<List<T>> = runCatching {
        objectMapper.convertValue(this, objectMapper.typeFactory.constructCollectionType(List::class.java, clazz.java))
    }

    private fun Any.toJsonString(): String {
        return if (this is String) {
            this
        } else {
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)
        }
    }

    fun Any.toJsonRequestBody() = toJsonString().toRequestBody(DEFAULT_MEDIA_TYPE)

    fun JsonNode.removeAllFieldsNamed(fieldName: String): JsonNode = when {
        isObject -> {
            val objectNode = objectMapper.createObjectNode()
            this.fields().forEach { (name, value) ->
                if (name != fieldName) {
                    objectNode.set<JsonNode>(name, value.removeAllFieldsNamed(fieldName))
                }
            }
            objectNode
        }
        isArray -> {
            val arrayNode = objectMapper.createArrayNode()
            this.forEach { element ->
                arrayNode.add(element.removeAllFieldsNamed(fieldName))
            }
            arrayNode
        }
        else -> deepCopy()
    }

    fun JsonNode.updateAllFieldsNamed(fieldName: String, newValue: Any?): JsonNode {
        return when {
            this.isObject -> {
                val objectNode = objectMapper.createObjectNode()
                this.fields().forEach { (name, value) ->
                    if (name == fieldName) {
                        objectNode.replaceField(name, newValue)
                    } else {
                        objectNode.set<JsonNode>(name, value.updateAllFieldsNamed(fieldName, newValue))
                    }
                }
                objectNode
            }
            this.isArray -> {
                val arrayNode = objectMapper.createArrayNode()
                this.forEach { element ->
                    arrayNode.add(element.updateAllFieldsNamed(fieldName, newValue))
                }
                arrayNode
            }
            else -> this.deepCopy()
        }
    }

    private fun ObjectNode.replaceField(fieldName: String, newValue: Any?) {
        when (newValue) {
            is String -> this.put(fieldName, newValue)
            is Int -> this.put(fieldName, newValue)
            is Long -> this.put(fieldName, newValue)
            is Double -> this.put(fieldName, newValue)
            is Boolean -> this.put(fieldName, newValue)
            null -> this.putNull(fieldName)
            is JsonNode -> this.set<JsonNode>(fieldName, newValue)
            else -> throw IllegalArgumentException("Unsupported value type: ${newValue::class.simpleName}")
        }
    }
}