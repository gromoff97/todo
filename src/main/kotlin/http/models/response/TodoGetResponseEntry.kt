package http.models.response

import java.math.BigInteger

data class TodoGetResponseEntry(
    val id: BigInteger,
    val text: String,
    val completed: Boolean
)
