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
import br.udesc.radar.ui.message
import br.udesc.radar.ui.observe
import br.udesc.radar.ui.radar
import br.udesc.radar.ui.showMessage
import br.udesc.radar.util.AcademicDates
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TaskDetailFragment : Fragment(R.layout.fragment_task_detail) {
    private val viewModel by viewModels<TaskDetailViewModel> {
        viewModelFactory { initializer { TaskDetailViewModel(radar.academic, requireArguments().getLong("id")) } }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentTaskDetailBinding.bind(view)
        val nav = findNavController()
        binding.toggle.isEnabled = false
        binding.edit.isEnabled = false
        binding.delete.isEnabled = false
        observe(viewModel.task) { item ->
            if (item == null) {
                nav.navigateUp()
                return@observe
            }
            val task = item.row.task
            binding.title.text = task.title
            binding.info.text = getString(
                R.string.ui_task_info, item.row.disciplineName, task.type,
                AcademicDates.format(task.deadline), task.priority.lowercase(), item.status,
            )
            binding.description.showMessage(task.description.takeIf { it.isNotBlank() })
            binding.photo.isVisible = task.photo != null
            binding.photo.load(task.photo?.let(viewModel::photoFile))
            binding.toggle.setText(if (task.completed) R.string.ui_reabrir_tarefa else R.string.ui_concluir_tarefa)
            binding.toggle.isEnabled = true
            binding.edit.isEnabled = true
            binding.delete.isEnabled = true
            binding.toggle.setOnClickListener { viewModel.toggle(task.id, !task.completed) }
        }
        observe(viewModel.errors, ::message)
        observe(viewModel.deleteErrors, ::message)
        binding.edit.setOnClickListener { nav.navigate(R.id.taskFormFragment, bundleOf("id" to viewModel.id)) }
        binding.delete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Excluir tarefa?")
                .setMessage("A tarefa e a foto anexada serão excluídas.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir") { _, _ -> viewModel.delete() }
                .show()
        }
    }
}
