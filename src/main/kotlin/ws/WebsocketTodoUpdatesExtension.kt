package ws

import http.models.Segments
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import utils.http.providers.OkHttpRequestProvider.noAuthWebSocketRequest

class WebsocketTodoUpdatesExtension : ParameterResolver, BeforeAllCallback, AfterEachCallback, AfterAllCallback {
    private lateinit var listener: WsAdvancedListener
    private lateinit var webSocket: WebSocket

    override fun beforeAll(context: ExtensionContext?) {
        listener = WsAdvancedListener()
        webSocket = defaultHttpClient.newWebSocket(defaultWsRequest, listener)
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext) = with(parameterContext) {
        isAnnotated(WebSocketTodoUpdatesAwait::class.java) && parameter.type == TodoUpdatesAwait::class.java
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): TodoUpdatesAwait {
        return listener
    }

    override fun afterEach(context: ExtensionContext) {
        listener.clearMessages()
    }

    override fun afterAll(context: ExtensionContext?) {
        webSocket.closeGracefully()
    }

    companion object {
        val defaultHttpClient = OkHttpClient()
        val defaultWsRequest = noAuthWebSocketRequest(Segments.ws)

        fun WebSocket.closeGracefully(message: String = "Closing Successfully") = close(1000, message)
    }
}