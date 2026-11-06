package br.udesc.radar.ui.tarefas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.udesc.radar.data.local.AcademicTask
import br.udesc.radar.data.repository.AcademicRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class TaskFormState(
    val loading: Boolean = true,
    val missing: Boolean = false,
    val task: AcademicTask? = null,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
)

class TaskFormViewModel(private val academic: AcademicRepository, private val state: SavedStateHandle) : ViewModel() {
    val id = state.get<Long>("id") ?: 0L
    val disciplineId = state.get<Long>("disciplineId") ?: 0L
    val disciplines = academic.disciplines()
    val types = listOf("Tarefa", "Trabalho", "Prova")
    val priorities = listOf("Baixa", "Média", "Alta")

    private val mutableForm = MutableStateFlow(TaskFormState())
    val form = mutableForm.asStateFlow()
    val photo = state.getStateFlow<String?>("photo", null)
    val deadline = state.getStateFlow<Long?>("deadline", null)

    init {
        viewModelScope.launch {
            try {
                val task = if (id == 0L) null else academic.task(id)
                if (state.get<Boolean>("draftInitialized") != true) {
                    state["photo"] = task?.photo
                    state["deadline"] = task?.deadline
                    state["draftInitialized"] = true
                }
                mutableForm.value = TaskFormState(loading = false, missing = id != 0L && task == null, task = task)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableForm.value = TaskFormState(loading = false, missing = true)
            }
        }
    }

    fun setDeadline(value: Long) {
        state["deadline"] = value
    }

    fun photoFile(name: String) = academic.photoFile(name)

    fun createCapture(): File {
        check(state.get<String>("pendingCapture") == null)
        val directory = academic.photoDirectory
        check(directory.isDirectory || directory.mkdirs())
        return File.createTempFile("radar-", ".jpg", directory).also { state["pendingCapture"] = it.name }
    }

    fun finishCapture(success: Boolean) {
        val name = state.remove<String>("pendingCapture") ?: return
        if (success && photoFile(name).length() > 0L) {
            state.remove<String>("newPhoto")?.let(academic::deletePhoto)
            state["newPhoto"] = name
            state["photo"] = name
        } else {
            academic.deletePhoto(name)
            if (success) captureFailed()
        }
    }

    fun removePhoto() {
        state.remove<String>("newPhoto")?.let(academic::deletePhoto)
        state["photo"] = null
    }

    fun captureFailed() {
        mutableForm.value = mutableForm.value.copy(error = "Não foi possível usar a câmera. Confira se há um app de câmera disponível.")
    }

    fun save(title: String, description: String, disciplineId: Long, type: String, priority: String) {
        val current = mutableForm.value
        if (current.loading || current.missing || current.saving || current.saved) return
        val due = deadline.value
        val error = when {
            title.isBlank() -> "Informe o título da tarefa."
            disciplineId == 0L -> "Escolha a disciplina da tarefa."
            due == null -> "Selecione o prazo da tarefa."
            else -> null
        }
        if (error != null) {
            mutableForm.value = current.copy(error = error)
            return
        }
        val task = AcademicTask(
            id, disciplineId, title.trim(), description.trim(), type, priority, checkNotNull(due),
            photo = photo.value,
        )
        mutableForm.value = current.copy(saving = true, error = null)
        viewModelScope.launch {
            mutableForm.value = try {
                academic.saveTask(task)
                state.remove<String>("newPhoto")
                current.copy(saved = true, error = null)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                current.copy(error = "Não foi possível salvar. Confira se a disciplina ainda está cadastrada.")
            }
        }
    }

    override fun onCleared() {
        if (!mutableForm.value.saving) {
            state.remove<String>("pendingCapture")?.let(academic::deletePhoto)
            state.remove<String>("newPhoto")?.let(academic::deletePhoto)
        }
    }
}
