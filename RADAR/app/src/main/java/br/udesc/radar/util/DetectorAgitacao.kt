package br.udesc.radar.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class DetectorAgitacao(contexto: Context, private val aoAgitar: () -> Unit) : SensorEventListener {
    private val gerenciador = contexto.getSystemService(SensorManager::class.java)
    private val sensor = gerenciador.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gesto = GestoAgitacao()

    fun iniciar(): Boolean {
        val acelerometro = sensor ?: return false
        return gerenciador.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_UI)
    }

    fun parar() {
        gerenciador.unregisterListener(this)
        gesto.limparPicos()
    }

    override fun onSensorChanged(evento: SensorEvent) {
        if (gesto.detectar(evento.timestamp / 1_000_000, evento.values)) aoAgitar()
    }

    override fun onAccuracyChanged(sensor: Sensor?, precisao: Int) = Unit
}

internal class GestoAgitacao {
    private var primeiroPico: Long? = null
    private var acimaDoLimite = false
    private var ultimaAgitacao: Long? = null

    fun detectar(agora: Long, aceleracao: FloatArray): Boolean {
        val (x, y, z) = aceleracao
        val limite = 2.1f * SensorManager.GRAVITY_EARTH
        val acima = x * x + y * y + z * z >= limite * limite
        val novoPico = acima && !acimaDoLimite
        acimaDoLimite = acima
        if (!novoPico || ultimaAgitacao?.let { agora - it < 2_000 } == true) return false

        val anterior = primeiroPico
        if (anterior != null && agora - anterior in 80L..700L) {
            ultimaAgitacao = agora
            primeiroPico = null
            return true
        }
        if (anterior == null || agora - anterior > 700) primeiroPico = agora
        return false
    }

    fun limparPicos() {
        primeiroPico = null
        acimaDoLimite = false
    }
}
