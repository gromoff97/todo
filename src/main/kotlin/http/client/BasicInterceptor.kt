package http.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.TimeUnit

class BasicInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startNs = System.nanoTime()

        if (request.url.queryParameterNames.contains("asRepo")) {
            val newUrl = request.url.newBuilder().removeAllQueryParameters("asRepo").build()
            val requestWithoutAsRepoParameter = request.newBuilder().url(newUrl).build()
            return chain.proceed(requestWithoutAsRepoParameter)
        }

        logger.info {
            buildString {
                appendLine("╭─ REQUEST → ${request.method} ${request.url}")
                request.headers.forEach { (name, value) -> appendLine("│ $name: $value") }
                request.body?.let { body ->
                    val buffer = Buffer().apply { body.writeTo(this) }
                    val charset = body.contentType()?.charset(UTF_8) ?: UTF_8
                    val bodyString = buffer.readString(charset)
                    appendLine("│")
                    appendFormattedBody(bodyString)
                }
                appendLine("╰──────────────────────────────────────────────")
            }
        }

        val tookMs: Long
        val response = try {
            val resp = chain.proceed(request)
            tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
            resp
        } catch (e: Exception) {
            logger.error { "╭✖️ ERROR: ${e.message}\n╰──────────────────────────────────────────────" }
            throw e
        }

        logger.info {
            buildString {
                appendLine("╭─ RESPONSE → ${response.code} ${response.message} [${tookMs}ms]")
                response.headers.forEach { (name, value) -> appendLine("│ $name: $value") }
                response.body?.let { body ->
                    val source = body.source().apply { request(Long.MAX_VALUE) }
                    val buffer = source.buffer.clone()
                    val charset = body.contentType()?.charset(UTF_8) ?: UTF_8
                    val bodyString = buffer.readString(charset)
                    appendLine("│")
                    appendFormattedBody(bodyString)
                }
                appendLine("╰──────────────────────────────────────────────")
            }
        }

        return response
    }

    companion object {
        private fun StringBuilder.appendFormattedBody(body: String) {
            if (body.isBlank()) {
                appendLine("│ (empty body)")
                return
            }
            val formattedBody = try {
                val jsonNode = objectMapper.readTree(body)
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode)
            } catch (e: Exception) {
                body
            }
            formattedBody.lineSequence().forEach { appendLine("│ $it") }
        }

        private val objectMapper = ObjectMapper().apply {
            enable(SerializationFeature.INDENT_OUTPUT)
        }

        private val logger = KotlinLogging.logger {}
    }
}