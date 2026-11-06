package br.udesc.radar.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Embedded
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
data class Disciplina(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val nome: String,
    @ColumnInfo(name = "nameKey") val chaveNome: String,
    @ColumnInfo(name = "teacher") val professor: String,
    @ColumnInfo(name = "color") val cor: Int,
)

@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(
        entity = Disciplina::class,
        parentColumns = ["id"],
        childColumns = ["disciplineId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("disciplineId")],
)
data class TarefaAcademica(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "disciplineId") val idDisciplina: Long,
    @ColumnInfo(name = "title") val titulo: String,
    @ColumnInfo(name = "description") val descricao: String,
    @ColumnInfo(name = "type") val tipo: String,
    @ColumnInfo(name = "priority") val prioridade: String,
    @ColumnInfo(name = "deadline") val prazo: Long,
    @ColumnInfo(name = "completed") val concluida: Boolean = false,
    @ColumnInfo(name = "photo") val foto: String? = null,
)

data class RegistroTarefa(
    @Embedded val tarefa: TarefaAcademica,
    @ColumnInfo(name = "disciplineName") val nomeDisciplina: String,
)

@Dao
interface DisciplinaDao {
    @Query("SELECT * FROM disciplines ORDER BY nameKey")
    fun observarTodos(): Flow<List<Disciplina>>

    @Query("SELECT * FROM disciplines WHERE id = :id")
    fun observar(id: Long): Flow<Disciplina?>

    @Query("SELECT * FROM disciplines WHERE id = :id")
    suspend fun buscar(id: Long): Disciplina?

    @Insert
    suspend fun inserir(disciplina: Disciplina): Long

    @Update
    suspend fun atualizar(disciplina: Disciplina): Int

    @Delete
    suspend fun excluir(disciplina: Disciplina): Int
}

@Dao
interface TarefaDao {
    @Query("""
        SELECT tasks.*, disciplines.name AS disciplineName
        FROM tasks JOIN disciplines ON tasks.disciplineId = disciplines.id
        ORDER BY tasks.completed, tasks.deadline, tasks.id
    """)
    fun observarTodos(): Flow<List<RegistroTarefa>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun buscar(id: Long): TarefaAcademica?

    @Query("SELECT photo FROM tasks WHERE disciplineId = :idDisciplina AND photo IS NOT NULL")
    suspend fun fotos(idDisciplina: Long): List<String>

    @Insert
    suspend fun inserir(tarefa: TarefaAcademica): Long

    @Update
    suspend fun atualizar(tarefa: TarefaAcademica): Int

    @Query("UPDATE tasks SET completed = :concluida WHERE id = :id")
    suspend fun definirConclusao(id: Long, concluida: Boolean): Int

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun excluir(id: Long): Int
}

@Database(entities = [Disciplina::class, TarefaAcademica::class], version = 1)
abstract class RadarDatabase : RoomDatabase() {
    abstract fun disciplinas(): DisciplinaDao
    abstract fun tarefas(): TarefaDao
}
