package com.muchen.tweetstormmaker.androidui.di

import android.content.Context
import androidx.lifecycle.ViewModel
import com.muchen.tweetstormmaker.androidui.viewmodel.DraftsViewModel
import com.muchen.tweetstormmaker.androidui.viewmodel.TwitterViewModel
import com.muchen.tweetstormmaker.androidui.viewmodel.factory.ActivityViewModelFactory
import com.muchen.tweetstormmaker.interfaceadapter.repository.*
import com.muchen.tweetstormmaker.interfaceadapter.usecase.database.DraftsCRUDUseCases
import com.muchen.tweetstormmaker.interfaceadapter.usecase.database.TwitterUserAndTokensCRUDUseCases
import com.muchen.tweetstormmaker.interfaceadapter.usecase.twitterservice.*
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

    // repositories
    @JvmStatic
    @ActivityScope
    @Provides
    fun provideIDraftsRepository(persistence: IPersistence): IDraftsRepository {
        return DraftsRepositoryImpl(persistence)
    }

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideITwitterRepository(persistence: IPersistence,
                                  twitterService: ITwitterService): ITwitterRepository {
        return TwitterRepositoryImpl(persistence, twitterService)
    }

    // use cases
    @JvmStatic
    @ActivityScope
    @Provides
    fun provideDraftsCRUDUseCases(repo: IDraftsRepository):
                DraftsCRUDUseCases {
        return DraftsCRUDUseCases(repo)
    }

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideTwitterUserAndTokensCRUDUseCases(repo: ITwitterRepository):
                TwitterUserAndTokensCRUDUseCases {
        return TwitterUserAndTokensCRUDUseCases(repo)
    }

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideGetAccessTokensUseCase(repo: ITwitterRepository):
                GetAccessTokensUseCase {
        return GetAccessTokensUseCase(repo)
    }

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideRetrieveAuthorizationUrlUseCase(repo: ITwitterRepository):
                RetrieveAuthorizationUrlUseCase {
        return RetrieveAuthorizationUrlUseCase(repo)
    }

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideRetrieveTwitterUserAndTokensUseCase(repo: ITwitterRepository):
                RetrieveTwitterUserAndTokensUseCase {
        return RetrieveTwitterUserAndTokensUseCase(repo)
    }

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideRevertBackToApiTokensUseCase(repo: ITwitterRepository): RevertBackToApiTokensUseCase {
        return RevertBackToApiTokensUseCase(repo)
    }

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideSendTweetstormUseCase(repo: ITwitterRepository): SendTweetstormUseCase {
        return SendTweetstormUseCase(repo)
    }

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideUnsendMultipleTweetstormsUseCase(twitterRepository: ITwitterRepository,
                                                draftsRepository: IDraftsRepository):
                UnsendMultipleTweetstormsUseCase {
        return UnsendMultipleTweetstormsUseCase(twitterRepository, draftsRepository)
    }

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideUnsendTweetstormUseCase(repo: ITwitterRepository): UnsendTweetstormUseCase {
        return UnsendTweetstormUseCase(repo)
    }

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideUpdateAccessTokensUseCase(repo: ITwitterRepository): UpdateAccessTokensUseCase {
        return UpdateAccessTokensUseCase(repo)
    }

    // view models
    @JvmStatic
    @ActivityScope
    @Provides
    fun provideTwitterViewModel(twitterUserAndTokensCRUDUseCases: TwitterUserAndTokensCRUDUseCases,
                                draftsCRUDUseCases: DraftsCRUDUseCases,
                                retrieveAuthorizationUrlUseCase: RetrieveAuthorizationUrlUseCase,
                                retrieveTwitterUserAndTokensUseCase: RetrieveTwitterUserAndTokensUseCase,
                                updateAccessTokensUseCase: UpdateAccessTokensUseCase,
                                revertBackToApiTokensUseCase: RevertBackToApiTokensUseCase,
                                sendTweetstormUseCase: SendTweetstormUseCase,
                                unsendTweetstormUseCase: UnsendTweetstormUseCase,
                                unsendMultipleTweetstormsUseCase: UnsendMultipleTweetstormsUseCase): TwitterViewModel {

        return TwitterViewModel(twitterUserAndTokensCRUDUseCases,
                                draftsCRUDUseCases,
                                retrieveAuthorizationUrlUseCase,
                                retrieveTwitterUserAndTokensUseCase,
                                updateAccessTokensUseCase,
                                revertBackToApiTokensUseCase,
                                sendTweetstormUseCase,
                                unsendTweetstormUseCase,
                                unsendMultipleTweetstormsUseCase)
    }

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideDraftsViewModel(draftsCRUDUseCases: DraftsCRUDUseCases): DraftsViewModel {
        return DraftsViewModel(draftsCRUDUseCases)
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