package br.udesc.radar

import androidx.test.ext.junit.runners.AndroidJUnit4
import br.udesc.radar.util.GestoAgitacao
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GestoAgitacaoTest {
    private val repouso = floatArrayOf(0f, 0f, 9.8f)
    private val pico = floatArrayOf(26f, 0f, 9.8f)

    @Test
    fun exigeDoisPicosEIgnoraAgitacoesRepetidas() {
        val gesto = GestoAgitacao()
        assertFalse(gesto.detectar(0, repouso))
        assertFalse(gesto.detectar(100, pico))
        assertFalse(gesto.detectar(180, pico))
        assertFalse(gesto.detectar(200, repouso))
        assertTrue(gesto.detectar(300, pico))
        assertFalse(gesto.detectar(400, repouso))
        assertFalse(gesto.detectar(500, pico))
        assertFalse(gesto.detectar(600, repouso))
        assertFalse(gesto.detectar(700, pico))
        assertFalse(gesto.detectar(2_400, repouso))
        assertFalse(gesto.detectar(2_500, pico))
        assertFalse(gesto.detectar(2_600, repouso))
        assertTrue(gesto.detectar(2_700, pico))
    }

    @Test
    fun descartaPicoCapturadoAntesDeSairDoPainel() {
        val gesto = GestoAgitacao()
        assertFalse(gesto.detectar(100, pico))
        gesto.limparPicos()
        assertFalse(gesto.detectar(350, pico))
    }
}
