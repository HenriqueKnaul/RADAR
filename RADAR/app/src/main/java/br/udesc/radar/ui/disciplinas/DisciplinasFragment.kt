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
import br.udesc.radar.databinding.FragmentDisciplinasBinding
import br.udesc.radar.ui.observe
import br.udesc.radar.ui.radar
import br.udesc.radar.ui.showMessage
import br.udesc.radar.ui.showRows

class DisciplinasFragment : Fragment(R.layout.fragment_disciplinas) {
    private val viewModel by viewModels<DisciplinasViewModel> {
        viewModelFactory { initializer { DisciplinasViewModel(radar.academic) } }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentDisciplinasBinding.bind(view)
        val adapter = DisciplineAdapter { id ->
            findNavController().navigate(R.id.disciplineDetailFragment, bundleOf("id" to id))
        }
        binding.disciplineList.showRows(adapter)
        binding.add.setOnClickListener { findNavController().navigate(R.id.disciplineFormFragment) }
        observe(viewModel.disciplines) { rows ->
            adapter.submitList(rows)
            binding.empty.showMessage("Cadastre sua primeira disciplina para começar.".takeIf { rows.isEmpty() })
        }
    }
}
