package http.client

import io.qameta.allure.Step
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object TodoHttpClient {
    private val client = OkHttpClient.Builder()
        .addNetworkInterceptor(BasicInterceptor())
        .build()

    @Step("Perform HTTP - request")
    fun <T> executeRequest(request: Request, beforeResponseCloseAction: (Response) -> T): T = client.newCall(request).execute().use(beforeResponseCloseAction)
}