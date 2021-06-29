package com.muchen.tweetstormmaker.androidui.di

import com.muchen.tweetstormmaker.androidui.view.MainActivity
import dagger.BindsInstance
import dagger.Subcomponent
import javax.inject.Named

@ActivityScope
@Subcomponent(modules = [ActivityModule::class])
interface ActivityComponent {

    fun inject(mainActivity: MainActivity)

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        fun apiKey(@Named("API_KEY")apiKey: String): Builder

        @BindsInstance
        fun apiKeySecret(@Named("API_KEY_SECRET")apiKeySecret: String): Builder

        fun build(): ActivityComponent
    }
}