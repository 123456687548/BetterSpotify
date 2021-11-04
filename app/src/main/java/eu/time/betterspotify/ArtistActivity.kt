package eu.time.betterspotify

import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.time.betterspotify.recycleview.adapter.AlbumRecycleViewAdapter
import eu.time.betterspotify.recycleview.adapter.TrackRecycleViewAdapter
import eu.time.betterspotify.spotify.SpotifyApi
import eu.time.betterspotify.spotify.SpotifyPlayer
import eu.time.betterspotify.spotify.data.types.Album
import eu.time.betterspotify.spotify.data.types.Artist
import eu.time.betterspotify.spotify.data.types.Track

class ArtistActivity : NavigationBarActivity() {
    companion object {
        lateinit var artist: Artist
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
            SpotifyApi.getInstance().getArtistAlbums(this, artist.id, { result ->
                albumsList.addAll(result.items)
                albumsAdapter.notifyDataSetChanged()
            })

            SpotifyApi.getInstance().getArtistTopTracks(this, artist.id, { result ->
                topTracksList.addAll(result.tracks)
                topTracksAdapter.notifyDataSetChanged()
            })

            PlayerController.getInstance().start(this)
        }
    }

    override fun getCurrentPage(): NavigationController.Page = NavigationController.Page.UNDEFINED
}