package eu.time.betterspotify.recycleview.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import eu.time.betterspotify.R
import eu.time.betterspotify.spotify.data.SpotifyPlayer
import eu.time.betterspotify.spotify.data.playlist.Playlist
import eu.time.betterspotify.spotify.data.track.Item

class TrackRecycleViewAdapter(private val dataSet: MutableList<Item>) :
    RecyclerView.Adapter<TrackRecycleViewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPlaylistName: TextView = view.findViewById(R.id.tvPlaylistName)
        val tvPlaylistUri: TextView = view.findViewById(R.id.tvPlaylistUri)
        val ivPlaylistImage: ImageView = view.findViewById(R.id.ivPlaylistImage)

        init {
            view.setOnClickListener {
                val uri = tvPlaylistUri.text

                SpotifyPlayer.getInstance().getRemote(view.context).playerApi.play(uri.toString())
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.playlist_view, viewGroup, false)

        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.tvPlaylistName.text = dataSet[position].track.name
        viewHolder.tvPlaylistUri.text = dataSet[position].track.uri

        if (dataSet[position].track.album.images.isNotEmpty()) {
            Picasso.get().load(dataSet[position].track.album.images[0].url).into(viewHolder.ivPlaylistImage)
        }
    }

    override fun getItemCount() = dataSet.size
}