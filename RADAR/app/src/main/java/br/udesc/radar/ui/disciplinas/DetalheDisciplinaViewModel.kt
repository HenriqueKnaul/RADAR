package br.udesc.radar.ui.disciplinas

import androidx.lifecycle.viewModelScope
import br.udesc.radar.data.local.Disciplina
import br.udesc.radar.data.repositorio.RepositorioAcademico
import br.udesc.radar.ui.tarefas.AcoesTarefa
import br.udesc.radar.ui.tarefas.apresentarEm
import br.udesc.radar.util.DatasAcademicas
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class DetalheDisciplinaViewModel(repositorioAcademico: RepositorioAcademico, val id: Long) : AcoesTarefa(repositorioAcademico) {
    val disciplina = repositorioAcademico.disciplina(id)
    val tarefas = combine(repositorioAcademico.tarefas(), DatasAcademicas.relogio()) { registros, agora ->
        registros.filter { it.tarefa.idDisciplina == id }.map { it.apresentarEm(agora) }
    }

    private val mensagens = Channel<String>(Channel.BUFFERED)
    val errosExclusao = mensagens.receiveAsFlow()

    fun excluir(disciplina: Disciplina) {
        viewModelScope.launch {
            try {
                repositorioAcademico.excluirDisciplina(disciplina)
            } catch (erro: CancellationException) {
                throw erro
            } catch (_: Exception) {
                mensagens.send("Não foi possível excluir a disciplina.")
            }
        }
    }
}
