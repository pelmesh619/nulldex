package org.pelmeshke.nulldex.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import org.pelmeshke.nulldex.FavoritesManager
import org.pelmeshke.nulldex.R
import org.pelmeshke.nulldex.databinding.FragmentPokemonDetailBinding
import org.pelmeshke.nulldex.ui.PokemonTypeView

class PokemonDetailFragment : Fragment() {
    private var _binding: FragmentPokemonDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PokemonDetailViewModel by viewModels()
    private val args: PokemonDetailFragmentArgs by navArgs()

    private lateinit var favoritesManager: FavoritesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPokemonDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pokemonName = try {
            args.pokemonName
        } catch (e: Exception) {
            arguments?.getString("pokemonName") ?: return
        }
        viewModel.loadPokemon(pokemonName)
        favoritesManager = FavoritesManager(requireContext())

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_detail, menu)
                updateStarIcon(menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_favorite -> {
                        if (favoritesManager.isFavorite(pokemonName)) {
                            favoritesManager.remove(pokemonName)
                        } else {
                            viewModel.pokemon.observe(viewLifecycleOwner) { pokemon ->
                                favoritesManager.add(pokemonName, pokemon.id.toString())
                            }
                        }
                        requireActivity().invalidateOptionsMenu()
                        true
                    }
                    else -> false
                }
            }

            private fun updateStarIcon(menu: Menu) {
                val item = menu.findItem(R.id.action_favorite)
                if (favoritesManager.isFavorite(pokemonName)) {
                    item.setIcon(android.R.drawable.btn_star_big_on)
                } else {
                    item.setIcon(android.R.drawable.btn_star_big_off)
                }
            }
        }, viewLifecycleOwner)

        viewModel.pokemon.observe(viewLifecycleOwner) { pokemon ->
            binding.tvName.text = pokemon.name.replaceFirstChar { it.uppercase() }

            binding.typesContainer.removeAllViews()
            pokemon.types.forEach { typeSlot ->
                val typeView = PokemonTypeView(requireContext()).apply {
                    typeName = typeSlot.type.name
                    layoutParams = LinearLayout.LayoutParams(200, 60).apply {
                        marginEnd = 8
                    }
                }
                binding.typesContainer.addView(typeView)
            }
            binding.tvHeight.text = "Height: ${pokemon.height / 10.0} m"
            binding.tvWeight.text = "Weight: ${pokemon.weight / 10.0} kg"

            Glide.with(this)
                .load(pokemon.sprites.frontDefault)
                .into(binding.ivSprite)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.errorLayout.isVisible = true
                binding.pokemonDetailLayout.isVisible = false
                binding.tvError.text = error
                binding.btnRetry.setOnClickListener {
                    binding.errorLayout.isVisible = false
                    binding.pokemonDetailLayout.isVisible = true
                    viewModel.clearError()
                    viewModel.loadPokemon(pokemonName)
                }
            } else {
                binding.errorLayout.isVisible = false
                binding.pokemonDetailLayout.isVisible = true
            }
        }
    }

    companion object {
        fun newInstance(pokemonName: String) = PokemonDetailFragment().apply {
            arguments = Bundle().apply {
                putString("pokemonName", pokemonName)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
