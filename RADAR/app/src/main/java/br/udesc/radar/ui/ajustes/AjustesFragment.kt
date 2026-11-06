package br.udesc.radar.ui.ajustes

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import br.udesc.radar.R
import br.udesc.radar.databinding.FragmentAjustesBinding
import br.udesc.radar.ui.radar

class AjustesFragment : Fragment(R.layout.fragment_ajustes) {
    private val viewModel by viewModels<AjustesViewModel> {
        viewModelFactory { initializer { AjustesViewModel(radar.preferences) } }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentAjustesBinding.bind(view)
        val manager = requireContext().getSystemService(SensorManager::class.java)
        val available = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
        binding.shake.isEnabled = available
        binding.shake.isChecked = available && viewModel.shakeEnabled
        binding.sensorInfo.setText(if (available) R.string.shake_enabled else R.string.shake_unavailable)
        binding.shake.setOnCheckedChangeListener { _, enabled -> viewModel.setShakeEnabled(enabled) }
        binding.holidays.setOnClickListener { findNavController().navigate(R.id.holidaysFragment) }
    }
}
