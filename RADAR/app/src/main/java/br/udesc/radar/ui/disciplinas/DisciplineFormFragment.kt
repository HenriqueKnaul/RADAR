package br.udesc.radar.ui.disciplinas

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import br.udesc.radar.R
import br.udesc.radar.databinding.FragmentDisciplineFormBinding
import br.udesc.radar.ui.message
import br.udesc.radar.ui.observe
import br.udesc.radar.ui.options
import br.udesc.radar.ui.radar
import br.udesc.radar.ui.showMessage
import kotlinx.coroutines.flow.take

class DisciplineFormFragment : Fragment(R.layout.fragment_discipline_form) {
    private val viewModel by viewModels<DisciplineFormViewModel> {
        viewModelFactory {
            initializer { DisciplineFormViewModel(radar.academic, requireArguments().getLong("id")) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentDisciplineFormBinding.bind(view)
        val editing = viewModel.id != 0L
        var ready = !editing
        var initial = savedInstanceState == null
        binding.heading.setText(if (editing) R.string.ui_editar_disciplina else R.string.ui_nova_disciplina)
        binding.color.options(DisciplineColor.entries.map { it.label })
        binding.save.isEnabled = ready
        if (editing) {
            observe(viewModel.discipline.take(1)) { discipline ->
                if (discipline == null) {
                    message("Disciplina não encontrada.")
                    findNavController().navigateUp()
                    return@observe
                }
                if (initial) {
                    binding.name.setText(discipline.name)
                    binding.teacher.setText(discipline.teacher)
                    val color = DisciplineColor.entries.indexOfFirst { it.argb == discipline.color }
                    binding.color.setSelection(color.coerceAtLeast(0))
                    initial = false
                }
                ready = true
                binding.save.isEnabled = !viewModel.saveState.value.saving
            }
        }
        binding.save.setOnClickListener {
            viewModel.save(
                binding.name.text.toString(),
                binding.teacher.text.toString(),
                DisciplineColor.entries[binding.color.selectedItemPosition.coerceAtLeast(0)],
            )
        }
        observe(viewModel.saveState) { state ->
            binding.save.isEnabled = ready && !state.saving && !state.saved
            binding.error.showMessage(state.error)
            if (state.saved) {
                message("Disciplina salva.")
                findNavController().navigateUp()
            }
        }
    }
}
