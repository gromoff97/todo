package utils.http.providers

import okhttp3.HttpUrl
import utils.general.EnvironmentConfig
import java.net.URI

object UriProvider {
    private val DEFAULT_HOST = EnvironmentConfig.appHost
    private val DEFAULT_HTTP_SCHEME = EnvironmentConfig.appScheme
    private val DEFAULT_HTTP_PORT = EnvironmentConfig.appPort

    fun appHttpUri(pathSegments: List<String> = emptyList(), queryParameters: Map<String, String?> = emptyMap()) =
        HttpUrl.Builder().scheme(DEFAULT_HTTP_SCHEME).host(DEFAULT_HOST).port(DEFAULT_HTTP_PORT)
            .addPathSegments(pathSegments)
            .addQueryParameters(queryParameters)
            .build().toUri()

    fun appWsUrl(pathSegments: List<String> = emptyList(), queryParameters: Map<String, String?> = emptyMap()) =
        HttpUrl.Builder().scheme(DEFAULT_HTTP_SCHEME).host(DEFAULT_HOST).port(DEFAULT_HTTP_PORT)
            .addPathSegments(pathSegments)
            .addQueryParameters(queryParameters)
            .build().toUri().withScheme("ws")

    private fun HttpUrl.Builder.addPathSegments(pathSegments: List<String>): HttpUrl.Builder {
        pathSegments.forEach(this::addPathSegment)
        return this
    }

    private fun HttpUrl.Builder.addQueryParameters(parameters: Map<String, String?>): HttpUrl.Builder {
        parameters.forEach(this::addQueryParameter)
        return this
    }

    private fun URI.withScheme(newScheme: String): URI {
        return URI.create(this.toString().replaceFirst(scheme, newScheme))
    }
}