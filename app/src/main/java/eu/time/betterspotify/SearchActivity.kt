package eu.time.betterspotify

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.time.betterspotify.recycleview.adapter.AlbumRecycleViewAdapter
import eu.time.betterspotify.recycleview.adapter.ArtistRecycleViewAdapter
import eu.time.betterspotify.recycleview.adapter.PlaylistRecycleViewAdapter
import eu.time.betterspotify.recycleview.adapter.TrackRecycleViewAdapter
import eu.time.betterspotify.spotify.SpotifyApi
import eu.time.betterspotify.spotify.SpotifyPlayer
import eu.time.betterspotify.spotify.data.results.search.SearchResult
import eu.time.betterspotify.spotify.data.types.Album
import eu.time.betterspotify.spotify.data.types.Artist
import eu.time.betterspotify.spotify.data.types.Playlist
import eu.time.betterspotify.spotify.data.types.Track
import kotlin.streams.toList

class SearchActivity : NavigationBarActivity() {
    private lateinit var spotifyPlayer: SpotifyPlayer
    private lateinit var adapterTracks: TrackRecycleViewAdapter
    private lateinit var adapterArtists: ArtistRecycleViewAdapter
    private lateinit var adapterAlbum: AlbumRecycleViewAdapter
    private lateinit var adapterPlaylist: PlaylistRecycleViewAdapter

    private val searchResultTracks = mutableListOf<Track>()
    private val searchResultArtists = mutableListOf<Artist>()
    private val searchResultAlbum = mutableListOf<Album>()
    private val searchResultPlaylist = mutableListOf<Playlist>()

    private lateinit var etSearchField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        spotifyPlayer = SpotifyPlayer.getInstance(this)

        initRecycleView()

        SearchController.getInstance().start(this) { searchResult, searchType ->
            changeViewVisibility(searchType)
            updateRecycleView(searchResult, searchType)
            changeTopTitle(searchType)
        }

        etSearchField = findViewById(R.id.etSearchField)
    }

    private fun focusSearchField() {
        etSearchField.requestFocus()
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(etSearchField, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onStart() {
        super.onStart()
        SpotifyApi.getInstance().initialize(this) {
            PlayerController.getInstance().start(this)

            focusSearchField()
        }
    }

    override fun onPause() {
        super.onPause()
        PlayerController.getInstance().stop()
    }

    private fun initRecycleView() {
        val rvSearchResultTop = findViewById<RecyclerView>(R.id.rvSearchResultTop)
        val rvSearchResultBottom = findViewById<RecyclerView>(R.id.rvSearchResultBottom)

        adapterTracks = TrackRecycleViewAdapter(searchResultTracks, spotifyPlayer)

        rvSearchResultTop.adapter = adapterTracks
        rvSearchResultTop.layoutManager = LinearLayoutManager(this)

        adapterArtists = ArtistRecycleViewAdapter(searchResultArtists, spotifyPlayer)

        rvSearchResultBottom.adapter = adapterArtists
        rvSearchResultBottom.layoutManager = LinearLayoutManager(this)

        adapterAlbum = AlbumRecycleViewAdapter(searchResultAlbum, spotifyPlayer)

        adapterPlaylist = PlaylistRecycleViewAdapter(searchResultPlaylist, spotifyPlayer)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecycleView(newData: SearchResult, searchType: SearchController.SearchType) {
        val rvSearchResultTop = findViewById<RecyclerView>(R.id.rvSearchResultTop)
        val rvSearchResultBottom = findViewById<RecyclerView>(R.id.rvSearchResultBottom)

        when (searchType) {
            SearchController.SearchType.BEST_RESULT -> {
                rvSearchResultTop.adapter = adapterTracks
                rvSearchResultBottom.adapter = adapterArtists

                searchResultTracks.clear()
                searchResultTracks.addAll(newData.tracks.items)
                adapterTracks.notifyDataSetChanged()

                searchResultArtists.clear()
                searchResultArtists.addAll(newData.artists.items.stream().limit(2).toList())
                adapterArtists.notifyDataSetChanged()
            }
            SearchController.SearchType.ARTIST -> {
                rvSearchResultTop.adapter = adapterArtists

                searchResultArtists.clear()
                searchResultArtists.addAll(newData.artists.items)
                adapterArtists.notifyDataSetChanged()
            }
            SearchController.SearchType.TRACK -> {
                rvSearchResultTop.adapter = adapterTracks

                searchResultTracks.clear()
                searchResultTracks.addAll(newData.tracks.items)
                adapterTracks.notifyDataSetChanged()
            }
            SearchController.SearchType.ALBUM -> {
                rvSearchResultTop.adapter = adapterAlbum

                searchResultAlbum.clear()
                searchResultAlbum.addAll(newData.albums.items)
                adapterAlbum.notifyDataSetChanged()
            }
            SearchController.SearchType.PLAYLIST -> {
                rvSearchResultTop.adapter = adapterPlaylist

                searchResultPlaylist.clear()
                searchResultPlaylist.addAll(newData.playlists.items)
                adapterPlaylist.notifyDataSetChanged()
            }
        }
    }

    private fun changeViewVisibility(searchType: SearchController.SearchType) {
        val rvSearchResultTop = findViewById<RecyclerView>(R.id.rvSearchResultTop)
        val rvSearchResultBottom = findViewById<RecyclerView>(R.id.rvSearchResultBottom)
        val tvSearchTitleTop = findViewById<TextView>(R.id.tvSearchTitleTop)
        val tvSearchTitleBottom = findViewById<TextView>(R.id.tvSearchTitleBottom)

        when (searchType) {
            SearchController.SearchType.BEST_RESULT -> {
                rvSearchResultTop.visibility = View.VISIBLE
                rvSearchResultBottom.visibility = View.VISIBLE
                tvSearchTitleTop.visibility = View.VISIBLE
                tvSearchTitleBottom.visibility = View.VISIBLE
            }
            else -> {
                tvSearchTitleBottom.visibility = View.GONE
                rvSearchResultBottom.visibility = View.GONE
            }
        }
    }

    private fun changeTopTitle(searchType: SearchController.SearchType) {
        val tvSearchTitleTop = findViewById<TextView>(R.id.tvSearchTitleTop)

        when (searchType) {
            SearchController.SearchType.BEST_RESULT -> tvSearchTitleTop.text = getString(R.string.tracks)
            SearchController.SearchType.ARTIST -> tvSearchTitleTop.text = getString(R.string.artists)
            SearchController.SearchType.TRACK -> tvSearchTitleTop.text = getString(R.string.tracks)
            SearchController.SearchType.ALBUM -> tvSearchTitleTop.text = getString(R.string.albums)
            SearchController.SearchType.PLAYLIST -> tvSearchTitleTop.text = getString(R.string.playlists)
        }
    }

    override fun getCurrentPage(): NavigationController.Page = NavigationController.Page.SEARCH
}