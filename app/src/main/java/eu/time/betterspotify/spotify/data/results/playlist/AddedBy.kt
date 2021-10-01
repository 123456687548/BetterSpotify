package eu.time.betterspotify.spotify.data.results.playlist

import eu.time.betterspotify.spotify.data.types.ExternalUrls

data class AddedBy(
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val type: String,
    val uri: String
)