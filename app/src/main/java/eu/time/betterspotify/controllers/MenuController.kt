package eu.time.betterspotify.controllers

import android.content.Context
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import com.spotify.protocol.types.Track
import eu.time.betterspotify.R
import eu.time.betterspotify.activities.AlbumActivity
import eu.time.betterspotify.activities.ArtistActivity
import eu.time.betterspotify.activities.PlaylistPickerActivity
import eu.time.betterspotify.spotify.data.spotifyApi.SpotifyApi
import eu.time.betterspotify.spotify.SpotifyPlayer
import eu.time.betterspotify.spotify.data.spotifyApi.addTracksToPlaylist
import eu.time.betterspotify.util.share

class MenuController {
    companion object {
        private val INSTANCE = MenuController()

        fun getInstance(): MenuController {
            return INSTANCE
        }
    }

    private val MENU_LIKE = 1
    private val MENU_ADD_TO_PLAYLIST = 2
    private val MENU_QUEUE = 3
    private val MENU_OPEN_ALBUM = 4
    private val MENU_OPEN_ARTIST = 5
    private val MENU_SHARE = 6

    fun openMenuTrack(context: Context, anchor: View, selectedTrack: Track) {
        val spotifyPlayer = SpotifyPlayer.getInstance(context)

        spotifyPlayer.isLiked(selectedTrack.uri) { isLiked ->
            val popup = PopupMenu(context, anchor)

            val menuLikeTitle = if (isLiked) context.getString(R.string.remove_like) else context.getString(R.string.like)

            popup.menu.add(Menu.NONE, MENU_LIKE, Menu.NONE, menuLikeTitle)
            popup.menu.add(Menu.NONE, MENU_ADD_TO_PLAYLIST, Menu.NONE, context.getString(R.string.add_to_playlist))
            popup.menu.add(Menu.NONE, MENU_QUEUE, Menu.NONE, context.getString(R.string.add_to_queue))
            popup.menu.add(Menu.NONE, MENU_OPEN_ALBUM, Menu.NONE, context.getString(R.string.open_album))
            popup.menu.add(Menu.NONE, MENU_OPEN_ARTIST, Menu.NONE, context.getString(R.string.open_artist))
            popup.menu.add(Menu.NONE, MENU_SHARE, Menu.NONE, context.getString(R.string.share))

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    MENU_LIKE -> {
                        spotifyPlayer.toggleLike(selectedTrack.uri) { wasLiked ->
                            if (wasLiked) {
                                item.setTitle(R.string.remove_like)
                            } else {
                                item.setTitle(R.string.like)
                            }
                        }
                    }
                    MENU_ADD_TO_PLAYLIST -> {
                        PlaylistPickerActivity.openPlaylistPicker(context) { playlist ->
                            addTracksToPlaylist(context, listOf(selectedTrack.uri), playlist)
                        }
                    }
                    MENU_QUEUE -> {
                        SpotifyPlayer.getInstance(context).queueUri(selectedTrack.uri) {
                            Toast.makeText(context, "${selectedTrack.name} queued!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    MENU_OPEN_ALBUM -> {
                        AlbumActivity.openAlbum(context, selectedTrack.album)
                    }
                    MENU_OPEN_ARTIST -> {
                        ArtistActivity.openArtist(context, selectedTrack.artist)
                    }
                    MENU_SHARE -> {
                        selectedTrack.share(context)
                    }
                }
                true
            }

            popup.show()
        }
    }
}