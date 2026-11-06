package br.udesc.radar.ui.tarefas

import androidx.lifecycle.SavedStateHandle
import br.udesc.radar.data.repositorio.RepositorioAcademico
import br.udesc.radar.util.DatasAcademicas
import kotlinx.coroutines.flow.combine

class TarefasViewModel(repositorioAcademico: RepositorioAcademico, private val salvo: SavedStateHandle) : AcoesTarefa(repositorioAcademico) {
    val situacoes = listOf("Todas", "Pendente", "Concluída", "Atrasada")
    val disciplinas = repositorioAcademico.disciplinas()
    val situacao = salvo.getStateFlow("statusFilter", 0)
    val disciplina = salvo.getStateFlow("disciplineFilter", 0L)
    val tarefas = combine(repositorioAcademico.tarefas(), situacao, disciplina, DatasAcademicas.relogio()) { registros, situacao, disciplina, agora ->
        registros.asSequence()
            .filter { disciplina == 0L || it.tarefa.idDisciplina == disciplina }
            .map { it.apresentarEm(agora) }
            .filter { situacao == 0 || it.situacao == situacoes[situacao] }
            .toList()
    }

    fun definirSituacao(posicao: Int) { salvo["statusFilter"] = posicao }
    fun definirDisciplina(id: Long) { salvo["disciplineFilter"] = id }
}
