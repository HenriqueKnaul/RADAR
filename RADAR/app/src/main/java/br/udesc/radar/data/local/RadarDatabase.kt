package br.udesc.radar.data.local

import androidx.room.Embedded
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "disciplines", indices = [Index(value = ["nameKey"], unique = true)])
data class Discipline(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val nameKey: String,
    val teacher: String,
    val color: Int,
)

@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(
        entity = Discipline::class,
        parentColumns = ["id"],
        childColumns = ["disciplineId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("disciplineId")],
)
data class AcademicTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val disciplineId: Long,
    val title: String,
    val description: String,
    val type: String,
    val priority: String,
    val deadline: Long,
    val completed: Boolean = false,
    val photo: String? = null,
)

data class TaskRow(
    @Embedded val task: AcademicTask,
    val disciplineName: String,
)

@Dao
interface DisciplineDao {
    @Query("SELECT * FROM disciplines ORDER BY nameKey")
    fun observeAll(): Flow<List<Discipline>>

    @Query("SELECT * FROM disciplines WHERE id = :id")
    fun observe(id: Long): Flow<Discipline?>

    @Query("SELECT * FROM disciplines WHERE id = :id")
    suspend fun find(id: Long): Discipline?

    @Insert
    suspend fun insert(discipline: Discipline): Long

    @Update
    suspend fun update(discipline: Discipline): Int

    @Delete
    suspend fun delete(discipline: Discipline): Int
}

@Dao
interface TaskDao {
    @Query("""
        SELECT tasks.*, disciplines.name AS disciplineName
        FROM tasks JOIN disciplines ON tasks.disciplineId = disciplines.id
        ORDER BY tasks.completed, tasks.deadline, tasks.id
    """)
    fun observeAll(): Flow<List<TaskRow>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun find(id: Long): AcademicTask?

    @Query("SELECT photo FROM tasks WHERE disciplineId = :disciplineId AND photo IS NOT NULL")
    suspend fun photos(disciplineId: Long): List<String>

    @Insert
    suspend fun insert(task: AcademicTask): Long

    @Update
    suspend fun update(task: AcademicTask): Int

    @Query("UPDATE tasks SET completed = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean): Int

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: Long): Int
}

@Database(entities = [Discipline::class, AcademicTask::class], version = 1)
abstract class RadarDatabase : RoomDatabase() {
    abstract fun disciplines(): DisciplineDao
    abstract fun tasks(): TaskDao
}
