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
import br.udesc.radar.ui.avisar
import br.udesc.radar.ui.definirOpcoes
import br.udesc.radar.ui.exibirMensagem
import br.udesc.radar.ui.observar
import br.udesc.radar.ui.radar
import kotlinx.coroutines.flow.take

class DisciplineFormFragment : Fragment(R.layout.fragment_discipline_form) {
    private val modelo by viewModels<FormularioDisciplinaViewModel> {
        viewModelFactory {
            initializer { FormularioDisciplinaViewModel(radar.repositorioAcademico, requireArguments().getLong("id")) }
        }
    }

    override fun onViewCreated(visao: View, estadoAnterior: Bundle?) {
        val vinculo = FragmentDisciplineFormBinding.bind(visao)
        val editando = modelo.id != 0L
        var pronto = !editando
        var inicial = estadoAnterior == null
        vinculo.heading.setText(if (editando) R.string.ui_editar_disciplina else R.string.ui_nova_disciplina)
        vinculo.color.definirOpcoes(CorDisciplina.entries.map { it.rotulo })
        vinculo.save.isEnabled = pronto
        if (editando) {
            observar(modelo.disciplina.take(1)) { disciplina ->
                if (disciplina == null) {
                    avisar("Disciplina não encontrada.")
                    findNavController().navigateUp()
                    return@observar
                }
                if (inicial) {
                    vinculo.name.setText(disciplina.nome)
                    vinculo.teacher.setText(disciplina.professor)
                    val cor = CorDisciplina.entries.indexOfFirst { it.argb == disciplina.cor }
                    vinculo.color.setSelection(cor.coerceAtLeast(0))
                    inicial = false
                }
                pronto = true
                vinculo.save.isEnabled = !modelo.estadoSalvamento.value.salvando
            }
        }
        vinculo.save.setOnClickListener {
            modelo.salvar(
                vinculo.name.text.toString(),
                vinculo.teacher.text.toString(),
                CorDisciplina.entries[vinculo.color.selectedItemPosition.coerceAtLeast(0)],
            )
        }
        observar(modelo.estadoSalvamento) { estado ->
            vinculo.save.isEnabled = pronto && !estado.salvando && !estado.salvo
            vinculo.error.exibirMensagem(estado.erro)
            if (estado.salvo) {
                avisar("Disciplina salva.")
                findNavController().navigateUp()
            }
        }
    }
}
