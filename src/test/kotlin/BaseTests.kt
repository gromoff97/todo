
import db.TodoRepository.deleteAllTodoEntities
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
abstract class BaseTests {

    @BeforeAll
    @AfterEach
    fun clearTodos() = deleteAllTodoEntities()
}