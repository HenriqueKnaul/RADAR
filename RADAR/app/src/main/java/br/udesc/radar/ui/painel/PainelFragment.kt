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
import br.udesc.radar.ui.avisar
import br.udesc.radar.ui.configurarLista
import br.udesc.radar.ui.exibirMensagem
import br.udesc.radar.ui.observar
import br.udesc.radar.ui.radar
import br.udesc.radar.ui.tarefas.AdaptadorTarefas
import br.udesc.radar.util.DetectorAgitacao
import com.google.android.material.snackbar.Snackbar

class PainelFragment : Fragment(R.layout.fragment_painel) {
    private val modelo by viewModels<PainelViewModel> {
        viewModelFactory { initializer { PainelViewModel(radar.repositorioAcademico) } }
    }
    private var detectorAgitacao: DetectorAgitacao? = null
    private var aguardandoAtualizacao = false

    override fun onViewCreated(visao: View, estadoAnterior: Bundle?) {
        val vinculo = FragmentPainelBinding.bind(visao)
        val adaptador = AdaptadorTarefas(
            { id -> findNavController().navigate(R.id.taskDetailFragment, bundleOf("id" to id)) },
            modelo::alterarConclusao,
        )
        vinculo.taskList.configurarLista(adaptador)
        vinculo.holidays.setOnClickListener { findNavController().navigate(R.id.holidaysFragment) }
        observar(modelo.erros, ::avisar)
        observar(modelo.resumo) { resumo ->
            vinculo.summary.text = getString(
                R.string.ui_painel_resumo, resumo.total, resumo.pendentes, resumo.atrasadas, resumo.concluidas,
            )
            adaptador.submitList(resumo.proximas)
            vinculo.empty.exibirMensagem(
                "Nenhuma tarefa com entrega nos próximos sete dias.".takeIf { resumo.proximas.isEmpty() },
            )
            if (aguardandoAtualizacao && resumo.revisao > 0) {
                aguardandoAtualizacao = false
                Snackbar.make(visao, "Painel atualizado!", Snackbar.LENGTH_SHORT)
                    .setAnchorView(requireActivity().findViewById(R.id.bottom_nav))
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (radar.preferencias.getBoolean("shake_enabled", true)) {
            val detector = detectorAgitacao ?: DetectorAgitacao(requireContext()) {
                aguardandoAtualizacao = true
                modelo.atualizarPainel()
            }.also { detectorAgitacao = it }
            detector.iniciar()
        }
    }

    override fun onPause() {
        detectorAgitacao?.parar()
        aguardandoAtualizacao = false
        super.onPause()
    }

    override fun onDestroyView() {
        detectorAgitacao = null
        super.onDestroyView()
    }
}
