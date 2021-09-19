package eu.time.betterspotify.recycleview.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import eu.time.betterspotify.R
import eu.time.betterspotify.spotify.data.SpotifyPlayer
import eu.time.betterspotify.spotify.data.track.Item
import eu.time.betterspotify.spotify.data.track.Track
import eu.time.betterspotify.util.loadImageFromUrl

class QueueRecycleViewAdapter(private val dataSet: MutableList<Item>) :
    RecyclerView.Adapter<QueueRecycleViewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        lateinit var track: Track
        val tvTrack: TextView = view.findViewById(R.id.tvTitle)
        val tvArtist: TextView = view.findViewById(R.id.tvArtist)
        val ivCover: ImageView = view.findViewById(R.id.ivCover)
        private val btnPlay: ImageButton = view.findViewById(R.id.btnPlay)
        private val btnQueue: ImageButton = view.findViewById(R.id.btnQueue)

        init {
            view.setOnClickListener {
                playTrack()
            }

            btnPlay.setOnClickListener {
                playTrack()
            }

            btnQueue.setOnClickListener {
                SpotifyPlayer.getInstance().getRemote()?.playerApi?.queue(track.uri)
            }
        }

        private fun playTrack() {
            SpotifyPlayer.getInstance().getRemote()?.playerApi?.play(track.uri)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.track_view, viewGroup, false)

        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.track = dataSet[position].track
        viewHolder.tvTrack.text = viewHolder.track.name
        viewHolder.tvArtist.text = viewHolder.track.artists[0].name

        if (dataSet[position].track.album.images.isNotEmpty()) {
            viewHolder.ivCover.loadImageFromUrl(dataSet[position].track.album.images[0].url)
        }
    }

    override fun getItemCount() = dataSet.size
}