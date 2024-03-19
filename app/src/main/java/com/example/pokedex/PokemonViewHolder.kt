package com.example.pokedex

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

    //PokemonViewHolder that holds image, name, and details
class PokemonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val pokemonImage: ImageView = itemView.findViewById(R.id.pokemonImage)
    val pokemonName: TextView = itemView.findViewById(R.id.pokemonName)
    val pokemonDetails: TextView = itemView.findViewById(R.id.pokemonDetails)
}
