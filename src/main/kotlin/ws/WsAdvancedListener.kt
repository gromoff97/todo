package ws

import assertk.Assert
import assertk.all
import assertk.assertThat
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.awaitility.kotlin.withPollDelay
import org.awaitility.kotlin.withPollInterval
import utils.general.asJsonListOf
import ws.models.TodoWebSocketMessage
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration

class WsAdvancedListener : WebSocketListener(), TodoUpdatesAwait {
    private val messages = ConcurrentLinkedQueue<String>()

    override fun onMessage(webSocket: WebSocket, text: String) {
        logger.debug { "Received Message: '$text'" }
        messages.add(text)
    }

    fun clearMessages() = messages.clear()

    override fun awaitTodoMessagesList(
        pollInterval: Duration,
        delay: Duration,
        atMost: Duration,
        assertBody: Assert<List<TodoWebSocketMessage>>.() -> Unit
    ) {
        await withPollInterval pollInterval withPollDelay delay atMost atMost untilAsserted {
            assertThat(messages.toList()).asJsonListOf(TodoWebSocketMessage::class).all(assertBody)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}