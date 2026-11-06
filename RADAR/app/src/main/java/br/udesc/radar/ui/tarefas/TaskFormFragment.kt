package br.udesc.radar.ui.tarefas

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import br.udesc.radar.R
import br.udesc.radar.databinding.FragmentTaskFormBinding
import br.udesc.radar.ui.avisar
import br.udesc.radar.ui.definirOpcoes
import br.udesc.radar.ui.exibirMensagem
import br.udesc.radar.ui.observar
import br.udesc.radar.ui.radar
import br.udesc.radar.util.DatasAcademicas
import coil.load
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.combine

class TaskFormFragment : Fragment(R.layout.fragment_task_form) {
    private val modelo by viewModels<FormularioTarefaViewModel> {
        viewModelFactory { initializer { FormularioTarefaViewModel(radar.repositorioAcademico, createSavedStateHandle()) } }
    }

    private val captura = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        modelo.finalizarCaptura(it)
    }

    override fun onViewCreated(visao: View, estadoAnterior: Bundle?) {
        val vinculo = FragmentTaskFormBinding.bind(visao)
        val navegacao = findNavController()
        val editando = modelo.id != 0L
        var inicializado = false
        var idsDisciplinas: List<Long> = emptyList()
        vinculo.heading.setText(if (editando) R.string.ui_editar_tarefa else R.string.ui_nova_tarefa)
        vinculo.type.definirOpcoes(modelo.tipos)
        vinculo.priority.definirOpcoes(modelo.prioridades, 1)
        observar(combine(modelo.formulario, modelo.disciplinas, ::Pair)) { (formulario, disciplinas) ->
            vinculo.empty.exibirMensagem(when {
                formulario.carregando -> "Carregando tarefa…"
                formulario.indisponivel -> "A tarefa não está mais disponível."
                disciplinas.isEmpty() -> "Cadastre uma disciplina antes de criar tarefas."
                else -> null
            })
            vinculo.form.isVisible = !formulario.carregando && !formulario.indisponivel && disciplinas.isNotEmpty()
            if (!formulario.carregando && !formulario.indisponivel) {
                val ids = listOf(0L) + disciplinas.map { it.id }
                val selecionado = if (inicializado) {
                    idsDisciplinas.getOrNull(vinculo.discipline.selectedItemPosition) ?: 0L
                } else {
                    val restaurado = estadoAnterior?.takeIf { it.containsKey("selectedDiscipline") }
                    restaurado?.getLong("selectedDiscipline") ?: formulario.tarefa?.idDisciplina ?: modelo.idDisciplina
                }
                if (!inicializado || ids != idsDisciplinas) {
                    idsDisciplinas = ids
                    vinculo.discipline.definirOpcoes(
                        listOf("Selecione uma disciplina") + disciplinas.map { it.nome },
                        ids.indexOf(selecionado).coerceAtLeast(0),
                    )
                }
                if (!inicializado && estadoAnterior == null) {
                    formulario.tarefa?.let { tarefa ->
                        vinculo.title.setText(tarefa.titulo)
                        vinculo.description.setText(tarefa.descricao)
                        vinculo.type.setSelection(modelo.tipos.indexOf(tarefa.tipo).coerceAtLeast(0))
                        vinculo.priority.setSelection(modelo.prioridades.indexOf(tarefa.prioridade).coerceAtLeast(0))
                    }
                }
                inicializado = true
            }
            vinculo.error.exibirMensagem(formulario.erro)
            vinculo.save.isEnabled = !formulario.salvando && !formulario.salvo
            vinculo.camera.isEnabled = !formulario.salvando
            vinculo.removePhoto.isEnabled = !formulario.salvando
            if (formulario.salvo) {
                avisar("Tarefa salva.")
                navegacao.navigateUp()
            }
        }
        observar(modelo.prazo) { prazo ->
            val data = prazo?.let(DatasAcademicas::paraDataLocal)
            vinculo.date.text = data?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: getString(R.string.ui_selecionar_data)
            vinculo.time.text = data?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: getString(R.string.ui_selecionar_horario)
        }
        observar(modelo.foto) { nome ->
            vinculo.photo.isVisible = nome != null
            vinculo.removePhoto.isVisible = nome != null
            vinculo.photo.load(nome?.let(modelo::arquivoFoto))
        }
        vinculo.date.setOnClickListener {
            val atual = dataSelecionada()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                modelo.definirPrazo(DatasAcademicas.paraMillis(LocalDateTime.of(y, m + 1, d, atual.hour, atual.minute)))
            }, atual.year, atual.monthValue - 1, atual.dayOfMonth).show()
        }
        vinculo.time.setOnClickListener {
            val atual = dataSelecionada()
            TimePickerDialog(requireContext(), { _, h, m ->
                modelo.definirPrazo(DatasAcademicas.paraMillis(atual.withHour(h).withMinute(m)))
            }, atual.hour, atual.minute, true).show()
        }
        vinculo.camera.setOnClickListener {
            try {
                val arquivo = modelo.prepararCaptura()
                val uri = FileProvider.getUriForFile(requireContext(), requireContext().packageName + ".photos", arquivo)
                captura.launch(uri)
            } catch (_: Exception) {
                modelo.finalizarCaptura(false)
                modelo.informarFalhaNaCaptura()
            }
        }
        vinculo.removePhoto.setOnClickListener { modelo.removerFoto() }
        vinculo.save.setOnClickListener {
            modelo.salvar(
                vinculo.title.text.toString(),
                vinculo.description.text.toString(),
                idsDisciplinas.getOrNull(vinculo.discipline.selectedItemPosition) ?: 0L,
                modelo.tipos[vinculo.type.selectedItemPosition.coerceAtLeast(0)],
                modelo.prioridades[vinculo.priority.selectedItemPosition.coerceAtLeast(0)],
            )
        }
        disciplinaSelecionada = { idsDisciplinas.getOrNull(vinculo.discipline.selectedItemPosition) }
    }

    private var disciplinaSelecionada: (() -> Long?)? = null

    override fun onSaveInstanceState(estadoSaida: Bundle) {
        super.onSaveInstanceState(estadoSaida)
        disciplinaSelecionada?.invoke()?.let { estadoSaida.putLong("selectedDiscipline", it) }
    }

    override fun onDestroyView() {
        disciplinaSelecionada = null
        super.onDestroyView()
    }

    private fun dataSelecionada() = modelo.prazo.value?.let(DatasAcademicas::paraDataLocal)
        ?: LocalDateTime.now().withHour(23).withMinute(59).withSecond(0).withNano(0)
}
