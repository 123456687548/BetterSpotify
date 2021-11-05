package eu.time.betterspotify.controllers

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import eu.time.betterspotify.R
import eu.time.betterspotify.spotify.SpotifyApi
import eu.time.betterspotify.spotify.data.types.SearchResult
import java.util.*

class SearchController private constructor() {
    companion object {
        private val INSTANCE = SearchController()

        fun getInstance(): SearchController {
            return INSTANCE
        }
    }

    enum class SearchType(val types: List<String>) {
        BEST_RESULT(listOf("track", "artist")),
        ARTIST(listOf("artist")),
        TRACK(listOf("track")),
        ALBUM(listOf("album")),
        PLAYLIST(listOf("playlist"))
    }

    private var currentSearchType = SearchType.BEST_RESULT
    private var searchQuery = ""

    private lateinit var searchCallback: (SearchResult, SearchType) -> Unit

    fun start(context: Context, searchCallback: (SearchResult, SearchType) -> Unit) {
        this.searchCallback = searchCallback
        initListeners(context)
    }

    private fun initListeners(context: Context) {
        context as Activity

        val etSearchField = context.findViewById<EditText>(R.id.etSearchField)
        val btnSearchBestResult = context.findViewById<Button>(R.id.btnSearchBestResult)
        val btnSearchArtist = context.findViewById<Button>(R.id.btnSearchArtist)
        val btnSearchSongs = context.findViewById<Button>(R.id.btnSearchSongs)
        val btnSearchAlbum = context.findViewById<Button>(R.id.btnSearchAlbum)
        val btnSearchPlaylist = context.findViewById<Button>(R.id.btnSearchPlaylist)

        btnSearchBestResult.setOnClickListener(SearchTypeListener(context, SearchType.BEST_RESULT))
        btnSearchArtist.setOnClickListener(SearchTypeListener(context, SearchType.ARTIST))
        btnSearchSongs.setOnClickListener(SearchTypeListener(context, SearchType.TRACK))
        btnSearchAlbum.setOnClickListener(SearchTypeListener(context, SearchType.ALBUM))
        btnSearchPlaylist.setOnClickListener(SearchTypeListener(context, SearchType.PLAYLIST))

        etSearchField.addTextChangedListener(object : TextWatcher {
            lateinit var lastChangeTimer: Timer

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                if (text.isNotEmpty()) {
                    if (::lastChangeTimer.isInitialized) {
                        lastChangeTimer.cancel()
                    }

                    lastChangeTimer = Timer("lastChangeTimer")
                    lastChangeTimer.schedule(object : TimerTask() {
                        override fun run() {
                            searchQuery = text.toString()
                            search(context)
                        }
                    }, 200)
                }
            }
        })
    }

    private fun changeBtnColors(context: Activity, initalizer: View) {
        val clickedColor = context.getColor(R.color.spotify_green)
        val notClickedColor = context.getColor(R.color.green_900)

        val btnSearchBestResult = context.findViewById<View>(R.id.btnSearchBestResult)
        val btnSearchArtist = context.findViewById<View>(R.id.btnSearchArtist)
        val btnSearchSongs = context.findViewById<View>(R.id.btnSearchSongs)
        val btnSearchAlbum = context.findViewById<View>(R.id.btnSearchAlbum)
        val btnSearchPlaylist = context.findViewById<View>(R.id.btnSearchPlaylist)

        initalizer.setBackgroundColor(clickedColor)

        if (!btnSearchBestResult.equals(initalizer)) {
            btnSearchBestResult.setBackgroundColor(notClickedColor)
        }
        if (!btnSearchArtist.equals(initalizer)) {
            btnSearchArtist.setBackgroundColor(notClickedColor)
        }
        if (!btnSearchSongs.equals(initalizer)) {
            btnSearchSongs.setBackgroundColor(notClickedColor)
        }
        if (!btnSearchAlbum.equals(initalizer)) {
            btnSearchAlbum.setBackgroundColor(notClickedColor)
        }
        if (!btnSearchPlaylist.equals(initalizer)) {
            btnSearchPlaylist.setBackgroundColor(notClickedColor)
        }
    }

    private fun search(context: Context) {
        if (searchQuery.isBlank()) return

        SpotifyApi.getInstance().search(context, searchQuery, currentSearchType.types) { result ->
            searchCallback(result, currentSearchType)
        }
    }

    class SearchTypeListener(val context: Context, val type: SearchType) : View.OnClickListener {
        override fun onClick(view: View) {
            val controller = getInstance()

            controller.currentSearchType = type
            controller.changeBtnColors(context as Activity, view)
            controller.search(view.context)
        }
    }
}