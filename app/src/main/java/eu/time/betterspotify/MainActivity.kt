package eu.time.betterspotify

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector.ConnectionListener
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track
import com.android.volley.AuthFailureError

import com.google.gson.Gson
import eu.time.betterspotify.spotify.data.playlist.Item
import eu.time.betterspotify.spotify.data.playlist.Playlists

import eu.time.betterspotify.recycleview.adapter.PlaylistRecycleViewAdapter
import eu.time.betterspotify.spotify.data.SpotifyApi
import eu.time.betterspotify.spotify.data.SpotifyPlayer


class MainActivity : AppCompatActivity() {
    companion object {
        val CLIENT_ID = "46d14dadfde64caaaf171e15245a9fe6"
        val REDIRECT_URI = "http://localhost/Spotify"
    }

    private val playlistList = mutableListOf<Item>()
    private lateinit var adapter: PlaylistRecycleViewAdapter

    private lateinit var spotifyPlayer: SpotifyPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        initRecycleView()
    }

    private fun initRecycleView() {
        val rvDeviceList = findViewById<RecyclerView>(R.id.rvPlaylistList)

        adapter = PlaylistRecycleViewAdapter(playlistList)

        rvDeviceList.adapter = adapter
        rvDeviceList.layoutManager = LinearLayoutManager(this)
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

    private fun connected() {
        // Play a playlist

        SpotifyApi.getInstance().getPlaylists(this, { response ->
            val playlists = Gson().fromJson(response, Playlists::class.java)
            updateRecycleView(playlists.items)
        })

//        mSpotifyAppRemote.playerApi.play("spotify:track:2aIB1CdRRG7YLBu9hNw9nR")

        // Subscribe to PlayerState
//        mSpotifyAppRemote.getPlayerApi()
//            .subscribeToPlayerState()
//            .setEventCallback { playerState ->
//                val track: Track = playerState.track
//                Log.d("MainActivity", track.name.toString() + " by " + track.artist.name)
//            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecycleView(newData: List<Item>) {
        playlistList.clear()
        playlistList.addAll(newData)
        adapter.notifyDataSetChanged()
    }

    private fun startLoginActivity() {
        val intent = Intent(this, SpotifyAuthenticationActivity::class.java)
        startActivity(intent)
    }
}