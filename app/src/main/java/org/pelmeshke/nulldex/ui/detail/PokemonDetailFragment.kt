package org.pelmeshke.nulldex.ui.detail

import android.app.Activity.OVERRIDE_TRANSITION_CLOSE
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import org.pelmeshke.nulldex.data.local.FavoritesManager
import org.pelmeshke.nulldex.R
import org.pelmeshke.nulldex.databinding.FragmentPokemonDetailBinding
import org.pelmeshke.nulldex.ui.view.PokemonTypeView

class PokemonDetailFragment() : Fragment() {
    private var _binding: FragmentPokemonDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PokemonDetailViewModel by viewModels()
    private val args: PokemonDetailFragmentArgs by navArgs()

    private lateinit var favoritesManager: FavoritesManager

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
        viewModel.loadPokemon(pokemonName)
        favoritesManager = FavoritesManager(requireContext())

        addActionBarIcons(pokemonName)

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

            binding.ivSprite.let {
                Glide.with(this)
                    .load(pokemon.sprites.frontDefault)
                    .into(it)
            }
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

        val swipeView = binding.cardView
        Log.i("SWIPE_TEST","swipeView present")

        val gesture = android.view.GestureDetector(requireContext(), object : android.view.GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent) = true
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (kotlin.math.abs(distanceX) > kotlin.math.abs(distanceY) * 2) return true
                return false
            }
        })

        swipeView.setOnTouchListener(object : View.OnTouchListener {
            private var isDragging: Boolean = false
            private var initialTranslateX: Float = 0.0F
            private var initialTouchX: Float = 0.0F

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                Log.i(
                    "SWIPE_TEST",
                    "card touch: ${event.actionMasked} x=${event.rawX} y=${event.rawY}"
                )
                gesture.onTouchEvent(event)
                v.performClick()

                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        initialTouchX = event.rawX
                        initialTranslateX = v.translationX
                        isDragging = true
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (!isDragging)
                            return false

                        val dx = event.rawX - initialTouchX
                        if (kotlin.math.abs(dx) < 8) return true
                        v.translationX = initialTranslateX + dx
                        val progress =
                            (kotlin.math.abs(v.translationX) / (v.width * 0.5f)).coerceIn(0f, 1f)
                        v.alpha = 1f - progress * 0.5f
                        v.scaleX = 1f - progress * 0.2f
                        v.scaleY = 1f - progress * 0.2f
                        return true
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (!isDragging) return false
                        isDragging = false
                        val currentX = v.translationX
                        val threshold = v.width * 0.4f
                        if (kotlin.math.abs(currentX) > threshold) {
                            val dir = if (currentX > 0) 1 else -1
                            val target = dir * v.width * 1.2f
                            v.animate().translationX(target)
                                .alpha(0f)
                                .setDuration(20)
                                .withEndAction {
                                    try {
                                        if (isTwoPane) {
                                            parentFragment?.childFragmentManager?.beginTransaction()?.remove(this@PokemonDetailFragment)?.commit()
                                        } else {
                                            requireActivity().finish()
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                                requireActivity().overrideActivityTransition(
                                                    OVERRIDE_TRANSITION_CLOSE,
                                                    0, R.anim.fade_out_short
                                                )
                                            } else {
                                                requireActivity().overridePendingTransition(
                                                    0, R.anim.fade_out_short
                                                )
                                            }
                                        }
                                    } catch (e: IllegalStateException) {
                                        Log.e("SWIPE_BACK", e.toString())
                                    }
                                }
                                .start()
                        } else {
                            v.animate()
                                .translationX(0f)
                                .alpha(1f)
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(20).start()
                        }
                        return true
                    }
                }
                return false
            }
        })
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
    }

    private fun isLandscape(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    override fun onDestroyView() {
        binding.cardView.animate()?.cancel()
        super.onDestroyView()
        _binding = null
    }
}
