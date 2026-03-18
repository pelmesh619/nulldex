package org.pelmeshke.nulldex.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.pelmeshke.nulldex.data.model.PokemonEntry
import org.pelmeshke.nulldex.databinding.ItemPokemonBinding

class PokemonAdapter(
    private val onClick: (PokemonEntry) -> Unit
) : RecyclerView.Adapter<PokemonAdapter.ViewHolder>() {

    private var items: List<PokemonEntry> = emptyList()

    fun submitList(list: List<PokemonEntry>) {
        items = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemPokemonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: PokemonEntry) {
            binding.tvName.text = entry.name

            val id = entry.url.trimEnd('/').split("/").last()
            val spriteUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"

            Glide.with(binding.ivSprite)
                .load(spriteUrl)
                .into(binding.ivSprite)

            binding.root.setOnClickListener { onClick(entry) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemPokemonBinding.inflate(LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount() = items.size
}
