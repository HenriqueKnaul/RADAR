package br.udesc.radar.ui.feriados

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.udesc.radar.data.remote.Feriado
import br.udesc.radar.databinding.RowHolidayBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class AdaptadorFeriados : ListAdapter<Feriado, AdaptadorFeriados.Suporte>(object : DiffUtil.ItemCallback<Feriado>() {
    override fun areItemsTheSame(itemAnterior: Feriado, itemNovo: Feriado) =
        itemAnterior.data == itemNovo.data && itemAnterior.nome == itemNovo.nome

    override fun areContentsTheSame(itemAnterior: Feriado, itemNovo: Feriado) = itemAnterior == itemNovo
}) {
    private val formatador = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale("pt", "BR"))

    class Suporte(val vinculo: RowHolidayBinding) : RecyclerView.ViewHolder(vinculo.root)

    override fun onCreateViewHolder(pai: ViewGroup, tipoDeVisao: Int) =
        Suporte(RowHolidayBinding.inflate(LayoutInflater.from(pai.context), pai, false))

    override fun onBindViewHolder(suporte: Suporte, posicao: Int) {
        val feriado = getItem(posicao)
        suporte.vinculo.title.text = feriado.nome
        suporte.vinculo.date.text = LocalDate.parse(feriado.data).format(formatador)
    }
}
