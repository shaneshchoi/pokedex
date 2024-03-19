package com.example.pokedex

class Pokemon(val name: String, val url: String) {

    override fun toString(): String {
        return "Pokemon{name='$name', link='$url'}"
    }
}