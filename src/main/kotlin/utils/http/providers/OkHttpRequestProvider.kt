package utils.http.providers

import utils.http.providers.HttpHeadersProvider.restSimpleHeaders
import utils.http.providers.UriProvider.appHttpUri
import utils.http.providers.UriProvider.appWsUrl
import http.models.AuthWay
import okhttp3.Request
import okhttp3.RequestBody

object OkHttpRequestProvider {

    fun getRequest(pathSegments: List<String>, authWay: AuthWay = AuthWay.noAuth(), parameters: Map<String, String?> = emptyMap()) =
        Request.Builder().get()
            .url(appHttpUri(pathSegments, parameters).toURL())
            .headers(restSimpleHeaders(authWay))
            .build()

    fun postRequest(pathSegments: List<String>, reqBody: RequestBody, authWay: AuthWay = AuthWay.noAuth(), parameters: Map<String, String?> = emptyMap()) =
        Request.Builder().post(reqBody)
            .url(appHttpUri(pathSegments, parameters).toURL())
            .headers(restSimpleHeaders(authWay))
            .build()

    fun putRequest(pathSegments: List<String>, reqBody: RequestBody, authWay: AuthWay = AuthWay.noAuth(), parameters: Map<String, String?> = emptyMap()) =
        Request.Builder().put(reqBody)
            .url(appHttpUri(pathSegments, parameters).toURL())
            .headers(restSimpleHeaders(authWay))
            .build()

    fun patchRequest(pathSegments: List<String>, reqBody: RequestBody, authWay: AuthWay = AuthWay.noAuth(), parameters: Map<String, String?> = emptyMap()) =
        Request.Builder().patch(reqBody)
            .url(appHttpUri(pathSegments, parameters).toURL())
            .headers(restSimpleHeaders(authWay))
            .build()

    fun deleteRequest(pathSegments: List<String>, reqBody: RequestBody, authWay: AuthWay = AuthWay.noAuth(), parameters: Map<String, String?> = emptyMap()) =
        Request.Builder().delete(reqBody)
            .url(appHttpUri(pathSegments, parameters).toURL())
            .headers(restSimpleHeaders(authWay))
            .build()

    fun noAuthWebSocketRequest(pathSegments: List<String>) = Request.Builder()
        .url(appWsUrl(pathSegments).toString())
        .build()
}