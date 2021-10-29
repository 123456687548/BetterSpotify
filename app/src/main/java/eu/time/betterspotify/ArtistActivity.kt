package eu.time.betterspotify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import eu.time.betterspotify.recycleview.adapter.AlbumRecycleViewAdapter
import eu.time.betterspotify.recycleview.adapter.TrackRecycleViewAdapter
import eu.time.betterspotify.spotify.SpotifyApi
import eu.time.betterspotify.spotify.SpotifyPlayer
import eu.time.betterspotify.spotify.data.types.*
import java.lang.reflect.Type

class ArtistActivity : AppCompatActivity() {
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

        SpotifyApi.getInstance().getArtistAlbums(this, artist.id, { respone ->
            val type: Type = object : TypeToken<ResultContainer<Album>>() {}.type
            val result = Gson().fromJson<ResultContainer<Album>>(respone, type)

            albumsList.addAll(result.items)
            albumsAdapter.notifyDataSetChanged()
        })

        SpotifyApi.getInstance().getArtistTopTracks(this, artist.id, { respone ->
            val result = Gson().fromJson(respone, ArtistTopTracks::class.java)

            topTracksList.addAll(result.tracks)
            topTracksAdapter.notifyDataSetChanged()
        })
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
        PlayerController.getInstance().start(this)
    }
}