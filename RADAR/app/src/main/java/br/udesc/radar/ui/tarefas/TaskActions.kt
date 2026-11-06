package br.udesc.radar.ui.tarefas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.udesc.radar.data.repository.AcademicRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

open class TaskActions(protected val academic: AcademicRepository) : ViewModel() {
    private val messages = Channel<String>(Channel.BUFFERED)
    val errors = messages.receiveAsFlow()

    fun toggle(id: Long, completed: Boolean) {
        viewModelScope.launch {
            try {
                academic.setCompleted(id, completed)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                messages.send("Não foi possível atualizar a tarefa.")
            }
        }
    }
}
