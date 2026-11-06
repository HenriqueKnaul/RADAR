package br.udesc.radar.ui.tarefas

import androidx.lifecycle.SavedStateHandle
import br.udesc.radar.data.repository.AcademicRepository
import br.udesc.radar.util.AcademicDates
import kotlinx.coroutines.flow.combine

class TarefasViewModel(academic: AcademicRepository, private val saved: SavedStateHandle) : TaskActions(academic) {
    val statuses = listOf("Todas", "Pendente", "Concluída", "Atrasada")
    val disciplines = academic.disciplines()
    val status = saved.getStateFlow("statusFilter", 0)
    val discipline = saved.getStateFlow("disciplineFilter", 0L)
    val tasks = combine(academic.tasks(), status, discipline, AcademicDates.clock()) { rows, status, discipline, now ->
        rows.asSequence()
            .filter { discipline == 0L || it.task.disciplineId == discipline }
            .map { it.displayAt(now) }
            .filter { status == 0 || it.status == statuses[status] }
            .toList()
    }

    fun setStatus(position: Int) { saved["statusFilter"] = position }
    fun setDiscipline(id: Long) { saved["disciplineFilter"] = id }
}
