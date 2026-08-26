package org.pelmeshke.nulldex.ui.detail

import android.app.Activity.OVERRIDE_TRANSITION_CLOSE
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import org.pelmeshke.nulldex.data.local.FavoritesManager
import org.pelmeshke.nulldex.R
import org.pelmeshke.nulldex.data.model.PokemonUIMapper
import org.pelmeshke.nulldex.databinding.FragmentPokemonDetailBinding
import org.pelmeshke.nulldex.ui.view.PokemonCardView

class PokemonDetailFragment() : Fragment() {
    private var _binding: FragmentPokemonDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PokemonDetailViewModel by viewModels()
    private val args: PokemonDetailFragmentArgs by navArgs()

    private lateinit var renderer: PokemonUIRenderer
    private val analyticsTracker = AnalyticsTrackers.create()
    private lateinit var favoritesManager: FavoritesManager
    private var pokemonName: String = ""

    private val isTwoPane by lazy { arguments?.getBoolean("isTwoPane") ?: false }

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
        } catch (_: Exception) {
            arguments?.getString("pokemonName") ?: return
        }
        renderer = PokemonUIRenderer(
            container = binding.uiContainer,
            actionHandler = PokemonActionHandler { componentId, action ->
                handleComponentAction(componentId, action)
            },
            analyticsTracker = analyticsTracker
        )
        favoritesManager = FavoritesManager(requireContext())
        this.pokemonName = pokemonName

        analyticsTracker.track("pokemon_detail_screen_impression", mapOf("pokemon_name" to pokemonName))

        viewModel.loadPokemon(pokemonName)
        viewModel.loadUIConfig()

        addActionBarIcons(pokemonName)

        viewModel.pokemon.observe(viewLifecycleOwner) { pokemon ->
            val config = viewModel.uiConfig.value ?: return@observe
            val uiModel = PokemonUIMapper.map(pokemon, config, requireContext())
            renderer.render(uiModel)
        }

        viewModel.uiConfig.observe(viewLifecycleOwner) { config ->
            val pokemon = viewModel.pokemon.value ?: return@observe
            val uiModel = PokemonUIMapper.map(pokemon, config, requireContext())
            renderer.render(uiModel)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.errorLayout.isVisible = true
                binding.uiContainer.isVisible = false
                binding.tvError.text = error
                binding.btnRetry.setOnClickListener {
                    binding.errorLayout.isVisible = false
                    binding.uiContainer.isVisible = true
                    viewModel.clearError()
                    viewModel.loadPokemon(pokemonName)
                }
            } else {
                binding.errorLayout.isVisible = false
                binding.uiContainer.isVisible = true
            }
        }

        binding.cardView.swipeDismissListener =
            PokemonCardView.SwipeDismissListener { dismissCard() }
    }

    private fun dismissCard() {
        if (!isAdded) return
        analyticsTracker.track(
            "pokemon_detail_swipe_dismiss",
            mapOf("pokemon_name" to pokemonName)
        )
        if (isTwoPane) {
            parentFragment?.childFragmentManager
                ?.beginTransaction()
                ?.remove(this)
                ?.commit()
            return
        }
        requireActivity().finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requireActivity().overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE,
                0,
                R.anim.fade_out_short
            )
        } else {
            @Suppress("DEPRECATION")
            requireActivity().overridePendingTransition(0, R.anim.fade_out_short)
        }
    }

    companion object {
        fun newInstance(pokemonName: String, isTwoPane: Boolean = false) = PokemonDetailFragment().apply {
            arguments = Bundle().apply {
                putString("pokemonName", pokemonName)
                putBoolean("isTwoPane", isTwoPane)
            }
        }
    }

    private fun addActionBarIcons(pokemonName: String) {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                if (isTwoPane xor !isLandscape()) {
                    menuInflater.inflate(R.menu.menu_detail, menu)
                    updateStarIcon(menu)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_favorite -> {
                        if (favoritesManager.isFavorite(pokemonName)) {
                            favoritesManager.remove(pokemonName)
                            analyticsTracker.track(
                                "favorite_removed",
                                mapOf("pokemon_name" to pokemonName, "source" to "action_bar")
                            )
                        } else {
                            viewModel.pokemon.observe(viewLifecycleOwner) { pokemon ->
                                favoritesManager.add(pokemonName, pokemon.id.toString())
                                analyticsTracker.track(
                                    "favorite_added",
                                    mapOf("pokemon_name" to pokemonName, "source" to "action_bar")
                                )
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
    }

    private fun isLandscape(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    private fun handleComponentAction(componentId: String, action: org.pelmeshke.nulldex.data.model.UIActionConfig) {
        when (action.type) {
            "show_toast" -> {
                val message = action.payloadOrEmpty()["message"] ?: componentId
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                analyticsTracker.track(
                    "component_action_executed",
                    mapOf(
                        "component_id" to componentId,
                        "action_type" to action.type,
                        "pokemon_name" to pokemonName
                    )
                )
            }

            "toggle_favorite" -> {
                if (favoritesManager.isFavorite(pokemonName)) {
                    favoritesManager.remove(pokemonName)
                    analyticsTracker.track(
                        "favorite_removed",
                        mapOf("pokemon_name" to pokemonName, "source" to componentId)
                    )
                } else {
                    viewModel.pokemon.value?.let { pokemon ->
                        favoritesManager.add(pokemonName, pokemon.id.toString())
                        analyticsTracker.track(
                            "favorite_added",
                            mapOf("pokemon_name" to pokemonName, "source" to componentId)
                        )
                    }
                }
                requireActivity().invalidateOptionsMenu()
            }

            else -> {
                analyticsTracker.track(
                    "component_action_ignored",
                    mapOf(
                        "component_id" to componentId,
                        "action_type" to action.type,
                        "pokemon_name" to pokemonName
                    )
                )
            }
        }
    }

    override fun onDestroyView() {
        binding.cardView.swipeDismissListener = null
        binding.cardView.animate()?.cancel()
        super.onDestroyView()
        _binding = null
    }
}
