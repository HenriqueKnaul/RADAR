package br.udesc.radar.ui.disciplinas

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.udesc.radar.data.local.Disciplina
import br.udesc.radar.data.repositorio.RepositorioAcademico
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EstadoSalvamentoDisciplina(val salvando: Boolean = false, val salvo: Boolean = false, val erro: String? = null)

enum class CorDisciplina(val rotulo: String, val rgb: Int) {
    AZUL("Azul", 0x4760A8),
    VERDE("Verde", 0x258470),
    ROXO("Roxo", 0x8254A8),
    LARANJA("Laranja", 0xBE621F),
    ROSA("Rosa", 0xBD4873);

    val argb: Int get() = 0xFF000000.toInt() or rgb
}

class FormularioDisciplinaViewModel(private val repositorioAcademico: RepositorioAcademico, val id: Long) : ViewModel() {
    val disciplina = repositorioAcademico.disciplina(id)
    private val salvamentoMutavel = MutableStateFlow(EstadoSalvamentoDisciplina())
    val estadoSalvamento = salvamentoMutavel.asStateFlow()

    fun salvar(nome: String, professor: String, cor: CorDisciplina) {
        if (salvamentoMutavel.value.salvando || salvamentoMutavel.value.salvo) return
        val nomeAjustado = nome.trim()
        if (nomeAjustado.isEmpty()) {
            salvamentoMutavel.value = EstadoSalvamentoDisciplina(erro = "Informe o nome da disciplina.")
            return
        }
        salvamentoMutavel.value = EstadoSalvamentoDisciplina(salvando = true)
        viewModelScope.launch {
            salvamentoMutavel.value = try {
                repositorioAcademico.salvarDisciplina(
                    Disciplina(id, nomeAjustado, nomeAjustado.lowercase(Locale.ROOT), professor.trim(), cor.argb),
                )
                EstadoSalvamentoDisciplina(salvo = true)
            } catch (erro: CancellationException) {
                throw erro
            } catch (_: SQLiteConstraintException) {
                EstadoSalvamentoDisciplina(erro = "Já existe uma disciplina com esse nome.")
            } catch (_: Exception) {
                EstadoSalvamentoDisciplina(erro = "Não foi possível salvar a disciplina.")
            }
        }
    }
}
