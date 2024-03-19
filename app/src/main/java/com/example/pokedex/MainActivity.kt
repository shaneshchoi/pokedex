package com.example.pokedex

import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //URL : Pokeapi to bring pokemon information
        //Also can change Pokedex range by modifying the number "limit={number}"
        val url = "https://pokeapi.co/api/v2/pokemon/?limit=120&offset=0"

        //If there is a connection to a network, execute it directly using URL
        if (isNetworkAvailable()) {
            HttpAsyncTask().execute(url)
            //Else call from the DB
        } else {
            fetchFromDatabase()
        }
    }


    // Function to fetch data from the local database
private fun fetchFromDatabase() {
    val dao = PokemonDatabase.getDatabase(applicationContext).pokemonDao()
    val pokemonEntitiesLiveData = dao.getAllPokemon()
        // Observe changes in the PokemonEntities LiveData from the database
    pokemonEntitiesLiveData.observe(this) { pokemonEntities ->
        pokemonEntities?.let {
            // Convert PokemonEntities to a list of Pokemon and update the RecyclerView
            val pokemonList = it.map { Pokemon(it.name, it.url) }
            updateRecyclerView(pokemonList)
        }
    }
}
    // Function to update the RecyclerView with a list of Pokemon for DB
    private fun updateRecyclerView(pokemonList: List<Pokemon>) {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = RecyclerViewAdapter(pokemonList)
        recyclerView.adapter = adapter
    }
    // Function to check if a network connection is available
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
    // Inner AsyncTask class to perform network operations asynchronously using okhttp3
    // Task includes performing network operations in the background
    private inner class HttpAsyncTask : AsyncTask<String, Void, List<Pokemon>>() {
        private val client = OkHttpClient()

        override fun doInBackground(vararg params: String): List<Pokemon>? {
            val pokeList = mutableListOf<Pokemon>()
            val strUrl = params[0]
                //Error handling
                // Parse JSON response and create a list of Pokemon objects
            try {
                val response: Response = client.newCall(createRequest(strUrl)).execute()
                val result = response.body?.string()
                result?.let {
                    pokeList.addAll(parsePokemonList(it))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return pokeList
        }
        // After fetching data from the network,
override fun onPostExecute(pokeList: List<Pokemon>?) {
    super.onPostExecute(pokeList)
    pokeList?.let {
        // Convert fetched Pokemon objects to PokemonEntity and insert into the local database
        // Since Pokemon number starts from 1, added 1 accordingly
        val pokemonEntities = pokeList.mapIndexed { index, pokemon ->
            PokemonEntity(index + 1, pokemon.name, pokemon.url)
        }

        // Store data in the database using coroutines in the IO context
        CoroutineScope(Dispatchers.IO).launch {
            val dao = PokemonDatabase.getDatabase(applicationContext).pokemonDao()
            dao.insertPokemonList(pokemonEntities)
        }
        //Check if Pokemons are properly being fetched and updating RecyclerView with fetched data
        for (pokemon in it) {
            Log.d(TAG, "Pokemon Name: ${pokemon.name}, link: ${pokemon.url}")
        }
        Log.d(TAG, "Parsed Pokemon List: $it")

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        val adapter = RecyclerViewAdapter(pokeList)
        recyclerView.adapter = adapter
    }
}
        // Function to create a network request
        private fun createRequest(url: String): Request {
            return Request.Builder()
                .url(url)
                .build()
        }

        // Function to parse the JSON response and create a list of Pokemon objects
        @Throws(JSONException::class)
        private fun parsePokemonList(jsonString: String): List<Pokemon> {
            val jsonObject = JSONObject(jsonString)
            val resultsArray = jsonObject.getJSONArray("results")
            val pokeList = mutableListOf<Pokemon>()
            for (i in 0 until resultsArray.length()) {
                val pokemonObject = resultsArray.getJSONObject(i)
                val name = pokemonObject.getString("name")
                val url = pokemonObject.getString("url")
                val poke = Pokemon(name, url)
                pokeList.add(poke)
            }
            return pokeList
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}