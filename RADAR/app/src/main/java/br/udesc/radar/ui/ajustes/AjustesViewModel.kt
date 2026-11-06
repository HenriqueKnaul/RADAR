package br.udesc.radar.ui.ajustes

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel

class AjustesViewModel(private val preferences: SharedPreferences) : ViewModel() {
    val shakeEnabled get() = preferences.getBoolean("shake_enabled", true)

    fun setShakeEnabled(enabled: Boolean) {
        preferences.edit { putBoolean("shake_enabled", enabled) }
    }
}
