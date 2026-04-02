package org.pelmeshke.nulldex.ui.list

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.pelmeshke.nulldex.ui.detail.PokemonDetailActivity
import org.pelmeshke.nulldex.R
import org.pelmeshke.nulldex.databinding.FragmentPokemonListBinding
import org.pelmeshke.nulldex.ui.detail.PokemonDetailFragment


class PokemonListFragment : Fragment() {
    private var _binding: FragmentPokemonListBinding? = null
    private val binding get() = _binding!!

    private val viewModel by lazy {
        ViewModelProvider(requireActivity())[PokemonListViewModel::class.java]
    }

    private lateinit var adapter: PokemonAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPokemonListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PokemonAdapter { entry ->
            val container = view.findViewById<View>(R.id.detailContainer)
            if (container != null && isLandscape()) {
                val fragment = PokemonDetailFragment.newInstance(entry.name, isTwoPane = true)
                childFragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, fragment)
                    .commit()
            } else {
                val intent = Intent(activity?.baseContext, PokemonDetailActivity::class.java).apply {
                    putExtra("pokemon_name", entry.name)
                }
                startActivity(intent)
            }
        }

        val swipeRefresh = binding.swipeRefresh
        swipeRefresh?.setOnRefreshListener {
            viewModel.refresh()
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { refreshing ->
            swipeRefresh?.isRefreshing = refreshing
        }
        viewModel.isListVisible.observe(viewLifecycleOwner) { isListVisible ->
            binding.recyclerView.isVisible = isListVisible
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

                if (visibleItemCount + firstVisibleItem >= totalItemCount - 5) {
                    viewModel.loadNextPage()
                }
            }
        })

        viewModel.pokemonList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar?.isVisible = loading
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            var isRestoring = false

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_search, menu)
                Log.d("Search", "onCreateMenu, lastQuery: ${viewModel.lastQuery}")

                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView

                if (viewModel.lastQuery.isNotEmpty()) {
                    Log.d("Search", "restoring query: ${viewModel.lastQuery}")
                    isRestoring = true
                    searchItem.expandActionView()
                    searchView.post {
                        searchView.setQuery(viewModel.lastQuery, false)
//                        searchView.clearFocus()
                        isRestoring = false
                        viewModel.search(viewModel.lastQuery)
                    }
                }

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?) = false

                    override fun onQueryTextChange(newText: String?): Boolean {
                        Log.d("Search", "onQueryTextChange: $newText, isRestoring: $isRestoring")
                        if (!isRestoring) {
                            viewModel.search(newText.orEmpty())
                        }
                        return true
                    }
                })
                searchView.setOnCloseListener {
                    isRestoring = false
                    viewModel.lastQuery = ""
                    false
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner)

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.errorLayout.isVisible = true
                binding.recyclerView.isVisible = false
                binding.tvError.text = error
                binding.btnRetry.setOnClickListener {
                    binding.errorLayout.isVisible = false
                    binding.recyclerView.isVisible = true
                    viewModel.clearError()
                    viewModel.loadAllPokemons()
                }
            } else {
                binding.errorLayout.isVisible = false
                binding.recyclerView.isVisible = true
            }
        }
    }

    private fun isLandscape(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
