package eu.time.betterspotify.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.time.betterspotify.controllers.NavigationController
import eu.time.betterspotify.controllers.PlayerController
import eu.time.betterspotify.R

import eu.time.betterspotify.recycleview.adapter.PlaylistRecycleViewAdapter
import eu.time.betterspotify.spotify.data.spotifyApi.SpotifyApi
import eu.time.betterspotify.spotify.SpotifyPlayer
import eu.time.betterspotify.spotify.data.spotifyApi.getUsersPlaylists
import eu.time.betterspotify.spotify.data.types.Playlist
import eu.time.betterspotify.util.NetworkHandler

class LibraryActivity : NavigationBarActivity() {
    companion object {
        fun openLibrary(context: Context) {
            val intent = Intent(context, LibraryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }
    }

    private val playlistList = mutableListOf<Playlist>()
    private lateinit var adapter: PlaylistRecycleViewAdapter

    private lateinit var spotifyPlayer: SpotifyPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        NetworkHandler.registerNetworkCallback(applicationContext)

        spotifyPlayer = SpotifyPlayer.getInstance(this)

        initRecycleView()
    }

    private fun initRecycleView() {
        val rvPlaylistList = findViewById<RecyclerView>(R.id.rvPlaylistList)

        adapter = PlaylistRecycleViewAdapter(playlistList, spotifyPlayer) { playlist, reversed ->
            PlaylistActivity.openPlaylist(this, playlist, reversed)
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
        getUsersPlaylists(this, this::updateRecycleView)
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