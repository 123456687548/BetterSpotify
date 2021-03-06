package eu.time.betterspotify.spotify.data.types

data class Playlist(
    val collaborative: Boolean,
    val description: String,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    val owner: Owner,
    val primary_color: Any,
    val `public`: Boolean,
    val snapshot_id: String,
    val tracks: Tracks,
    val type: String,
    val uri: String
) {
    companion object {
        val savedTracksPlaylist = Playlist(
            false,
            "Saved Tracks",
            ExternalUrls("https://api.spotify.com/v1/me/tracks"),
            "https://api.spotify.com/v1/me/",
            "https://api.spotify.com/v1/me/",
            emptyList(),
            "Saved Tracks",
            Owner(
                "me",
                ExternalUrls(""),
                "",
                "",
                "",
                ""
            ),
            Any(),
            false,
            "",
            Tracks("", 0),
            "savedTracksPlaylist",
            ""
        )
    }
}

