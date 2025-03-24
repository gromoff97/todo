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
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class WsAdvancedListener : WebSocketListener() {
    private val messages = ConcurrentLinkedQueue<String>()

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        logger.debug { "Received Message: '$text'" }
        messages.add(text)
    }

    private fun assertMessagesList(assertBody: Assert<List<String>>.() -> Unit) {
        assertThat(messages.toList()).all(assertBody)
    }

    fun awaitMessagesList(
        pollInterval: Duration = 200.milliseconds,
        delay: Duration = 1.seconds,
        atMost: Duration = 3.seconds,
        assertBody: Assert<List<String>>.() -> Unit
    ) {
        await withPollInterval pollInterval withPollDelay delay atMost atMost untilAsserted {
            assertMessagesList(assertBody)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}