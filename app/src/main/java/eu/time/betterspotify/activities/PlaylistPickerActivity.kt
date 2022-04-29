package eu.time.betterspotify.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.time.betterspotify.R
import eu.time.betterspotify.recycleview.adapter.PlaylistPickerRecycleViewAdapter
import eu.time.betterspotify.spotify.data.spotifyApi.SpotifyApi
import eu.time.betterspotify.spotify.data.spotifyApi.getUsersPlaylists
import eu.time.betterspotify.spotify.data.types.Playlist

class PlaylistPickerActivity : AppCompatActivity() {
    companion object {
        private lateinit var selectPlaylistCallback: (Playlist) -> Unit

        fun openPlaylistPicker(context: Context, callback: (Playlist) -> Unit) {
            val intent = Intent(context, PlaylistPickerActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            selectPlaylistCallback = callback
            context.startActivity(intent)
        }
    }

    private val playlistList = mutableListOf<Playlist>()
    private lateinit var adapter: PlaylistPickerRecycleViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        initRecycleView()
    }

    private fun initRecycleView() {
        val rvPlaylistList = findViewById<RecyclerView>(R.id.rvPlaylistList)

        adapter = PlaylistPickerRecycleViewAdapter(playlistList) { playlist ->
            finish()
            selectPlaylistCallback(playlist)
        }

        rvPlaylistList.adapter = adapter
        rvPlaylistList.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()

        SpotifyApi.getInstance().initialize(this) {
            loadPlaylists()
        }
    }

    private fun loadPlaylists() {
        getUsersPlaylists(this, this::updateRecycleView)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecycleView(newData: List<Playlist>) {
        playlistList.clear()
        playlistList.addAll(newData)
        adapter.notifyDataSetChanged()
    }
}