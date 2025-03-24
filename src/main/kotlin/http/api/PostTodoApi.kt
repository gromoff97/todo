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
import utils.http.providers.OkHttpRequestProvider.postRequest

object PostTodoApi {

    @Step("Perform POST - request for TODO")
    fun postTodo(requestBody: Any, authWay: AuthWay = noAuth(), queryParams: Map<String, String?> = emptyMap(), beforeResponseClose: (Response) -> Unit = isSuccessful) {
        executeRequest(
            postRequest(todos, requestBody.toJsonRequestBody(), authWay, queryParams),
            beforeResponseClose
        )
    }

    private val isSuccessful: (Response) -> Unit = { response ->
        assertThat(response).all {
            hasBlankBody()
            hasStatusCode(201)
        }
    }
}