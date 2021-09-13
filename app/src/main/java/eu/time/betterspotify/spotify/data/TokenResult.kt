package eu.time.betterspotify.spotify.data

import com.google.gson.annotations.SerializedName

data class TokenResult(@SerializedName("access_token") val accessToken: String,@SerializedName("refresh_token") val refreshToken: String)
