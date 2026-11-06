package br.udesc.radar.ui.painel

import br.udesc.radar.data.repository.AcademicRepository
import br.udesc.radar.ui.tarefas.TaskActions
import br.udesc.radar.ui.tarefas.TaskItem
import br.udesc.radar.ui.tarefas.displayAt
import br.udesc.radar.util.AcademicDates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

data class Dashboard(
    val total: Int,
    val pending: Int,
    val overdue: Int,
    val completed: Int,
    val upcoming: List<TaskItem>,
    val revision: Int,
)

@OptIn(ExperimentalCoroutinesApi::class)
class PainelViewModel(academic: AcademicRepository) : TaskActions(academic) {
    private val revision = MutableStateFlow(0)
    val dashboard = revision.flatMapLatest { request ->
        combine(academic.tasks(), AcademicDates.clock()) { rows, now ->
            val upcomingLimit = AcademicDates.epoch(AcademicDates.local(now).plusDays(7))
            val open = rows.filterNot { it.task.completed }
            val overdue = open.count { it.task.deadline < now }
            Dashboard(
                rows.size,
                open.size - overdue,
                overdue,
                rows.size - open.size,
                open.filter { it.task.deadline in now..upcomingLimit }.map { it.displayAt(now) },
                request,
            )
        }
    }

    fun refresh() = revision.update { it + 1 }
}
