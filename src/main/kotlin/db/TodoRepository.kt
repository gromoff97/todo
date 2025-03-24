package db

import com.fasterxml.jackson.core.type.TypeReference
import db.models.TodoEntity
import http.api.DeleteTodoApi.deleteTodo
import http.api.GetTodoApi.getTodos
import http.api.PostTodoApi.postTodo
import http.models.AuthWay.Companion.adminBasicAuth
import io.qameta.allure.Step
import utils.general.JsonUtils.decodeTo
import utils.general.JsonUtils.toJsonNode
import utils.general.toTodoCreationRequest

/**
 * Pseudo database manipulation class
 * Was created only for demonstration purposes
 */
object TodoRepository {
    private val todoListTypeReference = object : TypeReference<List<TodoEntity>>() {}

    fun insertTodos(entities: List<TodoEntity>) = entities.apply {
        entities.forEach(TodoRepository::insertTodo)
    }

    @Step("Insert TODO entity in database")
    fun insertTodo(todoEntity: TodoEntity) = todoEntity.apply {
        postTodo(this.toTodoCreationRequest(), queryParams = mapOf("asRepo" to null))
    }

    @Step("Delete all TODO entities from database")
    fun deleteAllTodoEntities() = selectAllTodos().map(TodoEntity::id).toSet().forEach { id ->
        deleteTodo(id.toString(), adminBasicAuth, queryParams = mapOf("asRepo" to null))
    }

    @Step("Select all TODO entities from database")
    fun selectAllTodos(): List<TodoEntity> {
        val response = getTodos(queryParams = mapOf("asRepo" to null)) { it.body!!.string() }
        val jsonNode = response.toJsonNode().getOrThrow()
        return jsonNode.decodeTo(todoListTypeReference).getOrThrow()
    }
}