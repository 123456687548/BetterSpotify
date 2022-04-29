package eu.time.betterspotify.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.time.betterspotify.controllers.NavigationController
import eu.time.betterspotify.controllers.PlayerController
import eu.time.betterspotify.R
import eu.time.betterspotify.recycleview.adapter.TrackRecycleViewAdapter
import eu.time.betterspotify.spotify.data.spotifyApi.SpotifyApi
import eu.time.betterspotify.spotify.SpotifyPlayer
import eu.time.betterspotify.spotify.data.spotifyApi.getAlbum
import eu.time.betterspotify.spotify.data.spotifyApi.getAlbumTracks
import eu.time.betterspotify.spotify.data.types.Album
import eu.time.betterspotify.spotify.data.types.Track

class AlbumActivity : NavigationBarActivity() {
    companion object {
        private lateinit var album: Album

        fun openAlbum(context: Context, album: Album) {
            val intent = Intent(context, AlbumActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            Companion.album = album
            context.startActivity(intent)
        }

        fun openAlbum(context: Context, album: com.spotify.protocol.types.Album) {
            getAlbum(context, album, { result ->
                if (result == null) return@getAlbum
                openAlbum(context, result)
            })
        }
    }

    private val trackList = mutableListOf<Track>()
    private lateinit var tracksAdapter: TrackRecycleViewAdapter

    private lateinit var spotifyPlayer: SpotifyPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)

        spotifyPlayer = SpotifyPlayer.getInstance(this)

        val tvAlbum = findViewById<TextView>(R.id.tvAlbum)
        tvAlbum.text = album.name

        initRecycleView()

        getAlbumTracks(this, album.id, { result ->
            trackList.addAll(result)
            trackList.forEach { it.album = album }
            tracksAdapter.notifyDataSetChanged()
        })
    }

    private fun initRecycleView() {
        val rvTracks = findViewById<RecyclerView>(R.id.rvTracks)

        tracksAdapter = TrackRecycleViewAdapter(trackList, spotifyPlayer, album.uri)

        rvTracks.adapter = tracksAdapter
        rvTracks.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        SpotifyApi.getInstance().initialize(this) {
            PlayerController.getInstance().start(this)
        }
    }

    override fun onPause() {
        super.onPause()
        PlayerController.getInstance().stop()
    }

    override fun getCurrentPage(): NavigationController.Page = NavigationController.Page.UNDEFINED
}