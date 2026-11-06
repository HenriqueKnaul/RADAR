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
import br.udesc.radar.ui.message
import br.udesc.radar.ui.observe
import br.udesc.radar.ui.onSelected
import br.udesc.radar.ui.options
import br.udesc.radar.ui.radar
import br.udesc.radar.ui.showMessage
import br.udesc.radar.ui.showRows

class TarefasFragment : Fragment(R.layout.fragment_tarefas) {
    private val viewModel by viewModels<TarefasViewModel> {
        viewModelFactory { initializer { TarefasViewModel(radar.academic, createSavedStateHandle()) } }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentTarefasBinding.bind(view)
        val nav = findNavController()
        val adapter = TaskAdapter(
            { nav.navigate(R.id.taskDetailFragment, bundleOf("id" to it)) },
            viewModel::toggle,
        )
        binding.taskList.showRows(adapter)
        binding.add.setOnClickListener { nav.navigate(R.id.taskFormFragment) }
        binding.status.options(viewModel.statuses, viewModel.status.value)
        binding.status.onSelected(viewModel::setStatus)
        observe(viewModel.disciplines) { disciplines ->
            val ids = listOf(0L) + disciplines.map { it.id }
            val position = ids.indexOf(viewModel.discipline.value).coerceAtLeast(0)
            binding.discipline.onItemSelectedListener = null
            binding.discipline.options(listOf("Todas as disciplinas") + disciplines.map { it.name }, position)
            binding.discipline.onSelected { viewModel.setDiscipline(ids[it]) }
            viewModel.setDiscipline(ids[position])
        }
        observe(viewModel.tasks) { tasks ->
            adapter.submitList(tasks)
            binding.empty.showMessage("Nenhuma tarefa para os filtros selecionados.".takeIf { tasks.isEmpty() })
        }
        observe(viewModel.errors, ::message)
    }
}
