package br.udesc.radar.ui.painel

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import br.udesc.radar.R
import br.udesc.radar.databinding.FragmentPainelBinding
import br.udesc.radar.ui.message
import br.udesc.radar.ui.observe
import br.udesc.radar.ui.radar
import br.udesc.radar.ui.showMessage
import br.udesc.radar.ui.showRows
import br.udesc.radar.ui.tarefas.TaskAdapter
import br.udesc.radar.util.ShakeDetector
import com.google.android.material.snackbar.Snackbar

class PainelFragment : Fragment(R.layout.fragment_painel) {
    private val viewModel by viewModels<PainelViewModel> {
        viewModelFactory { initializer { PainelViewModel(radar.academic) } }
    }
    private var shakeDetector: ShakeDetector? = null
    private var awaitingRefresh = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentPainelBinding.bind(view)
        val adapter = TaskAdapter(
            { id -> findNavController().navigate(R.id.taskDetailFragment, bundleOf("id" to id)) },
            viewModel::toggle,
        )
        binding.taskList.showRows(adapter)
        binding.holidays.setOnClickListener { findNavController().navigate(R.id.holidaysFragment) }
        observe(viewModel.errors, ::message)
        observe(viewModel.dashboard) { dashboard ->
            binding.summary.text = getString(
                R.string.ui_painel_resumo, dashboard.total, dashboard.pending, dashboard.overdue, dashboard.completed,
            )
            adapter.submitList(dashboard.upcoming)
            binding.empty.showMessage(
                "Nenhuma tarefa com entrega nos próximos sete dias.".takeIf { dashboard.upcoming.isEmpty() },
            )
            if (awaitingRefresh && dashboard.revision > 0) {
                awaitingRefresh = false
                Snackbar.make(view, "Painel atualizado!", Snackbar.LENGTH_SHORT)
                    .setAnchorView(requireActivity().findViewById(R.id.bottom_nav))
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (radar.preferences.getBoolean("shake_enabled", true)) {
            val detector = shakeDetector ?: ShakeDetector(requireContext()) {
                awaitingRefresh = true
                viewModel.refresh()
            }.also { shakeDetector = it }
            detector.start()
        }
    }

    override fun onPause() {
        shakeDetector?.stop()
        awaitingRefresh = false
        super.onPause()
    }

    override fun onDestroyView() {
        shakeDetector = null
        super.onDestroyView()
    }
}
