package com.muchen.tweetstormmaker.androidui.di

import android.content.Context
import androidx.lifecycle.ViewModel
import com.muchen.tweetstormmaker.androidui.viewmodel.DraftsViewModel
import com.muchen.tweetstormmaker.androidui.viewmodel.TwitterViewModel
import com.muchen.tweetstormmaker.androidui.viewmodel.factory.ActivityViewModelFactory
import com.muchen.tweetstormmaker.interfaceadapter.repository.*
import com.muchen.tweetstormmaker.persistence.PersistenceImpl
import com.muchen.tweetstormmaker.persistence.room.RoomAppDatabase
import com.muchen.tweetstormmaker.twitterservice.TwitterServiceImpl
import com.muchen.tweetstormmaker.twitterservice.retrofit.RetrofitTwitterApiClient
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.inject.Named
import javax.inject.Provider

@Module
object ActivityModule {

    // persistence and twitter service interface
    @JvmStatic
    @ActivityScope
    @Provides
    fun provideIPersistence(context: Context): IPersistence {
        return PersistenceImpl.getInstance(RoomAppDatabase.getInstance(context))
    }

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideITwitterService(@Named("API_KEY")key: String,
                               @Named("API_KEY_SECRET")keySecret: String): ITwitterService {
        return TwitterServiceImpl.getInstance(RetrofitTwitterApiClient(key, keySecret))
    }

    // view models
    @JvmStatic
    @ActivityScope
    @Provides
    fun provideTwitterViewModel(persistence: IPersistence,
                                twitterService: ITwitterService): TwitterViewModel {

        return TwitterViewModel(persistence,
                                twitterService)
    }

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideDraftsViewModel(persistence: IPersistence): DraftsViewModel {
        return DraftsViewModel(persistence)
    }

    // view model factory
    @JvmStatic
    @ActivityScope
    @Provides
    @IntoMap
    @ViewModelKey(TwitterViewModel::class)
    fun provideTwitterViewModelIntoMap(twitterApiViewModel: TwitterViewModel): ViewModel = twitterApiViewModel

    @JvmStatic
    @ActivityScope
    @Provides
    @IntoMap
    @ViewModelKey(DraftsViewModel::class)
    fun provideDraftsViewModelIntoMap(draftsViewModel: DraftsViewModel): ViewModel = draftsViewModel

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideActivityViewModelFactory(activityViewModelProvider: Map<Class<out ViewModel>,
            @JvmSuppressWildcards Provider<ViewModel>>): ActivityViewModelFactory {
        return ActivityViewModelFactory(activityViewModelProvider)
    }
}