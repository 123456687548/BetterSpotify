package eu.time.betterspotify.util

import android.widget.ImageView
import com.spotify.protocol.types.ImageUri
import com.squareup.picasso.Picasso
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

fun String.sha256(): ByteArray {
    val bytes = this.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    return md.digest(bytes)
}

fun ImageView.loadImageFromUrl(url: String) {
    Picasso.get().load(url).into(this)
}

fun ImageView.loadImageFromUri(uri: ImageUri) {
    SpotifyImageManager.getInstance().loadBitmap(uri, this)
}

fun Long.toTimestampString(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this).toInt()
    val minutes = (TimeUnit.MILLISECONDS.toMinutes(this) % 60).toInt()
    val seconds = (TimeUnit.MILLISECONDS.toSeconds(this) % 60).toInt()

    val hoursString = if (hours == 0) "" else if (hours < 10) "0${hours}:" else "$hours:"
//    val minutesString = if(minutes < 10) "0$minutes:" else "$minutes:"
    val minutesString = "$minutes:"
    val secondsString = if(seconds < 10) "0$seconds" else "$seconds"

    return "$hoursString$minutesString$secondsString"
}