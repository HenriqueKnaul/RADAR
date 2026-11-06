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
import br.udesc.radar.data.local.Discipline
import br.udesc.radar.databinding.FragmentDisciplineDetailBinding
import br.udesc.radar.ui.message
import br.udesc.radar.ui.observe
import br.udesc.radar.ui.radar
import br.udesc.radar.ui.showMessage
import br.udesc.radar.ui.showRows
import br.udesc.radar.ui.tarefas.TaskAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DisciplineDetailFragment : Fragment(R.layout.fragment_discipline_detail) {
    private val viewModel by viewModels<DisciplineDetailViewModel> {
        viewModelFactory {
            initializer { DisciplineDetailViewModel(radar.academic, requireArguments().getLong("id")) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentDisciplineDetailBinding.bind(view)
        val nav = findNavController()
        val adapter = TaskAdapter(
            { nav.navigate(R.id.taskDetailFragment, bundleOf("id" to it)) },
            viewModel::toggle,
        )
        binding.taskList.showRows(adapter)
        binding.edit.isEnabled = false
        binding.add.isEnabled = false
        binding.delete.isEnabled = false
        binding.edit.setOnClickListener { nav.navigate(R.id.disciplineFormFragment, bundleOf("id" to viewModel.id)) }
        binding.add.setOnClickListener { nav.navigate(R.id.taskFormFragment, bundleOf("disciplineId" to viewModel.id)) }
        var loaded: Discipline? = null
        var linkedTasks = 0
        observe(viewModel.discipline) { discipline ->
            loaded = discipline
            if (discipline == null) {
                nav.navigateUp()
                return@observe
            }
            binding.name.text = discipline.name
            binding.teacher.text = discipline.teacher.ifBlank { "Professor(a) não informado(a)" }
            binding.edit.isEnabled = true
            binding.add.isEnabled = true
            binding.delete.isEnabled = true
        }
        observe(viewModel.tasks) { tasks ->
            linkedTasks = tasks.size
            adapter.submitList(tasks)
            binding.empty.showMessage("Nenhuma tarefa vinculada a esta disciplina.".takeIf { tasks.isEmpty() })
        }
        observe(viewModel.errors, ::message)
        observe(viewModel.deleteErrors, ::message)
        binding.delete.setOnClickListener {
            val discipline = loaded ?: return@setOnClickListener
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Excluir disciplina?")
                .setMessage(resources.getQuantityString(R.plurals.ui_excluir_disciplina_aviso, linkedTasks, linkedTasks))
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir") { _, _ -> viewModel.delete(discipline) }
                .show()
        }
    }
}
