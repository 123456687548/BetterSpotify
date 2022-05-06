package eu.time.betterspotify.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.time.betterspotify.controllers.NavigationController
import eu.time.betterspotify.controllers.PlayerController
import eu.time.betterspotify.R
import eu.time.betterspotify.recycleview.adapter.TrackRecycleViewAdapter
import eu.time.betterspotify.spotify.SpotifyPlayer
import eu.time.betterspotify.spotify.data.spotifyApi.*
import eu.time.betterspotify.spotify.data.types.Playlist
import eu.time.betterspotify.spotify.data.types.Track
import eu.time.betterspotify.util.openActivity
import kotlin.math.floor

class PlaylistActivity : NavigationBarActivity() {
    companion object {
        private val EXTRA_TRACK_KEY = "EXTRA_TRACK_KEY"

        private lateinit var playlist: Playlist
        private var reversed = false

        fun openPlaylist(context: Context, playlist: Playlist, reversed: Boolean = false) {
            Companion.playlist = playlist
            Companion.reversed = reversed

            val intent = Intent(context, PlaylistActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }

        fun openPlaylistFromPlayerContext(context: Context, playlistId: String, currentTrack: com.spotify.protocol.types.Track?) {
            val extras = Bundle()

            if (currentTrack != null) {
                extras.putString(EXTRA_TRACK_KEY, currentTrack.uri)
            }

            getPlaylist(context, playlistId, { result ->
                playlist = result
                openActivity(context, PlaylistActivity::class.java, extras)
            })
        }
    }

    private val trackList = mutableListOf<Track>()

    private lateinit var spotifyPlayer: SpotifyPlayer

    private lateinit var adapter: TrackRecycleViewAdapter
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)

        spotifyPlayer = SpotifyPlayer.getInstance(this)


        initRecycleView()
    }

    private fun initRecycleView() {
        val rvPlaylistList = findViewById<RecyclerView>(R.id.rvPlaylistList)
        adapter = TrackRecycleViewAdapter(trackList, spotifyPlayer, playlist.uri)
        rvPlaylistList.adapter = adapter
        layoutManager = LinearLayoutManager(this)
        rvPlaylistList.layoutManager = layoutManager
    }

    private fun scrollToTrack() {
        val trackUri = intent.getStringExtra(EXTRA_TRACK_KEY) ?: return

        val trackPos = trackList.indexOfFirst { track -> track.uri == trackUri }

        layoutManager.scrollToPosition(trackPos)
    }

    override fun onStart() {
        super.onStart()

        findViewById<TextView>(R.id.tvPlaylistName).text = playlist.name

        SpotifyApi.getInstance().initialize(this) {
            if (playlist == Playlist.savedTracksPlaylist) {
                getSavedTracks(this, this::updateTracklist)
            } else {
                getPlaylistTracks(this, playlist.id, this::updateTracklist)
            }


            PlayerController.getInstance().start(this)
        }
    }

    private fun updatePlaylistInfo() {
        findViewById<TextView>(R.id.tvPlaylistSize).text = trackList.size.toString()
        findViewById<TextView>(R.id.tvPlaylistPlaytime).text = calcPlaylistPlaytime()
        findViewById<LinearLayout>(R.id.llPlaylistInfo).visibility = View.VISIBLE
    }

    private fun calcPlaylistPlaytime(): String {
        var milliseconds = 0

        trackList.forEach { track ->
            milliseconds += track.duration_ms
        }

        var seconds = floor((milliseconds / 1000).toDouble())
        var minutes = floor(seconds / 60)
        var hours = floor(minutes / 60)

        seconds %= 60
        minutes %= 60

        return "${hours.toInt()}h ${minutes.toInt()}m ${seconds.toInt()}s"
    }

    private fun updateTracklist(result: List<Track>) {
        trackList.clear()
        trackList.addAll(result)
        if (reversed) trackList.reverse()
        adapter.notifyDataSetChanged()
        scrollToTrack()

        updatePlaylistInfo()
    }

    override fun onPause() {
        super.onPause()
        PlayerController.getInstance().stop()

        if (::spotifyPlayer.isInitialized) {
            spotifyPlayer.disconnect()
        }
    }

    override fun getCurrentPage(): NavigationController.Page = NavigationController.Page.UNDEFINED
}