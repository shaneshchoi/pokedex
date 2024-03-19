package com.example.pokedex

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecyclerViewAdapter(private var pokeList: List<Pokemon>) :
    RecyclerView.Adapter<PokemonViewHolder>() {

    // Function to update the adapter's data with a new list and notify the RecyclerView
    // Create a new ViewHolder instance
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context)
            // Inflate the layout for a single item view
            .inflate(R.layout.list_item_pokemon, parent, false)
        return PokemonViewHolder(view)
    }
    // Bind data to the views in the ViewHolder
    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = pokeList[position]

        // Load the Pokemon image using Glide library and
        // load corresponding images using the url.
        Glide.with(holder.itemView.context)
            .load(getImageUrl(pokemon.url))
            .into(holder.pokemonImage)

        // Set the Pokemon name and the rest content
        // Could be used in the future to modify any other contents but for this
        // midterm, I will leave it empty.
        holder.pokemonName.text = pokemon.name
        holder.pokemonDetails.text = "Other information could be added"
    }

    // Inserting id value which is the unique number included in the URL
    // the number is located in between slashes, so it can extract the number using split
    // and then call the github repository with corresponding unique number will bring the image
    // of the Pokemon
    private fun getImageUrl(pokemonUrl: String): String {
        val id = pokemonUrl.split("/").dropLast(1).last()
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
    }
    // It returns the total number of Pokemon being called
    override fun getItemCount(): Int {
        return pokeList.size
    }
}
