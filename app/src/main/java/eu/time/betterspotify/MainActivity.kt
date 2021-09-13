package eu.time.betterspotify

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.gson.Gson
import eu.time.betterspotify.spotify.data.playlist.Playlist
import eu.time.betterspotify.spotify.data.playlist.Playlists

import eu.time.betterspotify.recycleview.adapter.PlaylistRecycleViewAdapter
import eu.time.betterspotify.recycleview.adapter.TrackRecycleViewAdapter
import eu.time.betterspotify.spotify.data.SpotifyApi
import eu.time.betterspotify.spotify.data.SpotifyPlayer
import eu.time.betterspotify.spotify.data.track.Item
import eu.time.betterspotify.spotify.data.track.Tracks


class MainActivity : AppCompatActivity() {
    companion object {
        val CLIENT_ID = "46d14dadfde64caaaf171e15245a9fe6"
        val REDIRECT_URI = "http://localhost/Spotify"
    }

    private val playlistList = mutableListOf<Playlist>()
    private val trackList = mutableListOf<Item>()
    private lateinit var adapter: PlaylistRecycleViewAdapter

    private lateinit var spotifyPlayer: SpotifyPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initRecycleView()
    }

    private fun initRecycleView() {
        val rvPlaylistList = findViewById<RecyclerView>(R.id.rvPlaylistList)

        adapter = PlaylistRecycleViewAdapter(playlistList) { playlistId ->
            SpotifyApi.getInstance().getPlaylistTracks(this, "https://api.spotify.com/v1/playlists/$playlistId/tracks", { result ->
                val tracks = result

                trackList.clear()
                trackList.addAll(tracks)
                rvPlaylistList.adapter = TrackRecycleViewAdapter(trackList)
            })
        }

        rvPlaylistList.adapter = adapter
        rvPlaylistList.layoutManager = LinearLayoutManager(this)
    }

    override fun onBackPressed() {
        initRecycleView()
        updateRecycleView(playlistList.toList())
    }

    override fun onStart() {
        super.onStart()

        val sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val accessToken = sharedPref.getString(getString(R.string.spotify_access_token), "").toString()
        val refreshToken = sharedPref.getString(getString(R.string.spotify_refresh_token), "").toString()

        if (accessToken.isNotBlank() && refreshToken.isNotBlank()) {
            SpotifyApi.getInstance().initalize(accessToken, refreshToken)

            loadSpotify()
        } else {
            startLoginActivity()
        }
    }

    private fun loadSpotify() {
        spotifyPlayer = SpotifyPlayer.getInstance()

        spotifyPlayer.connect(this)

        SpotifyApi.getInstance().getPlaylists(this, { response ->
            val playlists = Gson().fromJson(response, Playlists::class.java)
            updateRecycleView(playlists.items)
        })
    }

    override fun onStop() {
        super.onStop()

        if (::spotifyPlayer.isInitialized) {
            spotifyPlayer.disconnect()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecycleView(newData: List<Playlist>) {
        playlistList.clear()
        playlistList.addAll(newData)
        adapter.notifyDataSetChanged()
    }

    private fun startLoginActivity() {
        val intent = Intent(this, SpotifyAuthenticationActivity::class.java)
        startActivity(intent)
    }
}