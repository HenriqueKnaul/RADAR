package br.udesc.radar.ui.disciplinas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.udesc.radar.data.local.Discipline
import br.udesc.radar.databinding.RowDisciplineBinding

class DisciplineAdapter(private val open: (Long) -> Unit) :
    ListAdapter<Discipline, DisciplineAdapter.Holder>(object : DiffUtil.ItemCallback<Discipline>() {
        override fun areItemsTheSame(oldItem: Discipline, newItem: Discipline) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Discipline, newItem: Discipline) = oldItem == newItem
    }) {
    class Holder(val binding: RowDisciplineBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(RowDisciplineBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val discipline = getItem(position)
        with(holder.binding) {
            name.text = discipline.name
            teacher.text = discipline.teacher.ifBlank { "Professor(a) não informado(a)" }
            color.setBackgroundColor(discipline.color)
            root.setOnClickListener { open(discipline.id) }
        }
    }
}
