package br.udesc.radar.ui.painel

import br.udesc.radar.data.repositorio.RepositorioAcademico
import br.udesc.radar.ui.tarefas.AcoesTarefa
import br.udesc.radar.ui.tarefas.ItemTarefa
import br.udesc.radar.ui.tarefas.apresentarEm
import br.udesc.radar.util.DatasAcademicas
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

data class ResumoPainel(
    val total: Int,
    val pendentes: Int,
    val atrasadas: Int,
    val concluidas: Int,
    val proximas: List<ItemTarefa>,
    val revisao: Int,
)

@OptIn(ExperimentalCoroutinesApi::class)
class PainelViewModel(repositorioAcademico: RepositorioAcademico) : AcoesTarefa(repositorioAcademico) {
    private val revisao = MutableStateFlow(0)
    val resumo = revisao.flatMapLatest { solicitacao ->
        combine(repositorioAcademico.tarefas(), DatasAcademicas.relogio()) { registros, agora ->
            val limiteProximas = DatasAcademicas.paraMillis(DatasAcademicas.paraDataLocal(agora).plusDays(7))
            val emAberto = registros.filterNot { it.tarefa.concluida }
            val atrasadas = emAberto.count { it.tarefa.prazo < agora }
            ResumoPainel(
                registros.size,
                emAberto.size - atrasadas,
                atrasadas,
                registros.size - emAberto.size,
                emAberto.filter { it.tarefa.prazo in agora..limiteProximas }.map { it.apresentarEm(agora) },
                solicitacao,
            )
        }
    }

    fun atualizarPainel() = revisao.update { it + 1 }
}
