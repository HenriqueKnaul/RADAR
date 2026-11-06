package br.udesc.radar

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import br.udesc.radar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(estadoAnterior: Bundle?) {
        super.onCreate(estadoAnterior)
        enableEdgeToEdge()
        val vinculo = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vinculo.root)

        ViewCompat.setOnApplyWindowInsetsListener(vinculo.root) { conteudo, recuos ->
            val margensSeguras = recuos.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                    or WindowInsetsCompat.Type.ime(),
            )
            conteudo.updatePadding(left = margensSeguras.left, top = margensSeguras.top, right = margensSeguras.right, bottom = margensSeguras.bottom)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(vinculo.root)

        val anfitriaoNavegacao = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navegacao = anfitriaoNavegacao.navController
        val abas = setOf(R.id.painelFragment, R.id.disciplinasFragment, R.id.tarefasFragment, R.id.ajustesFragment)
        vinculo.bottomNav.setupWithNavController(navegacao)
        vinculo.toolbar.setupWithNavController(navegacao, AppBarConfiguration(abas))
        navegacao.addOnDestinationChangedListener { _, destino, _ ->
            val ehAba = destino.id in abas
            vinculo.bottomNav.isVisible = ehAba
            vinculo.headerLogo.isVisible = ehAba
            if (ehAba) {
                vinculo.toolbar.title = null
            } else {
                vinculo.toolbar.setNavigationContentDescription(R.string.ui_voltar)
            }
        }
    }
}
