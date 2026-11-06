package br.udesc.radar.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.time.LocalDate

data class Holiday(val date: String, val name: String)

interface HolidayService {
    @GET("api/feriados/v1/{year}")
    suspend fun list(@Path("year") year: Int): List<Holiday>
}

class HolidayRepository {
    private val service by lazy {
        Retrofit.Builder()
            .baseUrl("https://brasilapi.com.br/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HolidayService::class.java)
    }

    suspend fun list(year: Int) = service.list(year)
        .onEach { LocalDate.parse(it.date) }
        .sortedBy { it.date }
}
