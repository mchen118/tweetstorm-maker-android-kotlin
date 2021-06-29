package com.muchen.tweetstormmaker.androidui.di

import androidx.navigation.NavController
import com.muchen.tweetstormmaker.androidui.view.listfragment.BaseListFragment
import dagger.BindsInstance
import dagger.Component

@FragmentScope
@Component(modules = [ListFragmentModule::class])
interface ListFragmentComponent{

    fun inject(baseListFragment: BaseListFragment)

    @Component.Builder
    interface Builder{

        @BindsInstance
        fun navigationToEditFragment(navigationToEditFragment: (NavController, Long) -> Unit): Builder

        fun build(): ListFragmentComponent
    }
}