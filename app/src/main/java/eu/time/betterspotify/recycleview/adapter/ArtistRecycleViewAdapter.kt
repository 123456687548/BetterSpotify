package eu.time.betterspotify.recycleview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import eu.time.betterspotify.R
import eu.time.betterspotify.spotify.SpotifyPlayer
import eu.time.betterspotify.spotify.data.types.Artist
import eu.time.betterspotify.util.loadImageFromUrl

import eu.time.betterspotify.activities.ArtistActivity

class ArtistRecycleViewAdapter(private val dataSet: MutableList<Artist>, private val spotifyPlayer: SpotifyPlayer) :
    RecyclerView.Adapter<ArtistRecycleViewAdapter.ViewHolder>() {

    class ViewHolder(view: View, private val spotifyPlayer: SpotifyPlayer) : RecyclerView.ViewHolder(view) {
        lateinit var artist: Artist
        val tvArtist: TextView = view.findViewById(R.id.tvArtist)
        val ivCover: ImageView = view.findViewById(R.id.ivCover)
        private val btnPlay: ImageButton = view.findViewById(R.id.btnPlay)

        init {
            view.setOnClickListener {
                ArtistActivity.openArtist(it.context, artist)
            }

            btnPlay.setOnClickListener {
                playArtist()
            }
        }

        private fun playArtist() {
            spotifyPlayer.playUri(artist.uri)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.artist_view, viewGroup, false)

        return ViewHolder(view, spotifyPlayer)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.artist = dataSet[position]
        viewHolder.tvArtist.text = viewHolder.artist.name

        if (dataSet[position].images.isNotEmpty()) {
            viewHolder.ivCover.loadImageFromUrl(dataSet[position].images[0].url)
        }
    }

    override fun getItemCount() = dataSet.size
}