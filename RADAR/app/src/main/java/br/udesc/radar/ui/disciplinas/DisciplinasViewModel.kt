package br.udesc.radar.ui.disciplinas

import androidx.lifecycle.ViewModel
import br.udesc.radar.data.repository.AcademicRepository

class DisciplinasViewModel(academic: AcademicRepository) : ViewModel() {
    val disciplines = academic.disciplines()
}
