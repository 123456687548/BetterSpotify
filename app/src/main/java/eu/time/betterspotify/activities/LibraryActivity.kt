package eu.time.betterspotify.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.time.betterspotify.controllers.NavigationController
import eu.time.betterspotify.controllers.PlayerController
import eu.time.betterspotify.R

import eu.time.betterspotify.recycleview.adapter.PlaylistRecycleViewAdapter
import eu.time.betterspotify.spotify.SpotifyApi
import eu.time.betterspotify.spotify.SpotifyPlayer
import eu.time.betterspotify.spotify.data.types.Playlist

class LibraryActivity : NavigationBarActivity() {
    private val playlistList = mutableListOf<Playlist>()
    private lateinit var adapter: PlaylistRecycleViewAdapter

    private lateinit var spotifyPlayer: SpotifyPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        spotifyPlayer = SpotifyPlayer.getInstance(this)

        initRecycleView()
    }

    private fun initRecycleView() {
        val rvPlaylistList = findViewById<RecyclerView>(R.id.rvPlaylistList)

        adapter = PlaylistRecycleViewAdapter(playlistList, spotifyPlayer) { playlist ->
            PlaylistActivity.openPlaylist(this, playlist)
        }

        rvPlaylistList.adapter = adapter
        rvPlaylistList.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()

        SpotifyApi.getInstance().initialize(this) {
            loadPlaylists()

            PlayerController.getInstance().start(this)
        }
    }

    private fun loadPlaylists() {
        SpotifyApi.getInstance().getPlaylists(this, { result ->
            updateRecycleView(result.items)
        })
    }

    override fun onPause() {
        super.onPause()
        PlayerController.getInstance().stop()

        if (::spotifyPlayer.isInitialized) {
            spotifyPlayer.disconnect()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecycleView(newData: List<Playlist>) {
        playlistList.clear()
        playlistList.addAll(newData)
        playlistList.add(Playlist.savedTracksPlaylist)
        adapter.notifyDataSetChanged()
    }

    override fun getCurrentPage(): NavigationController.Page = NavigationController.Page.LIBRARY
}