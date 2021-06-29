package com.muchen.tweetstormmaker.androidui

import android.app.Application
import com.muchen.tweetstormmaker.androidui.di.ApplicationComponent
import com.muchen.tweetstormmaker.androidui.di.DaggerApplicationComponent
import com.muchen.tweetstormmaker.androidui.livedata.InternetAccessLiveData
import javax.inject.Inject

class TweetstormMakerApplication : Application() {

    lateinit var applicationComponent: ApplicationComponent

    @Inject
    lateinit var hasInternetAccess: InternetAccessLiveData

    override fun onCreate() {
        super.onCreate()
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationContext(this)
                .build()
        applicationComponent.inject(this)
    }
}