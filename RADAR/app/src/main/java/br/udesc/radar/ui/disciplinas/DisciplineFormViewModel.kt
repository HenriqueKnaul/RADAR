package br.udesc.radar.ui.disciplinas

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.udesc.radar.data.local.Discipline
import br.udesc.radar.data.repository.AcademicRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class DisciplineSaveState(val saving: Boolean = false, val saved: Boolean = false, val error: String? = null)

enum class DisciplineColor(val label: String, val rgb: Int) {
    BLUE("Azul", 0x4760A8),
    GREEN("Verde", 0x258470),
    PURPLE("Roxo", 0x8254A8),
    ORANGE("Laranja", 0xBE621F),
    PINK("Rosa", 0xBD4873);

    val argb: Int get() = 0xFF000000.toInt() or rgb
}

class DisciplineFormViewModel(private val academic: AcademicRepository, val id: Long) : ViewModel() {
    val discipline = academic.discipline(id)
    private val mutableSave = MutableStateFlow(DisciplineSaveState())
    val saveState = mutableSave.asStateFlow()

    fun save(name: String, teacher: String, color: DisciplineColor) {
        if (mutableSave.value.saving || mutableSave.value.saved) return
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            mutableSave.value = DisciplineSaveState(error = "Informe o nome da disciplina.")
            return
        }
        mutableSave.value = DisciplineSaveState(saving = true)
        viewModelScope.launch {
            mutableSave.value = try {
                academic.saveDiscipline(
                    Discipline(id, trimmedName, trimmedName.lowercase(Locale.ROOT), teacher.trim(), color.argb),
                )
                DisciplineSaveState(saved = true)
            } catch (error: CancellationException) {
                throw error
            } catch (_: SQLiteConstraintException) {
                DisciplineSaveState(error = "Já existe uma disciplina com esse nome.")
            } catch (_: Exception) {
                DisciplineSaveState(error = "Não foi possível salvar a disciplina.")
            }
        }
    }
}
