package br.udesc.radar.ui

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.udesc.radar.RadarApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

val Fragment.radar get() = requireActivity().application as RadarApplication

fun <T> Fragment.observe(flow: Flow<T>, render: (T) -> Unit) {
    val owner = viewLifecycleOwner
    owner.lifecycleScope.launch {
        owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(render)
        }
    }
}

fun Fragment.message(text: String) {
    Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
}

fun RecyclerView.showRows(rows: RecyclerView.Adapter<*>) {
    layoutManager = LinearLayoutManager(context)
    adapter = rows
}

fun TextView.showMessage(text: String?) {
    this.text = text.orEmpty()
    isVisible = text != null
}

fun Spinner.options(labels: List<String>, selected: Int = 0) {
    adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, labels).also {
        it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }
    setSelection(selected.coerceIn(0, labels.lastIndex.coerceAtLeast(0)))
}

fun Spinner.onSelected(action: (Int) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            action(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>) = Unit
    }
}
