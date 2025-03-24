package http.api

import assertk.all
import assertk.assertThat
import http.client.TodoHttpClient.executeRequest
import http.models.AuthWay
import http.models.Segments.todos
import http.models.response.TodoGetResponseEntry
import io.qameta.allure.Step
import okhttp3.Response
import utils.general.hasBodyContainingListOf
import utils.general.hasStatusCode
import utils.http.providers.OkHttpRequestProvider.getRequest

object GetTodoApi {

    @Step("Perform GET - request for TODO")
    fun <T> getTodos(authWay: AuthWay = AuthWay.noAuth(), queryParams: Map<String, String?> = emptyMap(), beforeResponseClose: (Response) -> T) =
        executeRequest(
            getRequest(todos, authWay, queryParams),
            beforeResponseClose
        )

    fun getTodos(authWay: AuthWay = AuthWay.noAuth(), queryParams: Map<String, String?> = emptyMap()) =
        getTodos(authWay, queryParams, isSuccessful)

    private val isSuccessful: (Response) -> Unit = { response ->
        assertThat(response).all {
            hasStatusCode(200)
            hasBodyContainingListOf(TodoGetResponseEntry::class)
        }
    }
}