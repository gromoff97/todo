package ws

import assertk.Assert
import ws.models.TodoWebSocketMessage
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

interface TodoUpdatesAwait {
    fun awaitTodoMessagesList(
        pollInterval: Duration = 200.milliseconds,
        delay: Duration = Duration.ZERO,
        atMost: Duration = 3.seconds,
        assertBody: Assert<List<TodoWebSocketMessage>>.() -> Unit
    )
}