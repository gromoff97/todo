package http.api

import assertk.all
import assertk.assertThat
import http.client.TodoHttpClient.executeRequest
import http.models.AuthWay
import http.models.AuthWay.Companion.noAuth
import http.models.Segments.todos
import io.qameta.allure.Step
import okhttp3.Response
import utils.general.JsonUtils.toJsonRequestBody
import utils.general.hasBlankBody
import utils.general.hasStatusCode
import utils.http.providers.OkHttpRequestProvider.putRequest

object PutTodoApi {

    @Step("Perform PUT - request for TODO")
    fun <T> putTodo(todoId: String, requestBody: Any, authWay: AuthWay = noAuth(), queryParams: Map<String, String?> = emptyMap(), useBlock: (Response) -> T): T {
        val request = putRequest(todos(todoId), requestBody.toJsonRequestBody(), authWay, queryParams)
        return executeRequest(request, useBlock)
    }

    fun putTodo(todoId: String, requestBody: Any, authWay: AuthWay = noAuth(), queryParams: Map<String, String?> = emptyMap()) =
        putTodo(todoId, requestBody, authWay, queryParams, isSuccessful)

    private val isSuccessful: (Response) -> Unit = {
        assertThat(it).all {
            hasStatusCode(200)
            hasBlankBody()
        }
    }
}