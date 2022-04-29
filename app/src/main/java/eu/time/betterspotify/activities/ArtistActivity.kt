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
import eu.time.betterspotify.recycleview.adapter.AlbumRecycleViewAdapter
import eu.time.betterspotify.recycleview.adapter.TrackRecycleViewAdapter
import eu.time.betterspotify.spotify.SpotifyPlayer
import eu.time.betterspotify.spotify.data.spotifyApi.*
import eu.time.betterspotify.spotify.data.types.Album
import eu.time.betterspotify.spotify.data.types.Artist
import eu.time.betterspotify.spotify.data.types.Track
import eu.time.betterspotify.util.openActivity

class ArtistActivity : NavigationBarActivity() {
    companion object {
        private lateinit var artist: Artist

        fun openArtist(context: Context, artist: Artist) {
            val intent = Intent(context, ArtistActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            Companion.artist = artist
            context.startActivity(intent)
        }

        fun openArtist(context: Context, artist: com.spotify.protocol.types.Artist) {
            getArtist(context, artist, { result ->
                openArtist(context, result)
            })
        }

        fun openArtistFromPlayerContext(context: Context, artistId: String) {
            getArtist(context, artistId, { result ->
                openArtist(context, result)
            })
        }
    }

    private val topTracksList = mutableListOf<Track>()
    private val albumsList = mutableListOf<Album>()
    private lateinit var topTracksAdapter: TrackRecycleViewAdapter
    private lateinit var albumsAdapter: AlbumRecycleViewAdapter

    private lateinit var spotifyPlayer: SpotifyPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist)

        spotifyPlayer = SpotifyPlayer.getInstance(this)

        val tvArtist = findViewById<TextView>(R.id.tvArtist)
        tvArtist.text = artist.name

        initRecycleView()
    }

    private fun initRecycleView() {
        val rvAlbums = findViewById<RecyclerView>(R.id.rvAlbums)
        val rvTopTracks = findViewById<RecyclerView>(R.id.rvTopTracks)

        albumsAdapter = AlbumRecycleViewAdapter(albumsList, spotifyPlayer)

        rvAlbums.adapter = albumsAdapter
        rvAlbums.layoutManager = LinearLayoutManager(this)

        topTracksAdapter = TrackRecycleViewAdapter(topTracksList, spotifyPlayer)

        rvTopTracks.adapter = topTracksAdapter
        rvTopTracks.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        SpotifyApi.getInstance().initialize(this) {
            getArtistAlbums(this, artist.id, { result ->
                albumsList.clear()
                albumsList.addAll(result.items)
                albumsAdapter.notifyDataSetChanged()
            })

            getArtistTopTracks(this, artist.id, { result ->
                topTracksList.clear()
                topTracksList.addAll(result.tracks)
                topTracksAdapter.notifyDataSetChanged()
            })

            PlayerController.getInstance().start(this)
        }
    }

    override fun onPause() {
        super.onPause()
        PlayerController.getInstance().stop()
    }

    override fun getCurrentPage(): NavigationController.Page = NavigationController.Page.UNDEFINED
}