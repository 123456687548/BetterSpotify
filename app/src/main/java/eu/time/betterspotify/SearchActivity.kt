package eu.time.betterspotify

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import eu.time.betterspotify.recycleview.adapter.ArtistRecycleViewAdapter
import eu.time.betterspotify.recycleview.adapter.TrackRecycleViewAdapter
import eu.time.betterspotify.spotify.SpotifyApi
import eu.time.betterspotify.spotify.SpotifyPlayer
import eu.time.betterspotify.spotify.data.results.search.SearchResult
import eu.time.betterspotify.spotify.data.types.Artist
import eu.time.betterspotify.spotify.data.types.Track
import java.util.*
import kotlin.streams.toList

class SearchActivity : AppCompatActivity() {
    private lateinit var spotifyPlayer: SpotifyPlayer
    private lateinit var adapterTracks: TrackRecycleViewAdapter
    private lateinit var adapterArtists: ArtistRecycleViewAdapter
    private val searchResultTracks = mutableListOf<Track>()
    private val searchResultArtists = mutableListOf<Artist>()
    private lateinit var etSearchField: EditText

    private lateinit var rvSearchResultTracks: RecyclerView
    private lateinit var rvSearchResultArtists: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        spotifyPlayer = SpotifyPlayer.getInstance(this)

        initRecycleView()

        etSearchField = findViewById(R.id.etSearchField)

        val context = this.applicationContext

        etSearchField.addTextChangedListener(object : TextWatcher {
            lateinit var lastChangeTimer: Timer

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            //todo search types wie in der app
            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                if (text.isNotEmpty()) {
                    if (::lastChangeTimer.isInitialized) {
                        lastChangeTimer.cancel()
                    }

                    lastChangeTimer = Timer("lastChangeTimer")
                    lastChangeTimer.schedule(object : TimerTask() {
                        override fun run() {
                            SpotifyApi.getInstance().search(context, text.toString(), listOf("track", "artist")) { result ->
                                val searchResult = Gson().fromJson(result, SearchResult::class.java)

                                updateRecycleView(searchResult)
                                changeViewVisibility()
                            }
                        }
                    }, 200)
                }
            }
        })
    }

    private fun focusSearchField() {
        etSearchField.requestFocus()
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(etSearchField, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onStart() {
        super.onStart()

//        changeViewVisibility()

        PlayerController.getInstance().start(this)

        focusSearchField()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        PlayerController.getInstance().stop()
    }

    private fun initRecycleView() {
        val rvSearchResultTracks = findViewById<RecyclerView>(R.id.rvSearchResultTracks)
        val rvSearchResultArtists = findViewById<RecyclerView>(R.id.rvSearchResultArtists)

        adapterTracks = TrackRecycleViewAdapter(searchResultTracks, spotifyPlayer)

        rvSearchResultTracks.adapter = adapterTracks
        rvSearchResultTracks.layoutManager = LinearLayoutManager(this)

        adapterArtists = ArtistRecycleViewAdapter(searchResultArtists, spotifyPlayer)

        rvSearchResultArtists.adapter = adapterArtists
        rvSearchResultArtists.layoutManager = LinearLayoutManager(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecycleView(newData: SearchResult) {
        searchResultTracks.clear()
        searchResultTracks.addAll(newData.tracks.items)
        adapterTracks.notifyDataSetChanged()

        searchResultArtists.clear()
        searchResultArtists.addAll(newData.artists.items.stream().limit(2).toList())
        adapterArtists.notifyDataSetChanged()
    }

    private fun changeViewVisibility() {
        val rvSearchResultTracks = findViewById<RecyclerView>(R.id.rvSearchResultTracks)
        val rvSearchResultArtists = findViewById<RecyclerView>(R.id.rvSearchResultArtists)
        val tvTitleTrack = findViewById<TextView>(R.id.tvTitleTrack)
        val tvTitleArtist = findViewById<TextView>(R.id.tvTitleArtist)

        if (searchResultTracks.isNotEmpty()) {
            tvTitleTrack.visibility = View.VISIBLE
            rvSearchResultTracks.visibility = View.VISIBLE
        } else {
            tvTitleTrack.visibility = View.INVISIBLE
            rvSearchResultTracks.visibility = View.INVISIBLE
        }
        if (searchResultArtists.isNotEmpty()) {
            tvTitleArtist.visibility = View.VISIBLE
            rvSearchResultArtists.visibility = View.VISIBLE
        } else {
            tvTitleArtist.visibility = View.INVISIBLE
            rvSearchResultArtists.visibility = View.INVISIBLE
        }
    }
}