package br.udesc.radar.ui.tarefas

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import br.udesc.radar.R
import br.udesc.radar.databinding.FragmentTaskDetailBinding
import br.udesc.radar.ui.avisar
import br.udesc.radar.ui.exibirMensagem
import br.udesc.radar.ui.observar
import br.udesc.radar.ui.radar
import br.udesc.radar.util.DatasAcademicas
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TaskDetailFragment : Fragment(R.layout.fragment_task_detail) {
    private val modelo by viewModels<DetalheTarefaViewModel> {
        viewModelFactory { initializer { DetalheTarefaViewModel(radar.repositorioAcademico, requireArguments().getLong("id")) } }
    }

    override fun onViewCreated(visao: View, estadoAnterior: Bundle?) {
        val vinculo = FragmentTaskDetailBinding.bind(visao)
        val navegacao = findNavController()
        vinculo.alterarConclusao.isEnabled = false
        vinculo.editar.isEnabled = false
        vinculo.excluir.isEnabled = false
        observar(modelo.tarefa) { item ->
            if (item == null) {
                navegacao.navigateUp()
                return@observar
            }
            val tarefa = item.registro.tarefa
            vinculo.titulo.text = tarefa.titulo
            vinculo.informacoes.text = getString(
                R.string.ui_informacoes_tarefa, item.registro.nomeDisciplina, tarefa.tipo,
                DatasAcademicas.formatar(tarefa.prazo), tarefa.prioridade.lowercase(), item.situacao,
            )
            vinculo.descricao.exibirMensagem(tarefa.descricao.takeIf { it.isNotBlank() })
            vinculo.foto.isVisible = tarefa.foto != null
            vinculo.foto.load(tarefa.foto?.let(modelo::arquivoFoto))
            vinculo.alterarConclusao.setText(if (tarefa.concluida) R.string.ui_reabrir_tarefa else R.string.ui_concluir_tarefa)
            vinculo.alterarConclusao.isEnabled = true
            vinculo.editar.isEnabled = true
            vinculo.excluir.isEnabled = true
            vinculo.alterarConclusao.setOnClickListener { modelo.alterarConclusao(tarefa.id, !tarefa.concluida) }
        }
        observar(modelo.erros, ::avisar)
        observar(modelo.errosExclusao, ::avisar)
        vinculo.editar.setOnClickListener { navegacao.navigate(R.id.taskFormFragment, bundleOf("id" to modelo.id)) }
        vinculo.excluir.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Excluir tarefa?")
                .setMessage("A tarefa e a foto anexada serão excluídas.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir") { _, _ -> modelo.excluir() }
                .show()
        }
    }
}
