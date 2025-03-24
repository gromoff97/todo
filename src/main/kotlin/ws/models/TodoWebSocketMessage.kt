package ws.models

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigInteger

data class TodoWebSocketMessage(
    val data: Data,
    val type: Type,
) {
    data class Data(
        val id: BigInteger,
        val text: String,
        val completed: Boolean,
    )

    enum class Type {
        @JsonProperty("new_todo") NEW_TODO
    }
}
