package eu.time.betterspotify.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network

import java.lang.Exception

class NetworkHandler {
    companion object {
        private var initalized = false

        var isNetworkConnected = false

        fun registerNetworkCallback(context: Context) {
            if (initalized) return

            try {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                connectivityManager.registerDefaultNetworkCallback(object : NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        isNetworkConnected = true
                    }

                    override fun onLost(network: Network) {
                        isNetworkConnected = false
                    }
                })
                isNetworkConnected = false
                initalized = true
            } catch (e: Exception) {
                isNetworkConnected = false
            }
        }
    }
}