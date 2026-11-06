package br.udesc.radar.ui.feriados

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.udesc.radar.data.remote.Holiday
import br.udesc.radar.data.remote.HolidayRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Year

data class HolidaysState(
    val year: Int,
    val loading: Boolean = false,
    val holidays: List<Holiday> = emptyList(),
    val error: String? = null,
)

class HolidaysViewModel(private val repository: HolidayRepository, private val saved: SavedStateHandle) : ViewModel() {
    val years = listOf(2026, 2027)
    private val initialYear = saved.get<Int>("year")?.takeIf { it in years }
        ?: Year.now().value.takeIf { it in years }
        ?: years.first()
    private val mutableState = MutableStateFlow(HolidaysState(initialYear))
    val state = mutableState.asStateFlow()
    private var loading: Job? = null

    init {
        reload()
    }

    fun selectYear(year: Int) {
        if (year !in years || year == state.value.year) return
        saved["year"] = year
        load(year)
    }

    fun reload() = load(state.value.year)

    private fun load(year: Int) {
        loading?.cancel()
        mutableState.value = HolidaysState(year, loading = true)
        loading = viewModelScope.launch {
            mutableState.value = try {
                HolidaysState(year, holidays = repository.list(year))
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                HolidaysState(year, error = "Não foi possível consultar a BrasilAPI. Verifique a conexão e tente novamente.")
            }
        }
    }
}
