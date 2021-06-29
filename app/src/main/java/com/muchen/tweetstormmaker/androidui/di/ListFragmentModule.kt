package com.muchen.tweetstormmaker.androidui.di

import androidx.navigation.NavController
import com.muchen.tweetstormmaker.androidui.adatper.DraftListAdapter
import dagger.Module
import dagger.Provides

@Module
object ListFragmentModule {

    // adapter
    @JvmStatic
    @FragmentScope
    @Provides
    fun provideDraftListAdapter(navigateToEditFragment: (NavController, Long) -> Unit): DraftListAdapter {
        return DraftListAdapter(navigateToEditFragment)
    }
}