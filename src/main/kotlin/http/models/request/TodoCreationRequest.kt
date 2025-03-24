package http.models.request

import java.math.BigInteger

data class TodoCreationRequest(
    val id: BigInteger?,
    val text: String?,
    val completed: Boolean?
)
