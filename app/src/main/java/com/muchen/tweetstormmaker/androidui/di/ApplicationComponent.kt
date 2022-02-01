package com.muchen.tweetstormmaker.androidui.di

import android.content.Context
import com.muchen.tweetstormmaker.androidui.TweetstormMakerApplication
import dagger.BindsInstance
import dagger.Component

@ApplicationScope
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {

    val activityComponentBuilder: ActivityComponent.Builder

    fun inject(application: TweetstormMakerApplication)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun applicationContext(applicationContext: Context): Builder

        fun build(): ApplicationComponent
    }
}