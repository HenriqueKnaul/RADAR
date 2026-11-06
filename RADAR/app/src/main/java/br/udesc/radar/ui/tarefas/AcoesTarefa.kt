package br.udesc.radar.ui.tarefas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.udesc.radar.data.repositorio.RepositorioAcademico
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

open class AcoesTarefa(protected val repositorioAcademico: RepositorioAcademico) : ViewModel() {
    private val mensagens = Channel<String>(Channel.BUFFERED)
    val erros = mensagens.receiveAsFlow()

    fun alterarConclusao(id: Long, concluida: Boolean) {
        viewModelScope.launch {
            try {
                repositorioAcademico.definirConclusao(id, concluida)
            } catch (erro: CancellationException) {
                throw erro
            } catch (_: Exception) {
                mensagens.send("Não foi possível atualizar a tarefa.")
            }
        }
    }
}
