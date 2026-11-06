package br.udesc.radar.ui.feriados

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.udesc.radar.data.remote.Holiday
import br.udesc.radar.databinding.RowHolidayBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class HolidayAdapter : ListAdapter<Holiday, HolidayAdapter.Holder>(object : DiffUtil.ItemCallback<Holiday>() {
    override fun areItemsTheSame(oldItem: Holiday, newItem: Holiday) =
        oldItem.date == newItem.date && oldItem.name == newItem.name

    override fun areContentsTheSame(oldItem: Holiday, newItem: Holiday) = oldItem == newItem
}) {
    private val formatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale("pt", "BR"))

    class Holder(val binding: RowHolidayBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(RowHolidayBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val holiday = getItem(position)
        holder.binding.title.text = holiday.name
        holder.binding.date.text = LocalDate.parse(holiday.date).format(formatter)
    }
}
