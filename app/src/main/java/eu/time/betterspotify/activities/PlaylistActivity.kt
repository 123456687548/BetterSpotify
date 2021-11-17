package eu.time.betterspotify.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.time.betterspotify.controllers.NavigationController
import eu.time.betterspotify.controllers.PlayerController
import eu.time.betterspotify.R
import eu.time.betterspotify.recycleview.adapter.TrackRecycleViewAdapter
import eu.time.betterspotify.spotify.SpotifyApi
import eu.time.betterspotify.spotify.SpotifyPlayer
import eu.time.betterspotify.spotify.data.types.Playlist
import eu.time.betterspotify.spotify.data.types.Track
import eu.time.betterspotify.util.openActivity

class PlaylistActivity : NavigationBarActivity() {
    companion object {
        private val EXTRA_TRACK_KEY = "EXTRA_TRACK_KEY"

        private lateinit var playlist: Playlist

        fun openPlaylist(context: Context, playlist: Playlist) {
            Companion.playlist = playlist

            val intent = Intent(context, PlaylistActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }

        fun openPlaylistFromPlayerContext(context: Context, currentTrack: com.spotify.protocol.types.Track?) {
            SpotifyPlayer.getInstance(context).getPlayerContext { playerContext ->
                if (playerContext.uri == null) return@getPlayerContext

                val playlistId = playerContext.uri.substringAfterLast(':')

                val extras = Bundle()

                if (currentTrack != null) {
                    extras.putString(EXTRA_TRACK_KEY, currentTrack.uri)
                }

                SpotifyApi.getInstance().getPlaylist(context, playlistId, { result ->
                    playlist = result
                    openActivity(context, PlaylistActivity::class.java, extras)
                })
            }
        }
    }

    private val trackList = mutableListOf<Track>()

    private lateinit var spotifyPlayer: SpotifyPlayer

    private lateinit var adapter: TrackRecycleViewAdapter
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

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

        SpotifyApi.getInstance().initialize(this) {
            if (playlist == Playlist.savedTracksPlaylist) {
                SpotifyApi.getInstance().getSavedTracks(this, onSuccess = { result ->
                    trackList.clear()
                    trackList.addAll(result)
                    adapter.notifyDataSetChanged()
                    scrollToTrack()
                })
            } else {
                SpotifyApi.getInstance().getPlaylistTracks(this, "https://api.spotify.com/v1/playlists/${playlist.id}/tracks", { result ->
                    trackList.clear()
                    trackList.addAll(result)
                    adapter.notifyDataSetChanged()
                    scrollToTrack()
                })
            }
            PlayerController.getInstance().start(this)
        }
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