package org.pelmeshke.nulldex

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import org.pelmeshke.nulldex.databinding.ActivityPokemonDetailBinding
import org.pelmeshke.nulldex.ui.detail.PokemonDetailFragment

class PokemonDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPokemonDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPokemonDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val pokemonName = intent.getStringExtra("pokemon_name") ?: return

        if (savedInstanceState == null) {
            val fragment = PokemonDetailFragment.newInstance(pokemonName)
            supportFragmentManager.beginTransaction()
                .replace(R.id.detail_container, fragment)
                .commit()
        }

        binding.root.post {
            val systemBars =
                WindowInsetsCompat.toWindowInsetsCompat(window.decorView.rootWindowInsets)
                    .getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(0, systemBars.top, 0, systemBars.bottom)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
