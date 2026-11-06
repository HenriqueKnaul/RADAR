package br.udesc.radar

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.udesc.radar.data.local.AcademicTask
import br.udesc.radar.data.local.Discipline
import br.udesc.radar.data.local.RadarDatabase
import br.udesc.radar.data.repository.AcademicRepository
import br.udesc.radar.ui.tarefas.TaskFormViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class AcademicRepositoryTest {
    @Test
    fun crudSurvivesReopeningAndRemovesLinkedPhotos() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "test-${UUID.randomUUID()}.db"
        val files = File(context.cacheDir, name)
        try {
            val disciplineId = withDatabase(context, name) { db ->
                val repo = AcademicRepository(db, files)
                repo.saveDiscipline(Discipline(name = "Matemática", nameKey = "matemática", teacher = "", color = 0))
                val discipline = repo.disciplines().first().single()
                repo.saveDiscipline(discipline.copy(teacher = "Ana"))
                try {
                    repo.saveDiscipline(discipline.copy(id = 0L, name = "MATEMÁTICA"))
                    fail("Nome duplicado deve ser rejeitado pelo banco")
                } catch (_: SQLiteConstraintException) {
                    assertEquals(1, repo.disciplines().first().size)
                }
                assertTrue(repo.photoDirectory.mkdirs())
                repo.photoFile("original.jpg").writeText("anexo de teste")
                repo.saveTask(
                    AcademicTask(
                        disciplineId = discipline.id,
                        title = "Lista",
                        description = "",
                        type = "Tarefa",
                        priority = "Alta",
                        deadline = 1_800_000_000_000L,
                        photo = "original.jpg",
                    ),
                )
                discipline.id
            }

            withDatabase(context, name) { db ->
                val repo = AcademicRepository(db, files)
                val discipline = repo.discipline(disciplineId).first()!!
                assertEquals("Ana", discipline.teacher)
                val task = repo.tasks().first().single().task
                assertEquals("Lista", task.title)
                assertTrue(repo.photoFile("original.jpg").isFile)
                verifyEditDraft(repo, task)

                repo.setCompleted(task.id, true)
                repo.photoFile("nova.jpg").writeText("novo anexo de teste")
                repo.saveTask(task.copy(title = "Lista revisada", photo = "nova.jpg"))
                assertFalse(repo.photoFile("original.jpg").exists())
                assertEquals("Lista revisada", repo.task(task.id)?.title)
                assertEquals(true, repo.task(task.id)?.completed)
                repo.setCompleted(task.id, false)
                assertEquals(false, repo.task(task.id)?.completed)

                repo.deleteTask(task.id)
                assertNull(repo.task(task.id))
                assertFalse(repo.photoFile("nova.jpg").exists())

                repo.photoFile("vinculada.jpg").writeText("anexo vinculado de teste")
                repo.saveTask(task.copy(id = 0, photo = "vinculada.jpg"))
                repo.deleteDiscipline(discipline)
                assertTrue(repo.disciplines().first().isEmpty())
                assertTrue(repo.tasks().first().isEmpty())
                assertFalse(repo.photoFile("vinculada.jpg").exists())
            }
        } finally {
            context.deleteDatabase(name)
            files.deleteRecursively()
        }
    }

    private suspend fun verifyEditDraft(repo: AcademicRepository, task: AcademicTask) {
        withContext(Dispatchers.Main) {
            val saved = SavedStateHandle(mapOf("id" to task.id))
            val editor = TaskFormViewModel(repo, saved)
            withTimeout(5_000) { editor.form.first { !it.loading } }
            assertEquals(task.photo, editor.photo.value)
            assertEquals(task.deadline, editor.deadline.value)

            editor.removePhoto()
            val restored = TaskFormViewModel(repo, saved)
            withTimeout(5_000) { restored.form.first { !it.loading } }
            assertNull(restored.photo.value)
            assertEquals(task.deadline, restored.deadline.value)
            assertTrue(repo.photoFile(checkNotNull(task.photo)).isFile)
        }
    }

    private suspend fun <T> withDatabase(
        context: Context,
        name: String,
        action: suspend (RadarDatabase) -> T,
    ): T {
        val db = Room.databaseBuilder(context, RadarDatabase::class.java, name).build()
        try {
            return action(db)
        } finally {
            db.close()
        }
    }
}
