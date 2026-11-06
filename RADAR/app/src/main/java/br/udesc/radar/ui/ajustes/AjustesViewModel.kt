package br.udesc.radar.ui.ajustes

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel

class AjustesViewModel(private val preferencias: SharedPreferences) : ViewModel() {
    val agitacaoAtiva get() = preferencias.getBoolean("shake_enabled", true)

    fun definirAgitacaoAtiva(ativo: Boolean) {
        preferencias.edit { putBoolean("shake_enabled", ativo) }
    }
}
