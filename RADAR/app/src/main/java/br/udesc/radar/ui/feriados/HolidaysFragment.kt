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
import br.udesc.radar.ui.aoSelecionar
import br.udesc.radar.ui.configurarLista
import br.udesc.radar.ui.definirOpcoes
import br.udesc.radar.ui.exibirMensagem
import br.udesc.radar.ui.observar
import br.udesc.radar.ui.radar

class HolidaysFragment : Fragment(R.layout.fragment_holidays) {
    private val modelo by viewModels<FeriadosViewModel> {
        viewModelFactory { initializer { FeriadosViewModel(radar.feriados, createSavedStateHandle()) } }
    }

    override fun onViewCreated(visao: View, estadoAnterior: Bundle?) {
        val vinculo = FragmentHolidaysBinding.bind(visao)
        val adaptador = AdaptadorFeriados()
        vinculo.holidayList.configurarLista(adaptador)
        vinculo.year.definirOpcoes(modelo.anos.map(Int::toString), modelo.anos.indexOf(modelo.estado.value.ano))
        vinculo.year.aoSelecionar { modelo.selecionarAno(modelo.anos[it]) }
        vinculo.reload.setOnClickListener { modelo.recarregar() }
        observar(modelo.estado) { estado ->
            vinculo.progress.isVisible = estado.carregando
            vinculo.reload.isEnabled = !estado.carregando
            vinculo.message.exibirMensagem(estado.erro ?: "Nenhum feriado foi retornado para este ano."
                .takeIf { !estado.carregando && estado.feriados.isEmpty() })
            adaptador.submitList(estado.feriados)
        }
    }
}
