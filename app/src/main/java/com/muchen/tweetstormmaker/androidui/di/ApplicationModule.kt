package com.muchen.tweetstormmaker.androidui.di

import android.content.Context
import android.net.ConnectivityManager
import com.muchen.tweetstormmaker.androidui.livedata.InternetAccessLiveData
import dagger.Module
import dagger.Provides

@Module(subcomponents = [ActivityComponent::class])
object ApplicationModule {

    @JvmStatic
    @ApplicationScope
    @Provides
    fun provideInternetAccessLiveData(applicationContext: Context): InternetAccessLiveData {
        return InternetAccessLiveData(applicationContext.getSystemService(ConnectivityManager::class.java))
    }
}