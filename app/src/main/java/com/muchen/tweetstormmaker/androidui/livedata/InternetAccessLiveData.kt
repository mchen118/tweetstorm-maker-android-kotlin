package com.muchen.tweetstormmaker.androidui.livedata

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData

class InternetAccessLiveData(private val cm: ConnectivityManager): LiveData<Boolean>() {
    override fun onActive() {
        super.onActive()
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        val networkCallback = object: ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                postValue(true)
                Log.d(TAG, "network $network with internet access is available")
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                postValue(false)
                Log.d(TAG, "network $network with internet access is lost")
            }

            override fun onUnavailable() {
                super.onUnavailable()
                postValue(false)
                Log.d(TAG, "no network with internet access is unavailable")
            }
        }
        cm.requestNetwork(networkRequest, networkCallback)
    }

    companion object {
        val TAG = this::class.simpleName
    }
}