package org.pelmeshke.nulldex

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import org.pelmeshke.nulldex.data.model.PokemonEntry
import org.pelmeshke.nulldex.databinding.ActivityFavoriteBinding
import org.pelmeshke.nulldex.ui.list.PokemonAdapter

class FavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteBinding
    private lateinit var adapter: PokemonAdapter
    private lateinit var favoritesManager: FavoritesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Favourites"

        favoritesManager = FavoritesManager(this)
        adapter = PokemonAdapter { entry ->
            val intent = Intent(this, PokemonDetailActivity::class.java).apply {
                putExtra("pokemon_name", entry.name)
            }
            startActivity(intent)
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.root.post {
            val systemBars =
                WindowInsetsCompat.toWindowInsetsCompat(window.decorView.rootWindowInsets)
                    .getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(0, systemBars.top, 0, systemBars.bottom)
        }
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }
    private fun loadFavorites() {
        val favorites = favoritesManager.getAll()
        if (favorites.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            adapter.submitList(favorites)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
