package eu.time.betterspotify.recycleview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import eu.time.betterspotify.activities.AlbumActivity
import eu.time.betterspotify.R
import eu.time.betterspotify.spotify.SpotifyPlayer
import eu.time.betterspotify.spotify.data.types.Album
import eu.time.betterspotify.util.loadImageFromUrl

class AlbumRecycleViewAdapter(private val dataSet: MutableList<Album>, private val spotifyPlayer: SpotifyPlayer, private val contextUri: String? = null) :
    RecyclerView.Adapter<AlbumRecycleViewAdapter.ViewHolder>() {

    class ViewHolder(view: View, private val spotifyPlayer: SpotifyPlayer, private val contextUri: String?) : RecyclerView.ViewHolder(view) {
        lateinit var album: Album
        var trackPlaylistPos: Int = 0
        val tvTrack: TextView = view.findViewById(R.id.tvTitle)
        val ivCover: ImageView = view.findViewById(R.id.ivCover)
        private val btnPlay: ImageButton = view.findViewById(R.id.btnPlay)
        private val btnQueue: ImageButton = view.findViewById(R.id.btnQueue)

        init {
            view.setOnClickListener {
                AlbumActivity.openAlbum(it.context, album)
            }

            btnPlay.setOnClickListener {
                spotifyPlayer.playUri(album.uri)
            }

            btnQueue.setOnClickListener { view ->
                //todo queue every track of album one by one
//                spotifyPlayer.queueUri(album.uri) {
//                    Toast.makeText(view.context, "${album.name} queued!", Toast.LENGTH_SHORT).show()
//                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.album_view, viewGroup, false)

        return ViewHolder(view, spotifyPlayer, contextUri)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.album = dataSet[position]
        viewHolder.tvTrack.text = viewHolder.album.name
        viewHolder.trackPlaylistPos = position

        if (dataSet[position].images.isNotEmpty()) {
            viewHolder.ivCover.loadImageFromUrl(dataSet[position].images[0].url)
        } else {
            viewHolder.ivCover.setImageResource(R.drawable.ic_no_cover_24)
        }
    }

    override fun getItemCount() = dataSet.size
}