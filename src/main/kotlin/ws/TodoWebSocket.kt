package ws

import http.models.Segments.ws
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import utils.http.providers.OkHttpRequestProvider.noAuthWebSocketRequest

class TodoWebSocket private constructor(private val client: OkHttpClient, private val socket: WebSocket) {

    fun closeGracefully() {
        socket.close(1000, "Closing ${this.javaClass.simpleName}")
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
    }

    companion object {
        fun createTodoWebSocket(
            listener: WebSocketListener,
            client: OkHttpClient = OkHttpClient(),
            request: Request = noAuthWebSocketRequest(ws)
        ): TodoWebSocket = TodoWebSocket(client, client.newWebSocket(request, listener))
    }
}