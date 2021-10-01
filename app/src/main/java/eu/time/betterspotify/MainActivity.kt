package eu.time.betterspotify

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.gson.Gson

import eu.time.betterspotify.recycleview.adapter.PlaylistRecycleViewAdapter
import eu.time.betterspotify.recycleview.adapter.TrackRecycleViewAdapter
import eu.time.betterspotify.spotify.SpotifyApi
import eu.time.betterspotify.spotify.SpotifyPlayer
import eu.time.betterspotify.spotify.data.TokenResult
import eu.time.betterspotify.spotify.data.results.playlist.PlaylistItem
import eu.time.betterspotify.spotify.data.results.playlist.Playlists
import eu.time.betterspotify.spotify.data.types.Playlist

class MainActivity : AppCompatActivity() {
    private val playlistList = mutableListOf<Playlist>()
    private val trackList = mutableListOf<PlaylistItem>()
    private lateinit var adapter: PlaylistRecycleViewAdapter

    private lateinit var spotifyPlayer: SpotifyPlayer

    private var activeColor = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spotifyPlayer = SpotifyPlayer.getInstance(this)

        initRecycleView()

        activeColor = Color.valueOf(getColor(R.color.green_900)).toArgb()

        findViewById<ImageButton>(R.id.btnSearch).setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initRecycleView() {
        val rvPlaylistList = findViewById<RecyclerView>(R.id.rvPlaylistList)

        adapter = PlaylistRecycleViewAdapter(playlistList, spotifyPlayer) { playlistId ->
            SpotifyApi.getInstance().getPlaylistTracks(this, "https://api.spotify.com/v1/playlists/$playlistId/tracks", { result ->
                trackList.clear()
                trackList.addAll(result)
                rvPlaylistList.adapter = TrackRecycleViewAdapter(trackList, spotifyPlayer)
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

        val token = getToken()

        if (token != null) {
            SpotifyApi.getInstance().initialize(token)

            loadPlaylists()

            MiniPlayerController.getInstance().start(this)
        } else {
            startLoginActivity()
        }
    }

    private fun getToken(): TokenResult? {
        val sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val accessToken = sharedPref.getString(getString(R.string.spotify_access_token), "").toString()
        val refreshToken = sharedPref.getString(getString(R.string.spotify_refresh_token), "").toString()

        if (accessToken.isNotBlank() && refreshToken.isNotBlank()) {
            return TokenResult(accessToken, refreshToken)
        }

        return null
    }

    private fun loadPlaylists() {
        SpotifyApi.getInstance().getPlaylists(this, { response ->
            val playlists = Gson().fromJson(response, Playlists::class.java)
            updateRecycleView(playlists.items)
        })
    }

    override fun onPause() {
        super.onPause()
        MiniPlayerController.getInstance().stop()

        if (::spotifyPlayer.isInitialized) {
            spotifyPlayer.disconnect()
        }
    }

    override fun onStop() {
        super.onStop()

//        if (::spotifyPlayer.isInitialized) {
//            spotifyPlayer.disconnect()
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        if (::spotifyPlayer.isInitialized) {
//            spotifyPlayer.disconnect()
//        }
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