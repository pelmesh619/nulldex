package org.pelmeshke.nulldex.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import org.pelmeshke.nulldex.ui.favorites.FavoriteActivity
import org.pelmeshke.nulldex.R
import org.pelmeshke.nulldex.databinding.ActivityMainBinding
import org.pelmeshke.nulldex.ui.list.PokemonListFragmentDirections
import org.pelmeshke.nulldex.ui.list.PokemonListViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val viewModel: PokemonListViewModel by viewModels()

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

//        binding.fabRefreshPokemonList.setOnClickListener {
//            viewModel.refresh()
//        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.pokemonDetailFragment) {
                binding.fabFavorites.hide()
            } else {
                binding.fabFavorites.show()
            }
        }

        intent.getStringExtra("pokemon_name")?.let { name ->
            navController.navigate(
                PokemonListFragmentDirections.Companion.actionListFragmentToDetailFragment(name)
            )
        }

        binding.root.post {
            WindowCompat.setDecorFitsSystemWindows(window, false)

            val insets = WindowInsetsCompat.toWindowInsetsCompat(window.decorView.rootWindowInsets)

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())

            binding.root.setPadding(
                maxOf(systemBars.left, cutout.left),
                systemBars.top,
                maxOf(systemBars.right, cutout.right),
                systemBars.bottom
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra("pokemon_name")?.let { name ->
            navController.navigate(
                PokemonListFragmentDirections.Companion.actionListFragmentToDetailFragment(name)
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}