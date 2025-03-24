package db.models

import java.math.BigInteger

data class TodoEntity(
    val id: BigInteger,
    val text: String,
    val completed: Boolean
)
