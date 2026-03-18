package org.pelmeshke.nulldex.ui.detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import org.pelmeshke.nulldex.databinding.FragmentPokemonDetailBinding

class PokemonDetailFragment : Fragment() {
    private var _binding: FragmentPokemonDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PokemonDetailViewModel by viewModels()
    private val args: PokemonDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPokemonDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadPokemon(args.pokemonName)

        viewModel.pokemon.observe(viewLifecycleOwner) { pokemon ->
            binding.tvName.text = pokemon.name.replaceFirstChar { it.uppercase() }
            binding.tvTypes.text = pokemon.types.joinToString(", ") { it.type.name }
            binding.tvHeight.text = "Height: ${pokemon.height / 10.0} m"
            binding.tvWeight.text = "Weight: ${pokemon.weight / 10.0} kg"

            Glide.with(this)
                .load(pokemon.sprites.frontDefault)
                .into(binding.ivSprite)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
