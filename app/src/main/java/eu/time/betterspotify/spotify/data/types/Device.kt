package eu.time.betterspotify.spotify.data.types

data class Device(
    val id: String,
    val is_active: Boolean,
    val is_private_session: Boolean,
    val is_restricted: Boolean,
    val name: String,
    val type: String,
    val volume_percent: Int
)