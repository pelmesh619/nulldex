package org.pelmeshke.nulldex.ui.favorites

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import org.pelmeshke.nulldex.ui.detail.PokemonDetailActivity
import org.pelmeshke.nulldex.databinding.ActivityFavoriteBinding
import org.pelmeshke.nulldex.ui.list.PokemonAdapter

class FavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteBinding
    private lateinit var adapter: PokemonAdapter
    private val viewModel: FavoriteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Favourites"

        adapter = PokemonAdapter { entry ->
            val intent = Intent(this, PokemonDetailActivity::class.java).apply {
                putExtra("pokemon_name", entry.name)
            }
            startActivity(intent)
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.favorites.observe(this) { favorites ->
            if (favorites.isEmpty()) {
                binding.tvEmpty.isVisible = true
                binding.recyclerView.isVisible = false
            } else {
                binding.tvEmpty.isVisible = false
                binding.recyclerView.isVisible = true
                adapter.submitList(favorites)
            }
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
        viewModel.loadFavorites()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}