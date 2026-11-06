package br.udesc.radar.data.repositorio

import androidx.room.withTransaction
import br.udesc.radar.data.local.Disciplina
import br.udesc.radar.data.local.RadarDatabase
import br.udesc.radar.data.local.TarefaAcademica
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class RepositorioAcademico(private val banco: RadarDatabase, diretorioInterno: File) {
    private val disciplinas = banco.disciplinas()
    private val tarefas = banco.tarefas()
    val pastaFotos = File(diretorioInterno, "task-photos")

    fun disciplinas() = disciplinas.observarTodos()
    fun disciplina(id: Long) = disciplinas.observar(id)
    fun tarefas() = tarefas.observarTodos()
    suspend fun tarefa(id: Long) = tarefas.buscar(id)

    suspend fun salvarDisciplina(disciplina: Disciplina) {
        if (disciplina.id == 0L) disciplinas.inserir(disciplina)
        else check(disciplinas.atualizar(disciplina) == 1)
    }

    suspend fun excluirDisciplina(disciplina: Disciplina) {
        withContext(Dispatchers.IO + NonCancellable) {
            val fotos = banco.withTransaction {
                val caminhos = tarefas.fotos(disciplina.id)
                disciplinas.excluir(disciplina)
                caminhos
            }
            fotos.forEach(::excluirFoto)
        }
    }

    suspend fun salvarTarefa(tarefa: TarefaAcademica) {
        withContext(Dispatchers.IO + NonCancellable) {
            val fotoAnterior = banco.withTransaction {
                check(disciplinas.buscar(tarefa.idDisciplina) != null)
                if (tarefa.id == 0L) {
                    tarefas.inserir(tarefa)
                    null
                } else {
                    val anterior = checkNotNull(tarefas.buscar(tarefa.id))
                    tarefas.atualizar(tarefa.copy(concluida = anterior.concluida))
                    anterior.foto?.takeIf { it != tarefa.foto }
                }
            }
            fotoAnterior?.let(::excluirFoto)
        }
    }

    suspend fun excluirTarefa(id: Long) {
        withContext(Dispatchers.IO + NonCancellable) {
            val foto = banco.withTransaction {
                val anterior = tarefas.buscar(id)
                tarefas.excluir(id)
                anterior?.foto
            }
            foto?.let(::excluirFoto)
        }
    }

    suspend fun definirConclusao(id: Long, concluida: Boolean) {
        check(tarefas.definirConclusao(id, concluida) == 1)
    }

    fun arquivoFoto(nome: String) = File(pastaFotos, nome)
    fun excluirFoto(nome: String) {
        arquivoFoto(nome).delete()
    }
}
