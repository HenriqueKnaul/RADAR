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
    private val modelo by viewModels<AjustesViewModel> {
        viewModelFactory { initializer { AjustesViewModel(radar.preferencias) } }
    }

    override fun onViewCreated(visao: View, estadoAnterior: Bundle?) {
        val vinculo = FragmentAjustesBinding.bind(visao)
        val gerenciador = requireContext().getSystemService(SensorManager::class.java)
        val disponivel = gerenciador.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
        vinculo.shake.isEnabled = disponivel
        vinculo.shake.isChecked = disponivel && modelo.agitacaoAtiva
        vinculo.sensorInfo.setText(if (disponivel) R.string.shake_enabled else R.string.shake_unavailable)
        vinculo.shake.setOnCheckedChangeListener { _, ativo -> modelo.definirAgitacaoAtiva(ativo) }
        vinculo.holidays.setOnClickListener { findNavController().navigate(R.id.holidaysFragment) }
    }
}
