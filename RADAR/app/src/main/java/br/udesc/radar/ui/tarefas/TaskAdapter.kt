package br.udesc.radar.ui.tarefas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.udesc.radar.R
import br.udesc.radar.data.local.TaskRow
import br.udesc.radar.databinding.RowTaskBinding
import br.udesc.radar.util.AcademicDates

data class TaskItem(val row: TaskRow, val status: String)

fun TaskRow.displayAt(now: Long) = TaskItem(this, AcademicDates.status(task, now))

class TaskAdapter(
    private val open: (Long) -> Unit,
    private val toggle: (Long, Boolean) -> Unit,
) : ListAdapter<TaskItem, TaskAdapter.Holder>(object : DiffUtil.ItemCallback<TaskItem>() {
    override fun areItemsTheSame(oldItem: TaskItem, newItem: TaskItem) =
        oldItem.row.task.id == newItem.row.task.id

    override fun areContentsTheSame(oldItem: TaskItem, newItem: TaskItem) = oldItem == newItem
}) {
    class Holder(val binding: RowTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(RowTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val (row, status) = getItem(position)
        val task = row.task
        with(holder.binding) {
            title.text = task.title
            info.text = root.context.getString(
                R.string.ui_task_info, row.disciplineName, task.type,
                AcademicDates.format(task.deadline), task.priority.lowercase(), status,
            )
            root.setOnClickListener { open(task.id) }
            done.setOnCheckedChangeListener(null)
            done.isChecked = task.completed
            done.setText(if (task.completed) R.string.ui_concluida_para_reabrir else R.string.ui_marcar_como_concluida)
            done.setOnCheckedChangeListener { _, checked -> toggle(task.id, checked) }
        }
    }
}
