package eu.time.betterspotify.spotify.data.types

data class Actions(
    val interrupting_playback: Boolean,
    val pausing: Boolean,
    val resuming: Boolean,
    val seeking: Boolean,
    val skipping_next: Boolean,
    val skipping_prev: Boolean,
    val toggling_repeat_context: Boolean,
    val toggling_repeat_track: Boolean,
    val toggling_shuffle: Boolean,
    val transferring_playback: Boolean
)