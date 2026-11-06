package br.udesc.radar.data.remote

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class Feriado(
    @SerializedName("date") val data: String,
    @SerializedName("name") val nome: String,
)

interface ServicoFeriados {
    @GET("api/feriados/v1/{year}")
    suspend fun listar(@Path("year") ano: Int): List<Feriado>
}

class RepositorioFeriados {
    private val servico by lazy {
        Retrofit.Builder()
            .baseUrl("https://brasilapi.com.br/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServicoFeriados::class.java)
    }

    suspend fun listar(ano: Int) = servico.listar(ano)
        .onEach { LocalDate.parse(it.data) }
        .sortedBy { it.data }
}
