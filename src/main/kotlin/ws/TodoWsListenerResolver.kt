package ws

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import ws.TodoWebSocket.Companion.createTodoWebSocket

class TodoWsListenerResolver : ParameterResolver, AfterEachCallback {
    private val webSocketsToClose = mutableMapOf<ExtensionContext, MutableList<TodoWebSocket>>()

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
        parameterContext.isAnnotated(TodoWsListener::class.java) &&
                parameterContext.parameter.type == WsAdvancedListener::class.java

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        val listener = WsAdvancedListener()
        val newWs = createTodoWebSocket(listener)

        webSocketsToClose.computeIfAbsent(extensionContext) { mutableListOf() }.add(newWs)

        return listener
    }

    override fun afterEach(context: ExtensionContext) {
        webSocketsToClose.remove(context)?.forEach(TodoWebSocket::closeGracefully)
    }
}