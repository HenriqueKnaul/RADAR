package br.udesc.radar.ui.tarefas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.udesc.radar.R
import br.udesc.radar.data.local.RegistroTarefa
import br.udesc.radar.databinding.RowTaskBinding
import br.udesc.radar.util.DatasAcademicas

data class ItemTarefa(val registro: RegistroTarefa, val situacao: String)

fun RegistroTarefa.apresentarEm(agora: Long) = ItemTarefa(this, DatasAcademicas.situacao(tarefa, agora))

class AdaptadorTarefas(
    private val abrir: (Long) -> Unit,
    private val alterarConclusao: (Long, Boolean) -> Unit,
) : ListAdapter<ItemTarefa, AdaptadorTarefas.Suporte>(object : DiffUtil.ItemCallback<ItemTarefa>() {
    override fun areItemsTheSame(itemAnterior: ItemTarefa, itemNovo: ItemTarefa) =
        itemAnterior.registro.tarefa.id == itemNovo.registro.tarefa.id

    override fun areContentsTheSame(itemAnterior: ItemTarefa, itemNovo: ItemTarefa) = itemAnterior == itemNovo
}) {
    class Suporte(val vinculo: RowTaskBinding) : RecyclerView.ViewHolder(vinculo.root)

    override fun onCreateViewHolder(pai: ViewGroup, tipoDeVisao: Int) =
        Suporte(RowTaskBinding.inflate(LayoutInflater.from(pai.context), pai, false))

    override fun onBindViewHolder(suporte: Suporte, posicao: Int) {
        val (registro, situacao) = getItem(posicao)
        val tarefa = registro.tarefa
        with(suporte.vinculo) {
            title.text = tarefa.titulo
            info.text = root.context.getString(
                R.string.ui_informacoes_tarefa, registro.nomeDisciplina, tarefa.tipo,
                DatasAcademicas.formatar(tarefa.prazo), tarefa.prioridade.lowercase(), situacao,
            )
            root.setOnClickListener { abrir(tarefa.id) }
            done.setOnCheckedChangeListener(null)
            done.isChecked = tarefa.concluida
            done.setText(if (tarefa.concluida) R.string.ui_concluida_para_reabrir else R.string.ui_marcar_como_concluida)
            done.setOnCheckedChangeListener { _, marcado -> alterarConclusao(tarefa.id, marcado) }
        }
    }
}
