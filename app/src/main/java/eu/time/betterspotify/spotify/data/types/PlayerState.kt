package eu.time.betterspotify.spotify.data.types

data class PlayerState(
    val actions: Actions,
    val context: Context,
    val currently_playing_type: String,
    val device: Device,
    val is_playing: Boolean,
    val item: Item,
    val progress_ms: Long,
    val repeat_state: String,
    val shuffle_state: String,
    val timestamp: Long
)