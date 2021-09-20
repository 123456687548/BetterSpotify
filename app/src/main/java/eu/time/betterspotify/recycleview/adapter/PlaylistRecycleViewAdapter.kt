package eu.time.betterspotify.recycleview.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import eu.time.betterspotify.R
import eu.time.betterspotify.spotify.data.SpotifyPlayer
import eu.time.betterspotify.spotify.data.playlist.Playlist
import eu.time.betterspotify.util.loadImageFromUrl

class PlaylistRecycleViewAdapter(private val dataSet: MutableList<Playlist>, private val playlistCallback: (String) -> Unit) :
    RecyclerView.Adapter<PlaylistRecycleViewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        lateinit var playlist: Playlist
        lateinit var callback: (String) -> Unit

        val tvPlaylistName: TextView = view.findViewById(R.id.tvTitle)
        val tvPlaylistUri: TextView = view.findViewById(R.id.tvArtist)
        val ivPlaylistImage: ImageView = view.findViewById(R.id.ivCover)

        init {
            view.setOnClickListener {
                callback(playlist.id)
            }

            ivPlaylistImage.setOnClickListener {
                SpotifyPlayer.getInstance().getRemote()?.playerApi?.play(playlist.uri)
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.playlist_view, viewGroup, false)

        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.playlist = dataSet[position]
        viewHolder.callback = playlistCallback
        viewHolder.tvPlaylistName.text = dataSet[position].name
        viewHolder.tvPlaylistUri.text = dataSet[position].tracks.total.toString()

        if (dataSet[position].images.isNotEmpty()) {
            viewHolder.ivPlaylistImage.loadImageFromUrl(dataSet[position].images[0].url)
        }
    }

    override fun getItemCount() = dataSet.size
}