package utils.general

import db.models.TodoEntity
import http.models.request.TodoCreationRequest
import http.models.request.TodoUpdateRequest
import http.models.response.TodoGetResponseEntry
import ws.models.TodoWebSocketMessage

fun TodoCreationRequest.toEntity() = TodoEntity(id!!, text!!, completed!!)

fun TodoEntity.toTodoCreationRequest() = TodoCreationRequest(id, text, completed)

fun TodoEntity.toTodoUpdateRequest() = TodoUpdateRequest(id, text, completed)

fun TodoEntity.toTodoGetEntry() = TodoGetResponseEntry(id, text, completed)

fun TodoCreationRequest.toWebSocketMessage() = TodoWebSocketMessage(
    data = TodoWebSocketMessage.Data(id!!, text!!, completed!!),
    type = TodoWebSocketMessage.Type.NEW_TODO
)