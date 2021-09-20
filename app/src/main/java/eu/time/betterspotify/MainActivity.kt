package eu.time.betterspotify

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewStub
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.gson.Gson
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import eu.time.betterspotify.spotify.data.playlist.Playlist
import eu.time.betterspotify.spotify.data.playlist.Playlists

import eu.time.betterspotify.recycleview.adapter.PlaylistRecycleViewAdapter
import eu.time.betterspotify.recycleview.adapter.TrackRecycleViewAdapter
import eu.time.betterspotify.spotify.data.SpotifyApi
import eu.time.betterspotify.spotify.data.SpotifyPlayer
import eu.time.betterspotify.spotify.data.track.Item
import eu.time.betterspotify.util.loadImageFromUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    companion object {
        val CLIENT_ID = "46d14dadfde64caaaf171e15245a9fe6"
        val REDIRECT_URI = "http://localhost/Spotify"
    }

    private val playlistList = mutableListOf<Playlist>()
    private val trackList = mutableListOf<Item>()
    private lateinit var adapter: PlaylistRecycleViewAdapter

    private lateinit var spotifyPlayer: SpotifyPlayer

    private lateinit var playerView: ViewStub

    private lateinit var llPlayerInfo: LinearLayout
    private lateinit var btnRepeat: ImageButton
    private lateinit var btnShuffle: ImageButton
    private lateinit var btnPlay: ImageButton
    private lateinit var ivPlayerCover: ImageView
    private lateinit var tvPlayerTitle: TextView
    private lateinit var tvPlayerArtist: TextView
    private lateinit var pbProgress: ProgressBar

    private var activeColor = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.stubPlayer)
        playerView.inflate()

        llPlayerInfo = findViewById(R.id.llPlayerInfo)
        btnRepeat = findViewById(R.id.btnRepeat)
        btnShuffle = findViewById(R.id.btnShuffle)
        btnPlay = findViewById(R.id.btnPlay)
        ivPlayerCover = findViewById(R.id.ivPlayerCover)
        tvPlayerTitle = findViewById(R.id.tvPlayerTitle)
        tvPlayerArtist = findViewById(R.id.tvPlayerArtist)
        pbProgress = findViewById(R.id.pbProgress)

        initRecycleView()

        activeColor = Color.valueOf(getColor(R.color.green_900)).toArgb()

        findViewById<ImageButton>(R.id.btnSearch).setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initRecycleView() {
        val rvPlaylistList = findViewById<RecyclerView>(R.id.rvPlaylistList)

        adapter = PlaylistRecycleViewAdapter(playlistList) { playlistId ->
            SpotifyApi.getInstance().getPlaylistTracks(this, "https://api.spotify.com/v1/playlists/$playlistId/tracks", { result ->
                trackList.clear()
                trackList.addAll(result)
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

            initPlayer()
        } else {
            startLoginActivity()
        }
    }

    private fun initPlayer() {
        var active = true

        llPlayerInfo.setOnClickListener {
            active = false
            val intent = Intent(this, BigPlayerActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up, 0)
        }

        btnRepeat.setOnClickListener {
            val playerApi = SpotifyPlayer.getInstance().getRemote()?.playerApi

            playerApi?.playerState?.setResultCallback {
                when (it.playbackOptions.repeatMode) {
                    0 -> { //is no repeat -> repeat all
                        playerApi.setRepeat(2)
                        btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_48)
                        btnRepeat.setColorFilter(activeColor)
                    }
                    1 -> { // is repeat one -> no repeat
                        playerApi.setRepeat(0)
                        btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_48)
                        btnRepeat.clearColorFilter()
                    }
                    2 -> { // is repeat all -> repeat one
                        playerApi.setRepeat(1)
                        btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_one_48)
                    }
                }
            }
        }

        btnShuffle.setOnClickListener {
            val playerApi = SpotifyPlayer.getInstance().getRemote()?.playerApi

            playerApi?.playerState?.setResultCallback {
                if (it.playbackOptions.isShuffling) {
                    playerApi.setShuffle(false)
                    btnShuffle.clearColorFilter()
                } else {
                    playerApi.setShuffle(true)
                    btnShuffle.setColorFilter(activeColor)
                }
            }
        }

        btnPlay.setOnClickListener {
            val playerApi = SpotifyPlayer.getInstance().getRemote()?.playerApi

            playerApi?.playerState?.setResultCallback {
                if (it.isPaused) {
                    playerApi.resume()
                    btnPlay.setImageResource(R.drawable.ic_baseline_pause_48)
                } else {
                    playerApi.pause()
                    btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_48)
                }
            }
        }

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                CoroutineScope(Dispatchers.IO).launch {
                    SpotifyPlayer.getInstance().getRemote()?.playerApi?.playerState?.setResultCallback { playerState ->
                        updateTinyPlayer(playerState)
                    }
                }

                if (active) {
                    mainHandler.postDelayed(this, 1000)
                }
            }
        })
    }

    private fun updateTinyPlayer(playerState: PlayerState) {
        runOnUiThread {
            val track: Track? = playerState.track
            if (track != null) {
                playerView.visibility = View.VISIBLE

                pbProgress.max = track.duration.toInt()
                pbProgress.progress = playerState.playbackPosition.toInt()

                if (tvPlayerTitle.text != track.name) {
                    tvPlayerTitle.text = track.name
                    tvPlayerArtist.text = track.artist.name

                    ivPlayerCover.loadImageFromUri(track.imageUri)
                }

                if (playerState.isPaused) {
                    btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_48)
                } else {
                    btnPlay.setImageResource(R.drawable.ic_baseline_pause_48)
                }

                if (playerState.playbackOptions.isShuffling) {
                    btnShuffle.setColorFilter(activeColor)
                } else {
                    btnShuffle.clearColorFilter()
                }

                when (playerState.playbackOptions.repeatMode) {
                    0 -> {
                        btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_48)
                        btnRepeat.clearColorFilter()
                    }
                    1 -> {
                        btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_one_48)
                    }
                    2 -> {
                        btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_48)
                        btnRepeat.setColorFilter(activeColor)
                    }
                }
            } else {
                playerView.visibility = View.INVISIBLE
            }
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

//        if (::spotifyPlayer.isInitialized) {
//            spotifyPlayer.disconnect()
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
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