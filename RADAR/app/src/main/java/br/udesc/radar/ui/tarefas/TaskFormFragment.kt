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
import br.udesc.radar.ui.message
import br.udesc.radar.ui.observe
import br.udesc.radar.ui.options
import br.udesc.radar.ui.radar
import br.udesc.radar.ui.showMessage
import br.udesc.radar.util.AcademicDates
import coil.load
import kotlinx.coroutines.flow.combine
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TaskFormFragment : Fragment(R.layout.fragment_task_form) {
    private val viewModel by viewModels<TaskFormViewModel> {
        viewModelFactory { initializer { TaskFormViewModel(radar.academic, createSavedStateHandle()) } }
    }

    private val capture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        viewModel.finishCapture(it)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentTaskFormBinding.bind(view)
        val nav = findNavController()
        val editing = viewModel.id != 0L
        var initialized = false
        var disciplineIds: List<Long> = emptyList()
        binding.heading.setText(if (editing) R.string.ui_editar_tarefa else R.string.ui_nova_tarefa)
        binding.type.options(viewModel.types)
        binding.priority.options(viewModel.priorities, 1)
        observe(combine(viewModel.form, viewModel.disciplines, ::Pair)) { (form, disciplines) ->
            binding.empty.showMessage(when {
                form.loading -> "Carregando tarefa…"
                form.missing -> "A tarefa não está mais disponível."
                disciplines.isEmpty() -> "Cadastre uma disciplina antes de criar tarefas."
                else -> null
            })
            binding.form.isVisible = !form.loading && !form.missing && disciplines.isNotEmpty()
            if (!form.loading && !form.missing) {
                val ids = listOf(0L) + disciplines.map { it.id }
                val selected = if (initialized) {
                    disciplineIds.getOrNull(binding.discipline.selectedItemPosition) ?: 0L
                } else {
                    val restored = savedInstanceState?.takeIf { it.containsKey("selectedDiscipline") }
                    restored?.getLong("selectedDiscipline") ?: form.task?.disciplineId ?: viewModel.disciplineId
                }
                if (!initialized || ids != disciplineIds) {
                    disciplineIds = ids
                    binding.discipline.options(
                        listOf("Selecione uma disciplina") + disciplines.map { it.name },
                        ids.indexOf(selected).coerceAtLeast(0),
                    )
                }
                if (!initialized && savedInstanceState == null) {
                    form.task?.let { task ->
                        binding.title.setText(task.title)
                        binding.description.setText(task.description)
                        binding.type.setSelection(viewModel.types.indexOf(task.type).coerceAtLeast(0))
                        binding.priority.setSelection(viewModel.priorities.indexOf(task.priority).coerceAtLeast(0))
                    }
                }
                initialized = true
            }
            binding.error.showMessage(form.error)
            binding.save.isEnabled = !form.saving && !form.saved
            binding.camera.isEnabled = !form.saving
            binding.removePhoto.isEnabled = !form.saving
            if (form.saved) {
                message("Tarefa salva.")
                nav.navigateUp()
            }
        }
        observe(viewModel.deadline) { deadline ->
            val date = deadline?.let(AcademicDates::local)
            binding.date.text = date?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: getString(R.string.ui_selecionar_data)
            binding.time.text = date?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: getString(R.string.ui_selecionar_horario)
        }
        observe(viewModel.photo) { name ->
            binding.photo.isVisible = name != null
            binding.removePhoto.isVisible = name != null
            binding.photo.load(name?.let(viewModel::photoFile))
        }
        binding.date.setOnClickListener {
            val current = selectedDate()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                viewModel.setDeadline(AcademicDates.epoch(LocalDateTime.of(y, m + 1, d, current.hour, current.minute)))
            }, current.year, current.monthValue - 1, current.dayOfMonth).show()
        }
        binding.time.setOnClickListener {
            val current = selectedDate()
            TimePickerDialog(requireContext(), { _, h, m ->
                viewModel.setDeadline(AcademicDates.epoch(current.withHour(h).withMinute(m)))
            }, current.hour, current.minute, true).show()
        }
        binding.camera.setOnClickListener {
            try {
                val file = viewModel.createCapture()
                val uri = FileProvider.getUriForFile(requireContext(), requireContext().packageName + ".photos", file)
                capture.launch(uri)
            } catch (_: Exception) {
                viewModel.finishCapture(false)
                viewModel.captureFailed()
            }
        }
        binding.removePhoto.setOnClickListener { viewModel.removePhoto() }
        binding.save.setOnClickListener {
            viewModel.save(
                binding.title.text.toString(),
                binding.description.text.toString(),
                disciplineIds.getOrNull(binding.discipline.selectedItemPosition) ?: 0L,
                viewModel.types[binding.type.selectedItemPosition.coerceAtLeast(0)],
                viewModel.priorities[binding.priority.selectedItemPosition.coerceAtLeast(0)],
            )
        }
        selectedDiscipline = { disciplineIds.getOrNull(binding.discipline.selectedItemPosition) }
    }

    private var selectedDiscipline: (() -> Long?)? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedDiscipline?.invoke()?.let { outState.putLong("selectedDiscipline", it) }
    }

    override fun onDestroyView() {
        selectedDiscipline = null
        super.onDestroyView()
    }

    private fun selectedDate() = viewModel.deadline.value?.let(AcademicDates::local)
        ?: LocalDateTime.now().withHour(23).withMinute(59).withSecond(0).withNano(0)
}
