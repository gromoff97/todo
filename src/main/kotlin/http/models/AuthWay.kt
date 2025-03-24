package http.models

import okhttp3.Credentials.basic
import okhttp3.Headers

data class AuthWay(val headerBuilder: (Headers.Builder) -> Headers.Builder) {
    companion object {
        fun noAuth() = AuthWay { it }
        fun basicAuth(username: String, password: String) = AuthWay {
            it.add("Authorization", basic(username, password))
        }
        val adminBasicAuth = basicAuth("admin", "admin")
    }
}
