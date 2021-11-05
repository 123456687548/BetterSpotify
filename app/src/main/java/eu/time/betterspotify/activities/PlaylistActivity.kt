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
        private lateinit var playlist: Playlist

        fun openPlaylist(context: Context, playlist: Playlist) {
            Companion.playlist = playlist

            val intent = Intent(context, PlaylistActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }

        fun openPlaylistFromPlayerContext(context: Context) {
            SpotifyPlayer.getInstance(context).getPlayerContext { playerContext ->
                if (playerContext.uri == null) return@getPlayerContext

                val playlistId = playerContext.uri.substringAfterLast(':')

                SpotifyApi.getInstance().getPlaylist(context, playlistId, { result ->
                    playlist = result
                    openActivity(context, PlaylistActivity::class.java)
                })
            }
        }
    }

    private val trackList = mutableListOf<Track>()

    private lateinit var spotifyPlayer: SpotifyPlayer

    private lateinit var adapter: TrackRecycleViewAdapter

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
        rvPlaylistList.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()

        SpotifyApi.getInstance().initialize(this) {
            SpotifyApi.getInstance().getPlaylistTracks(this, "https://api.spotify.com/v1/playlists/${playlist.id}/tracks", { result ->
                trackList.clear()
                trackList.addAll(result)
                adapter.notifyDataSetChanged()
            })

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