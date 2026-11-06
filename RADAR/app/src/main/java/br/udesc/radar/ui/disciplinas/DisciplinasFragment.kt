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
import br.udesc.radar.ui.configurarLista
import br.udesc.radar.ui.exibirMensagem
import br.udesc.radar.ui.observar
import br.udesc.radar.ui.radar

class DisciplinasFragment : Fragment(R.layout.fragment_disciplinas) {
    private val modelo by viewModels<DisciplinasViewModel> {
        viewModelFactory { initializer { DisciplinasViewModel(radar.repositorioAcademico) } }
    }

    override fun onViewCreated(visao: View, estadoAnterior: Bundle?) {
        val vinculo = FragmentDisciplinasBinding.bind(visao)
        val adaptador = AdaptadorDisciplinas { id ->
            findNavController().navigate(R.id.disciplineDetailFragment, bundleOf("id" to id))
        }
        vinculo.disciplineList.configurarLista(adaptador)
        vinculo.add.setOnClickListener { findNavController().navigate(R.id.disciplineFormFragment) }
        observar(modelo.disciplinas) { registros ->
            adaptador.submitList(registros)
            vinculo.empty.exibirMensagem("Cadastre sua primeira disciplina para começar.".takeIf { registros.isEmpty() })
        }
    }
}
