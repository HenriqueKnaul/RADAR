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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { root, insets ->
            val safe = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                    or WindowInsetsCompat.Type.ime(),
            )
            root.updatePadding(left = safe.left, top = safe.top, right = safe.right, bottom = safe.bottom)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(binding.root)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val nav = navHost.navController
        val tabs = setOf(R.id.painelFragment, R.id.disciplinasFragment, R.id.tarefasFragment, R.id.ajustesFragment)
        binding.bottomNav.setupWithNavController(nav)
        binding.toolbar.setupWithNavController(nav, AppBarConfiguration(tabs))
        nav.addOnDestinationChangedListener { _, destination, _ ->
            val isTab = destination.id in tabs
            binding.bottomNav.isVisible = isTab
            binding.headerLogo.isVisible = isTab
            if (isTab) {
                binding.toolbar.title = null
            } else {
                binding.toolbar.setNavigationContentDescription(R.string.ui_voltar)
            }
        }
    }
}
