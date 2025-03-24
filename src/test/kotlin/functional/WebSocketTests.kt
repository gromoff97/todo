package functional
import BaseTests
import assertk.assertThat
import http.api.DeleteTodoApi.deleteTodo
import http.api.GetTodoApi.getTodos
import http.api.PostTodoApi.postTodo
import http.api.PutTodoApi.putTodo
import http.models.AuthWay.Companion.adminBasicAuth
import http.models.AuthWay.Companion.noAuth
import http.models.request.TodoUpdateRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import utils.general.RandomPojoProvider.randomValidTodoCreationRequest
import utils.general.asJsonListOf
import utils.general.containsExactlyElementsOf
import utils.general.hasUnsuccessfulStatusCode
import utils.general.invoke
import utils.general.toWebSocketMessage
import ws.TodoWsListener
import ws.TodoWsListenerResolver
import ws.WsAdvancedListener
import ws.models.TodoWebSocketMessage

@ExtendWith(TodoWsListenerResolver::class)
@DisplayName("WebSocket testing")
class WebSocketTests : BaseTests() {

    @Test
    @DisplayName("Websocket end-to-end test")
    fun `websocket is related to todo creation only`(@TodoWsListener wsListener: WsAdvancedListener) {
        val firstCreationRequest = "Successful API calls provocation" {
            randomValidTodoCreationRequest().also { req ->
                postTodo(req)
                putTodo(req.id.toString(), TodoUpdateRequest(req.id, "Hello from PUT", true))
                getTodos()
                deleteTodo(req.id.toString(), authWay = adminBasicAuth)
            }
        }

        "Unsuccessful API calls provocation" {
            postTodo(firstCreationRequest.copy(id = null)) { assertThat(it).hasUnsuccessfulStatusCode() }
            putTodo("abc", TodoUpdateRequest()) { assertThat(it).hasUnsuccessfulStatusCode() }
            getTodos(queryParams = mapOf("offset" to "abc")) { assertThat(it).hasUnsuccessfulStatusCode() }
            deleteTodo("123", authWay = noAuth()) { assertThat(it).hasUnsuccessfulStatusCode() }
        }

        val secondCreationRequest = "Create TODO again" { randomValidTodoCreationRequest().also(::postTodo) }

        "Make sure there are only two websocket's messages related to TODO creation" {
            val expectedMessages = listOf(firstCreationRequest, secondCreationRequest).map { it.toWebSocketMessage() }
            wsListener.awaitMessagesList {
                asJsonListOf(TodoWebSocketMessage::class).containsExactlyElementsOf(expectedMessages)
            }
        }
    }
}
