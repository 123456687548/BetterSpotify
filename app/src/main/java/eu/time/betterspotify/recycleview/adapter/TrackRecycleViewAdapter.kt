package eu.time.betterspotify.recycleview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import eu.time.betterspotify.R
import eu.time.betterspotify.spotify.SpotifyPlayer
import eu.time.betterspotify.spotify.data.types.Track
import eu.time.betterspotify.util.loadImageFromUrl

class TrackRecycleViewAdapter(private val dataSet: MutableList<Track>, private val spotifyPlayer: SpotifyPlayer, private val contextUri: String? = null) :
    RecyclerView.Adapter<TrackRecycleViewAdapter.ViewHolder>() {

    class ViewHolder(view: View, private val spotifyPlayer: SpotifyPlayer, private val contextUri: String?) : RecyclerView.ViewHolder(view) {
        lateinit var track: Track
        var trackPlaylistPos: Int = 0
        val tvTrack: TextView = view.findViewById(R.id.tvTitle)
        val tvArtist: TextView = view.findViewById(R.id.tvArtist)
        val ivCover: ImageView = view.findViewById(R.id.ivCover)
        private val btnPlay: ImageButton = view.findViewById(R.id.btnPlay)
        private val btnQueue: ImageButton = view.findViewById(R.id.btnQueue)

        init {
            view.setOnClickListener {
                if (contextUri != null) {
                    SpotifyPlayer.getInstance(it.context).playContext(contextUri, trackPlaylistPos)
                } else {
                    playTrack()
                }
            }

            btnPlay.setOnClickListener {
                playTrack()
            }

            btnQueue.setOnClickListener { view ->
                spotifyPlayer.queueTrack(track) {
                    Toast.makeText(view.context, "${track.name} queued!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun playTrack() {
            spotifyPlayer.playUri(track.uri)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.track_view, viewGroup, false)

        return ViewHolder(view, spotifyPlayer, contextUri)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.track = dataSet[position]
        viewHolder.tvTrack.text = viewHolder.track.name
        viewHolder.tvArtist.text = viewHolder.track.artists[0].name
        viewHolder.trackPlaylistPos = position

        if (dataSet[position].album.images.isNotEmpty()) {
            viewHolder.ivCover.loadImageFromUrl(dataSet[position].album.images[0].url)
        } else {
            viewHolder.ivCover.setImageResource(R.drawable.ic_no_cover_24)
        }
    }

    override fun getItemCount() = dataSet.size
}