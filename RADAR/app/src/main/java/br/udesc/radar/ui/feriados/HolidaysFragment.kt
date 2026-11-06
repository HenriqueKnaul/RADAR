package br.udesc.radar.ui.feriados

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.udesc.radar.R
import br.udesc.radar.databinding.FragmentHolidaysBinding
import br.udesc.radar.ui.observe
import br.udesc.radar.ui.onSelected
import br.udesc.radar.ui.options
import br.udesc.radar.ui.radar
import br.udesc.radar.ui.showMessage
import br.udesc.radar.ui.showRows

class HolidaysFragment : Fragment(R.layout.fragment_holidays) {
    private val viewModel by viewModels<HolidaysViewModel> {
        viewModelFactory { initializer { HolidaysViewModel(radar.holidays, createSavedStateHandle()) } }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentHolidaysBinding.bind(view)
        val adapter = HolidayAdapter()
        binding.holidayList.showRows(adapter)
        binding.year.options(viewModel.years.map(Int::toString), viewModel.years.indexOf(viewModel.state.value.year))
        binding.year.onSelected { viewModel.selectYear(viewModel.years[it]) }
        binding.reload.setOnClickListener { viewModel.reload() }
        observe(viewModel.state) { state ->
            binding.progress.isVisible = state.loading
            binding.reload.isEnabled = !state.loading
            binding.message.showMessage(state.error ?: "Nenhum feriado foi retornado para este ano."
                .takeIf { !state.loading && state.holidays.isEmpty() })
            adapter.submitList(state.holidays)
        }
    }
}
