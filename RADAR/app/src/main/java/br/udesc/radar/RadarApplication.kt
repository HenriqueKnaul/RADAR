package br.udesc.radar

import android.app.Application
import androidx.room.Room
import br.udesc.radar.data.local.RadarDatabase
import br.udesc.radar.data.remote.RepositorioFeriados
import br.udesc.radar.data.repositorio.RepositorioAcademico

class RadarApplication : Application() {
    val banco by lazy {
        Room.databaseBuilder(this, RadarDatabase::class.java, "radar.db").build()
    }
    val repositorioAcademico by lazy { RepositorioAcademico(banco, filesDir) }
    val feriados by lazy { RepositorioFeriados() }
    val preferencias by lazy { getSharedPreferences("radar_preferences", MODE_PRIVATE) }
}
