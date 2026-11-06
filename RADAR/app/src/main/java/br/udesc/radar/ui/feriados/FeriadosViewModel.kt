package br.udesc.radar.ui.feriados

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.udesc.radar.data.remote.Feriado
import br.udesc.radar.data.remote.RepositorioFeriados
import java.time.Year
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EstadoFeriados(
    val ano: Int,
    val carregando: Boolean = false,
    val feriados: List<Feriado> = emptyList(),
    val erro: String? = null,
)

class FeriadosViewModel(private val repositorio: RepositorioFeriados, private val salvo: SavedStateHandle) : ViewModel() {
    val anos = listOf(2026, 2027)
    private val anoInicial = salvo.get<Int>("year")?.takeIf { it in anos }
        ?: Year.now().value.takeIf { it in anos }
        ?: anos.first()
    private val estadoMutavel = MutableStateFlow(EstadoFeriados(anoInicial))
    val estado = estadoMutavel.asStateFlow()
    private var carregando: Job? = null

    init {
        recarregar()
    }

    fun selecionarAno(ano: Int) {
        if (ano !in anos || ano == estado.value.ano) return
        salvo["year"] = ano
        carregar(ano)
    }

    fun recarregar() = carregar(estado.value.ano)

    private fun carregar(ano: Int) {
        carregando?.cancel()
        estadoMutavel.value = EstadoFeriados(ano, carregando = true)
        carregando = viewModelScope.launch {
            estadoMutavel.value = try {
                EstadoFeriados(ano, feriados = repositorio.listar(ano))
            } catch (erro: CancellationException) {
                throw erro
            } catch (_: Exception) {
                EstadoFeriados(ano, erro = "Não foi possível consultar a BrasilAPI. Verifique a conexão e tente novamente.")
            }
        }
    }
}
