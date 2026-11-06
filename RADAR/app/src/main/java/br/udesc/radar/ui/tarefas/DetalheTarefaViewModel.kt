package br.udesc.radar.ui.tarefas

import androidx.lifecycle.viewModelScope
import br.udesc.radar.data.repositorio.RepositorioAcademico
import br.udesc.radar.util.DatasAcademicas
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class DetalheTarefaViewModel(repositorioAcademico: RepositorioAcademico, val id: Long) : AcoesTarefa(repositorioAcademico) {
    val tarefa = combine(repositorioAcademico.tarefas(), DatasAcademicas.relogio()) { registros, agora ->
        registros.firstOrNull { it.tarefa.id == id }?.apresentarEm(agora)
    }
    private val mensagens = Channel<String>(Channel.BUFFERED)
    val errosExclusao = mensagens.receiveAsFlow()

    fun arquivoFoto(nome: String) = repositorioAcademico.arquivoFoto(nome)

    fun excluir() {
        viewModelScope.launch {
            try {
                repositorioAcademico.excluirTarefa(id)
            } catch (erro: CancellationException) {
                throw erro
            } catch (_: Exception) {
                mensagens.send("Não foi possível excluir a tarefa.")
            }
        }
    }
}
