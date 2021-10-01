package eu.time.betterspotify.spotify.data.results.playlist

import eu.time.betterspotify.spotify.data.types.Track
import eu.time.betterspotify.spotify.data.types.VideoThumbnail

data class PlaylistItem(
    val added_at: String,
    val added_by: AddedBy,
    val is_local: Boolean,
    val primary_color: Any,
    val track: Track,
    val video_thumbnail: VideoThumbnail
)