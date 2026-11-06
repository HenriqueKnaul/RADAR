package br.udesc.radar.data.repository

import androidx.room.withTransaction
import br.udesc.radar.data.local.AcademicTask
import br.udesc.radar.data.local.Discipline
import br.udesc.radar.data.local.RadarDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.File

class AcademicRepository(private val db: RadarDatabase, filesDir: File) {
    private val disciplines = db.disciplines()
    private val tasks = db.tasks()
    val photoDirectory = File(filesDir, "task-photos")

    fun disciplines() = disciplines.observeAll()
    fun discipline(id: Long) = disciplines.observe(id)
    fun tasks() = tasks.observeAll()
    suspend fun task(id: Long) = tasks.find(id)

    suspend fun saveDiscipline(discipline: Discipline) {
        if (discipline.id == 0L) disciplines.insert(discipline)
        else check(disciplines.update(discipline) == 1)
    }

    suspend fun deleteDiscipline(discipline: Discipline) {
        withContext(Dispatchers.IO + NonCancellable) {
            val photos = db.withTransaction {
                val paths = tasks.photos(discipline.id)
                disciplines.delete(discipline)
                paths
            }
            photos.forEach(::deletePhoto)
        }
    }

    suspend fun saveTask(task: AcademicTask) {
        withContext(Dispatchers.IO + NonCancellable) {
            val oldPhoto = db.withTransaction {
                check(disciplines.find(task.disciplineId) != null)
                if (task.id == 0L) {
                    tasks.insert(task)
                    null
                } else {
                    val previous = checkNotNull(tasks.find(task.id))
                    tasks.update(task.copy(completed = previous.completed))
                    previous.photo?.takeIf { it != task.photo }
                }
            }
            oldPhoto?.let(::deletePhoto)
        }
    }

    suspend fun deleteTask(id: Long) {
        withContext(Dispatchers.IO + NonCancellable) {
            val photo = db.withTransaction {
                val previous = tasks.find(id)
                tasks.delete(id)
                previous?.photo
            }
            photo?.let(::deletePhoto)
        }
    }

    suspend fun setCompleted(id: Long, completed: Boolean) {
        check(tasks.setCompleted(id, completed) == 1)
    }

    fun photoFile(name: String) = File(photoDirectory, name)
    fun deletePhoto(name: String) {
        photoFile(name).delete()
    }
}
