package eu.time.betterspotify.recycleview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import eu.time.betterspotify.R
import eu.time.betterspotify.spotify.SpotifyPlayer
import eu.time.betterspotify.spotify.data.types.Playlist
import eu.time.betterspotify.util.loadImageFromUrl

class PlaylistRecycleViewAdapter(private val dataSet: MutableList<Playlist>, private val spotifyPlayer: SpotifyPlayer, private val playlistCallback: (String) -> Unit) :
    RecyclerView.Adapter<PlaylistRecycleViewAdapter.ViewHolder>() {

    class ViewHolder(view: View, private val spotifyPlayer: SpotifyPlayer) : RecyclerView.ViewHolder(view) {
        lateinit var playlist: Playlist
        lateinit var callback: (String) -> Unit

        val tvPlaylistName: TextView = view.findViewById(R.id.tvTitle)
        val tvPlaylistSize: TextView = view.findViewById(R.id.tvPlaylistSize)
        val ivPlaylistImage: ImageView = view.findViewById(R.id.ivCover)

        init {
            view.setOnClickListener {
                callback(playlist.id)
            }

            ivPlaylistImage.setOnClickListener {
                spotifyPlayer.playUri(playlist.uri)
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.playlist_view, viewGroup, false)

        return ViewHolder(view, spotifyPlayer)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.playlist = dataSet[position]
        viewHolder.callback = playlistCallback
        viewHolder.tvPlaylistName.text = dataSet[position].name
        viewHolder.tvPlaylistSize.text = dataSet[position].tracks.total.toString()

        if (dataSet[position].images.isNotEmpty()) {
            viewHolder.ivPlaylistImage.loadImageFromUrl(dataSet[position].images[0].url)
        }
    }

    override fun getItemCount() = dataSet.size
}