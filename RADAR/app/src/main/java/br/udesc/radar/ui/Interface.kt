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

fun <T> Fragment.observar(fluxo: Flow<T>, exibir: (T) -> Unit) {
    val donoDaTela = viewLifecycleOwner
    donoDaTela.lifecycleScope.launch {
        donoDaTela.repeatOnLifecycle(Lifecycle.State.STARTED) {
            fluxo.collect(exibir)
        }
    }
}

fun Fragment.avisar(texto: String) {
    Toast.makeText(requireContext(), texto, Toast.LENGTH_SHORT).show()
}

fun RecyclerView.configurarLista(registros: RecyclerView.Adapter<*>) {
    layoutManager = LinearLayoutManager(context)
    adapter = registros
}

fun TextView.exibirMensagem(texto: String?) {
    this.text = texto.orEmpty()
    isVisible = texto != null
}

fun Spinner.definirOpcoes(rotulos: List<String>, selecionado: Int = 0) {
    adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, rotulos).also {
        it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }
    setSelection(selecionado.coerceIn(0, rotulos.lastIndex.coerceAtLeast(0)))
}

fun Spinner.aoSelecionar(acao: (Int) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(pai: AdapterView<*>, visao: View?, posicao: Int, id: Long) {
            acao(posicao)
        }

        override fun onNothingSelected(pai: AdapterView<*>) = Unit
    }
}
