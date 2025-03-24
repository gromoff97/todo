package http.models

object Segments {
    val todos = listOf("todos")
    val ws = listOf("ws")

    fun todos(oneMoreSegmentAsString: String) = todos.plus(oneMoreSegmentAsString)
}