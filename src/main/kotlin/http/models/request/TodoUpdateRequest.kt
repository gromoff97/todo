package http.models.request

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigInteger

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TodoUpdateRequest(
    val id: BigInteger? = null,
    val text: String? = null,
    val completed: Boolean? = null
)
