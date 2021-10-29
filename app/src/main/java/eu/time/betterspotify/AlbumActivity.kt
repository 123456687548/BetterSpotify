package eu.time.betterspotify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.time.betterspotify.recycleview.adapter.AlbumRecycleViewAdapter
import eu.time.betterspotify.recycleview.adapter.TrackRecycleViewAdapter
import eu.time.betterspotify.spotify.SpotifyApi
import eu.time.betterspotify.spotify.SpotifyPlayer
import eu.time.betterspotify.spotify.data.types.Album
import eu.time.betterspotify.spotify.data.types.Track

class AlbumActivity : AppCompatActivity() {
    companion object {
        lateinit var album: Album
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

        SpotifyApi.getInstance().getAlbumTracks(this, "https://api.spotify.com/v1/albums/${album.id}/tracks", { result ->
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
        PlayerController.getInstance().start(this)
    }
}