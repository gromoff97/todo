package utils.http.providers

import http.models.AuthWay
import okhttp3.Headers

object HttpHeadersProvider {
    private val basicHeadersBuilder: Headers.Builder get() = Headers.Builder().add("Accept", "application/json")

    fun restSimpleHeaders(authWay: AuthWay) = authWay.headerBuilder.invoke(basicHeadersBuilder).build()
}