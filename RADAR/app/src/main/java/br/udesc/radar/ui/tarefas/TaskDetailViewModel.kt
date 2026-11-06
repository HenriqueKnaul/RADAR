package br.udesc.radar.ui.tarefas

import androidx.lifecycle.viewModelScope
import br.udesc.radar.data.repository.AcademicRepository
import br.udesc.radar.util.AcademicDates
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TaskDetailViewModel(academic: AcademicRepository, val id: Long) : TaskActions(academic) {
    val task = combine(academic.tasks(), AcademicDates.clock()) { rows, now ->
        rows.firstOrNull { it.task.id == id }?.displayAt(now)
    }
    private val messages = Channel<String>(Channel.BUFFERED)
    val deleteErrors = messages.receiveAsFlow()

    fun photoFile(name: String) = academic.photoFile(name)

    fun delete() {
        viewModelScope.launch {
            try {
                academic.deleteTask(id)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                messages.send("Não foi possível excluir a tarefa.")
            }
        }
    }
}
