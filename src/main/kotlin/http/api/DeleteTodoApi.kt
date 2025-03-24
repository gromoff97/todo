package http.api

import assertk.all
import assertk.assertThat
import http.client.TodoHttpClient.executeRequest
import http.models.AuthWay
import http.models.AuthWay.Companion.noAuth
import http.models.Segments.todos
import io.qameta.allure.Step
import okhttp3.Response
import okhttp3.internal.EMPTY_REQUEST
import utils.general.hasBlankBody
import utils.general.hasStatusCode
import utils.http.providers.OkHttpRequestProvider.deleteRequest

object DeleteTodoApi {

    @Step("Perform DELETE - request for TODO")
    fun deleteTodo(todoId: String, authWay: AuthWay = noAuth(), queryParams: Map<String, String?> = emptyMap(), useBlock: (Response) -> Unit = isSuccessful) {
        val request = deleteRequest(todos(todoId), EMPTY_REQUEST, authWay, queryParams)
        return executeRequest(request, useBlock)
    }

    private val isSuccessful: (Response) -> Unit = {
        assertThat(it).all {
            hasBlankBody()
            hasStatusCode(204)
        }
    }
}