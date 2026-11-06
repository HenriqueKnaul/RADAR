package br.udesc.radar.ui.disciplinas

import androidx.lifecycle.ViewModel
import br.udesc.radar.data.repositorio.RepositorioAcademico

class DisciplinasViewModel(repositorioAcademico: RepositorioAcademico) : ViewModel() {
    val disciplinas = repositorioAcademico.disciplinas()
}
