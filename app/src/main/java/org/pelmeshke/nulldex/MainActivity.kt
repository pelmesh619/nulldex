package org.pelmeshke.nulldex

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import org.pelmeshke.nulldex.databinding.ActivityMainBinding
import org.pelmeshke.nulldex.ui.list.PokemonListFragmentDirections

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupActionBarWithNavController(navController)

        binding.fabFavorites.setOnClickListener {
            startActivity(Intent(this, FavoriteActivity::class.java))
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.pokemonDetailFragment) {
                binding.fabFavorites.hide()
            } else {
                binding.fabFavorites.show()
            }
        }

        intent.getStringExtra("pokemon_name")?.let { name ->
            navController.navigate(
                PokemonListFragmentDirections.actionListFragmentToDetailFragment(name)
            )
        }

        binding.root.post {
            val systemBars =
                WindowInsetsCompat.toWindowInsetsCompat(window.decorView.rootWindowInsets)
                    .getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(0, systemBars.top, 0, systemBars.bottom)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra("pokemon_name")?.let { name ->
            navController.navigate(
                PokemonListFragmentDirections.actionListFragmentToDetailFragment(name)
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}