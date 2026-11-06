package br.udesc.radar.ui.disciplinas

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.udesc.radar.data.local.Disciplina
import br.udesc.radar.databinding.RowDisciplineBinding

class AdaptadorDisciplinas(private val abrir: (Long) -> Unit) :
    ListAdapter<Disciplina, AdaptadorDisciplinas.Suporte>(object : DiffUtil.ItemCallback<Disciplina>() {
        override fun areItemsTheSame(itemAnterior: Disciplina, itemNovo: Disciplina) = itemAnterior.id == itemNovo.id
        override fun areContentsTheSame(itemAnterior: Disciplina, itemNovo: Disciplina) = itemAnterior == itemNovo
    }) {
    class Suporte(val vinculo: RowDisciplineBinding) : RecyclerView.ViewHolder(vinculo.root)

    override fun onCreateViewHolder(pai: ViewGroup, tipoDeVisao: Int) =
        Suporte(RowDisciplineBinding.inflate(LayoutInflater.from(pai.context), pai, false))

    override fun onBindViewHolder(suporte: Suporte, posicao: Int) {
        val disciplina = getItem(posicao)
        with(suporte.vinculo) {
            name.text = disciplina.nome
            teacher.text = disciplina.professor.ifBlank { "Professor(a) não informado(a)" }
            color.backgroundTintList = ColorStateList.valueOf(disciplina.cor)
            root.setOnClickListener { abrir(disciplina.id) }
        }
    }
}
