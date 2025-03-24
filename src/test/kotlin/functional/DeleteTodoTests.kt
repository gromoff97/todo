package functional
import BaseTests
import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.single
import db.TodoRepository.insertTodo
import db.TodoRepository.insertTodos
import http.api.DeleteTodoApi.deleteTodo
import http.models.AuthWay.Companion.adminBasicAuth
import http.models.AuthWay.Companion.basicAuth
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import utils.general.TWO_POW_64
import utils.general.RandomPojoProvider.randomValidTodoEntities
import utils.general.RandomPojoProvider.randomValidTodoEntity
import utils.general.assertTodosListInDb
import utils.general.hasBlankBody
import utils.general.hasBodyAsString
import utils.general.hasStatusCode

@DisplayName("DELETE /todos/:id")
class DeleteTodoTests : BaseTests() {

    @Test
    @DisplayName("Happy path deleting")
    fun `deleting two entities one by one`() {
        val (firstEntity, secondEntity, thirdEntity) = insertTodos(randomValidTodoEntities(3))
        deleteTodo(secondEntity.id.toString(), authWay = adminBasicAuth)
        deleteTodo(thirdEntity.id.toString(), authWay = adminBasicAuth)
        assertTodosListInDb.single().isEqualTo(firstEntity)
    }

    @Test
    @DisplayName("Entity does not exist")
    fun `deleting non existing todo leads to not found error`() {
        deleteTodo("412", authWay = adminBasicAuth) { response ->
            assertThat(response).all {
                hasStatusCode(404)
                hasBlankBody()
            }
        }
    }

    @Test
    @DisplayName("Wrong authorization credentials")
    fun `wrong basic auth header leads to unauthorized error`() {
        val initialEntity = insertTodo(randomValidTodoEntity())
        deleteTodo(initialEntity.id.toString(), authWay = basicAuth("Anton", "Gromov")) { response ->
            assertThat(response).all {
                hasStatusCode(401)
                hasBlankBody()
            }
        }
        assertTodosListInDb.containsExactly(initialEntity)
    }

    @Test
    @DisplayName("No authorization")
    fun `basic auth header absence leads to unauthorized error`() {
        val initialEntity = insertTodo(randomValidTodoEntity())
        deleteTodo(initialEntity.id.toString()) { response ->
            assertThat(response).all {
                hasStatusCode(401)
                hasBlankBody()
            }
        }
        assertTodosListInDb.containsExactly(initialEntity)
    }

    @ParameterizedTest(name = "id is ''{0}''")
    @ValueSource(strings = ["a", " ", "!@#", "-3", TWO_POW_64])
    @DisplayName("Invalid id")
    fun `passing invalid id leads to bad request error`(invalidId: String) {
        deleteTodo(invalidId, authWay = adminBasicAuth) { response ->
            assertThat(response).all {
                hasStatusCode(400)
                hasBodyAsString().all {
                    contains("invalid", ignoreCase = true)
                    contains("path", ignoreCase = true)
                }
            }
        }
    }

    @Test
    @DisplayName("No id argument")
    fun `absence of id leads to method not allowed error`() {
        deleteTodo("", authWay = adminBasicAuth) { response ->
            assertThat(response).all {
                hasStatusCode(405)
                hasBodyAsString().isEqualTo("HTTP method not allowed")
            }
        }
    }
}