package br.udesc.radar.ui.tarefas

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import br.udesc.radar.R
import br.udesc.radar.databinding.FragmentTarefasBinding
import br.udesc.radar.ui.aoSelecionar
import br.udesc.radar.ui.avisar
import br.udesc.radar.ui.configurarLista
import br.udesc.radar.ui.definirOpcoes
import br.udesc.radar.ui.exibirMensagem
import br.udesc.radar.ui.observar
import br.udesc.radar.ui.radar

class TarefasFragment : Fragment(R.layout.fragment_tarefas) {
    private val modelo by viewModels<TarefasViewModel> {
        viewModelFactory { initializer { TarefasViewModel(radar.repositorioAcademico, createSavedStateHandle()) } }
    }

    override fun onViewCreated(visao: View, estadoAnterior: Bundle?) {
        val vinculo = FragmentTarefasBinding.bind(visao)
        val navegacao = findNavController()
        val adaptador = AdaptadorTarefas(
            { navegacao.navigate(R.id.taskDetailFragment, bundleOf("id" to it)) },
            modelo::alterarConclusao,
        )
        vinculo.taskList.configurarLista(adaptador)
        vinculo.add.setOnClickListener { navegacao.navigate(R.id.taskFormFragment) }
        vinculo.status.definirOpcoes(modelo.situacoes, modelo.situacao.value)
        vinculo.status.aoSelecionar(modelo::definirSituacao)
        observar(modelo.disciplinas) { disciplinas ->
            val ids = listOf(0L) + disciplinas.map { it.id }
            val posicao = ids.indexOf(modelo.disciplina.value).coerceAtLeast(0)
            vinculo.discipline.onItemSelectedListener = null
            vinculo.discipline.definirOpcoes(listOf("Todas as disciplinas") + disciplinas.map { it.nome }, posicao)
            vinculo.discipline.aoSelecionar { modelo.definirDisciplina(ids[it]) }
            modelo.definirDisciplina(ids[posicao])
        }
        observar(modelo.tarefas) { tarefas ->
            adaptador.submitList(tarefas)
            vinculo.empty.exibirMensagem("Nenhuma tarefa para os filtros selecionados.".takeIf { tarefas.isEmpty() })
        }
        observar(modelo.erros, ::avisar)
    }
}
