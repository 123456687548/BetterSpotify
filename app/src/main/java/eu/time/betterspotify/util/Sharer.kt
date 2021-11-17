package eu.time.betterspotify.util

import android.content.Context
import android.content.Intent

fun share(context: Context, msg: String) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TEXT, msg)
    context.startActivity(Intent.createChooser(shareIntent, "choose one"))
}