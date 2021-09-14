package eu.time.betterspotify.util

import android.widget.ImageView
import com.spotify.protocol.types.ImageUri
import com.squareup.picasso.Picasso
import eu.time.betterspotify.spotify.data.SpotifyApi
import eu.time.betterspotify.spotify.data.SpotifyPlayer
import java.math.BigInteger
import java.security.MessageDigest

fun String.sha256(): String {
    val md = MessageDigest.getInstance("SHA-256")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}

fun String.newSha256(): ByteArray {
    val bytes = this.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    return md.digest(bytes)
}

fun ImageView.loadImageFromUrl(url: String) {
    Picasso.get().load(url).into(this)
}

fun ImageView.loadImageFromUri(uri: ImageUri) {
    SpotifyPlayer.getInstance().getRemote()?.imagesApi?.getImage(uri)?.setResultCallback {
        setImageBitmap(it)
    }
}