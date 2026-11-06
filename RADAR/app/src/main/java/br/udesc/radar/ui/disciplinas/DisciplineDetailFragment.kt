package br.udesc.radar.ui.disciplinas

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import br.udesc.radar.R
import br.udesc.radar.data.local.Disciplina
import br.udesc.radar.databinding.FragmentDisciplineDetailBinding
import br.udesc.radar.ui.avisar
import br.udesc.radar.ui.configurarLista
import br.udesc.radar.ui.exibirMensagem
import br.udesc.radar.ui.observar
import br.udesc.radar.ui.radar
import br.udesc.radar.ui.tarefas.AdaptadorTarefas
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DisciplineDetailFragment : Fragment(R.layout.fragment_discipline_detail) {
    private val modelo by viewModels<DetalheDisciplinaViewModel> {
        viewModelFactory {
            initializer { DetalheDisciplinaViewModel(radar.repositorioAcademico, requireArguments().getLong("id")) }
        }
    }

    override fun onViewCreated(visao: View, estadoAnterior: Bundle?) {
        val vinculo = FragmentDisciplineDetailBinding.bind(visao)
        val navegacao = findNavController()
        val adaptador = AdaptadorTarefas(
            { navegacao.navigate(R.id.taskDetailFragment, bundleOf("id" to it)) },
            modelo::alterarConclusao,
        )
        vinculo.taskList.configurarLista(adaptador)
        vinculo.edit.isEnabled = false
        vinculo.add.isEnabled = false
        vinculo.delete.isEnabled = false
        vinculo.edit.setOnClickListener { navegacao.navigate(R.id.disciplineFormFragment, bundleOf("id" to modelo.id)) }
        vinculo.add.setOnClickListener { navegacao.navigate(R.id.taskFormFragment, bundleOf("disciplineId" to modelo.id)) }
        var carregada: Disciplina? = null
        var tarefasVinculadas = 0
        observar(modelo.disciplina) { disciplina ->
            carregada = disciplina
            if (disciplina == null) {
                navegacao.navigateUp()
                return@observar
            }
            vinculo.name.text = disciplina.nome
            vinculo.teacher.text = disciplina.professor.ifBlank { "Professor(a) não informado(a)" }
            vinculo.edit.isEnabled = true
            vinculo.add.isEnabled = true
            vinculo.delete.isEnabled = true
        }
        observar(modelo.tarefas) { tarefas ->
            tarefasVinculadas = tarefas.size
            adaptador.submitList(tarefas)
            vinculo.empty.exibirMensagem("Nenhuma tarefa vinculada a esta disciplina.".takeIf { tarefas.isEmpty() })
        }
        observar(modelo.erros, ::avisar)
        observar(modelo.errosExclusao, ::avisar)
        vinculo.delete.setOnClickListener {
            val disciplina = carregada ?: return@setOnClickListener
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Excluir disciplina?")
                .setMessage(resources.getQuantityString(R.plurals.ui_excluir_disciplina_aviso, tarefasVinculadas, tarefasVinculadas))
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir") { _, _ -> modelo.excluir(disciplina) }
                .show()
        }
    }
}
