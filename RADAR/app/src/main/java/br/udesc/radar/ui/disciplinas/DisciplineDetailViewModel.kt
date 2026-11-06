package br.udesc.radar.ui.disciplinas

import androidx.lifecycle.viewModelScope
import br.udesc.radar.data.local.Discipline
import br.udesc.radar.data.repository.AcademicRepository
import br.udesc.radar.ui.tarefas.TaskActions
import br.udesc.radar.ui.tarefas.displayAt
import br.udesc.radar.util.AcademicDates
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class DisciplineDetailViewModel(academic: AcademicRepository, val id: Long) : TaskActions(academic) {
    val discipline = academic.discipline(id)
    val tasks = combine(academic.tasks(), AcademicDates.clock()) { rows, now ->
        rows.filter { it.task.disciplineId == id }.map { it.displayAt(now) }
    }

    private val messages = Channel<String>(Channel.BUFFERED)
    val deleteErrors = messages.receiveAsFlow()

    fun delete(discipline: Discipline) {
        viewModelScope.launch {
            try {
                academic.deleteDiscipline(discipline)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                messages.send("Não foi possível excluir a disciplina.")
            }
        }
    }
}
