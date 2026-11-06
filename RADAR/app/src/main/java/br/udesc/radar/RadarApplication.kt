package br.udesc.radar

import android.app.Application
import androidx.room.Room
import br.udesc.radar.data.local.RadarDatabase
import br.udesc.radar.data.remote.HolidayRepository
import br.udesc.radar.data.repository.AcademicRepository

class RadarApplication : Application() {
    val database by lazy {
        Room.databaseBuilder(this, RadarDatabase::class.java, "radar.db").build()
    }
    val academic by lazy { AcademicRepository(database, filesDir) }
    val holidays by lazy { HolidayRepository() }
    val preferences by lazy { getSharedPreferences("radar_preferences", MODE_PRIVATE) }
}
