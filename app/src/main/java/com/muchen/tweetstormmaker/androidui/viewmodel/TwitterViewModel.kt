package com.muchen.tweetstormmaker.androidui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.muchen.tweetstormmaker.androidui.mapper.toIAModel
import com.muchen.tweetstormmaker.androidui.mapper.toUIModel
import com.muchen.tweetstormmaker.androidui.model.*
import com.muchen.tweetstormmaker.interfaceadapter.model.SendTweetstormUseCaseInput
import com.muchen.tweetstormmaker.interfaceadapter.model.UnsendMultipleTweetstormsResultEnum
import com.muchen.tweetstormmaker.interfaceadapter.model.UnsendTweetStormResultEnum
import com.muchen.tweetstormmaker.interfaceadapter.usecase.database.DraftsCRUDUseCases
import com.muchen.tweetstormmaker.interfaceadapter.usecase.database.TwitterUserAndTokensCRUDUseCases
import com.muchen.tweetstormmaker.interfaceadapter.usecase.twitterservice.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TwitterViewModel(private val twitterUserAndTokensCRUDUseCases: TwitterUserAndTokensCRUDUseCases,
                       private val draftsCRUDUseCases: DraftsCRUDUseCases,

                       private val retrieveAuthorizationUrlUseCase: RetrieveAuthorizationUrlUseCase,
                       private val retrieveTwitterUserAndTokensUseCase: RetrieveTwitterUserAndTokensUseCase,

                       private val updateAccessTokensUseCase: UpdateAccessTokensUseCase,
                       private val revertBackToApiTokensUseCase: RevertBackToApiTokensUseCase,

                       private val sendTweetstormUseCase: SendTweetstormUseCase,
                       private val unsendTweetstormUseCase: UnsendTweetstormUseCase,
                       private val unsendMultipleTweetstormsUseCase: UnsendMultipleTweetstormsUseCase)
    : ViewModel() {

    val twitterUserAndTokens =
            twitterUserAndTokensCRUDUseCases.getTwitterUserAndTokens().toUIModel().asLiveData()

    val authorizationUrl = MutableLiveData<String?>(null)

    val showNotification = MutableLiveData<NotificationEnum?>(null)

    val showProgressIndicator: LiveData<Boolean>
        get() = _showProgressIndicator

    private val _showProgressIndicator = MutableLiveData<Boolean>(false)

    private val scope = viewModelScope

    fun logout() {
        authorizationUrl.value = null
        scope.launch(Dispatchers.Main) {
            twitterUserAndTokensCRUDUseCases.clearTwitterUserAndTokens()
            revertBackToApiTokensUseCase.execute()
        }
    }

    fun startLogin() {
        _showProgressIndicator.value = true
        scope.launch(Dispatchers.Main) {
            val url = retrieveAuthorizationUrlUseCase.execute()
            _showProgressIndicator.value = false

            when (url) {
                null -> {
                    if (authorizationUrl.value != null) authorizationUrl.value = null
                    showNotification.value = NotificationEnum.LOGIN_FAILED
                }
                else ->authorizationUrl.value = url
            }
        }
    }

    fun finishLogin(pin: String) {
        _showProgressIndicator.value = true
        scope.launch(Dispatchers.Main) {
            val twitterUserAndTokens = retrieveTwitterUserAndTokensUseCase.execute(pin)
            _showProgressIndicator.value = false
            when (twitterUserAndTokens) {
                null -> showNotification.value = NotificationEnum.LOGIN_FAILED
                else -> {
                    twitterUserAndTokensCRUDUseCases.insertTwitterUserAndTokens(
                            twitterUserAndTokens)
                    showNotification.value = NotificationEnum.LOGIN_SUCCESSFUL
                }
            }
        }
    }

    fun updateTokensWith(accessTokens: AccessTokens) {
        scope.launch(Dispatchers.Main) {
            updateAccessTokensUseCase.execute(accessTokens.toIAModel())
        }
    }

    fun refreshTwitterUser() {
        scope.launch(Dispatchers.IO) {
            twitterUserAndTokensCRUDUseCases.refreshTwitterUser()
        }
    }

    fun sendTweetStorm(draftContent: DraftContent, numberingTweets: Boolean) {
        _showProgressIndicator.value = true
        scope.launch(Dispatchers.Main) {
            val input = SendTweetstormUseCaseInput(draftContent.toIAModel(),
                "@${twitterUserAndTokens.value!!.screenName}",
                numberingTweets = numberingTweets)
            val updatedSentStatus = sendTweetstormUseCase.execute(input)
            draftsCRUDUseCases.updateDraftSentStatus(updatedSentStatus)
            _showProgressIndicator.value = false
            showNotification.value = when (updatedSentStatus.sentStatus.toUIModel()) {
                SentStatusEnum.FULLY_SENT ->
                        NotificationEnum.SEND_TWEETSTORM_SUCCESSFUL
                SentStatusEnum.PARTIALLY_SENT ->
                        NotificationEnum.SEND_TWEETSTORM_FAILED_SOME_TWEETS_SENT
                else -> NotificationEnum.SEND_TWEETSTORM_FAILED_NO_TWEET_SENT
            }
        }
    }

    fun unsendTweetstorm(tweetstorm: Draft, keepInDraft: Boolean) {
        _showProgressIndicator.value = true
        scope.launch(Dispatchers.Main) {
            val output = unsendTweetstormUseCase.execute(tweetstorm.toIAModel())
            if (!keepInDraft && (output.resultEnum == UnsendTweetStormResultEnum.FULLY_UNSENT)) {
                draftsCRUDUseCases.deleteDraftByTimeCreated(tweetstorm.timeCreated)
            } else {
                draftsCRUDUseCases.updateDraftSentStatus(output.updatedDraftSentStatus)
            }
            _showProgressIndicator.value = false
            showNotification.value = when (output.resultEnum) {
                UnsendTweetStormResultEnum.FULLY_UNSENT ->
                        NotificationEnum.UNSEND_TWEETSTORM_SUCCESSFUL
                UnsendTweetStormResultEnum.PARTIALLY_UNSENT ->
                        NotificationEnum.UNSEND_TWEETSTORM_FAILED_SOME_TWEETS_UNSENT
                UnsendTweetStormResultEnum.NO_TWEET_UNSENT ->
                        NotificationEnum.UNSEND_TWEETSTORM_FAILED_NO_TWEET_UNSENT
            }
        }
    }

    fun unsendTweetstorms(tweetstorms: List<Draft>) {
        _showProgressIndicator.value = true
        scope.launch(Dispatchers.Main) {
            val output = unsendMultipleTweetstormsUseCase.execute(tweetstorms.toIAModel())
            _showProgressIndicator.value = false
            showNotification.value = when (output.resultEnum) {
                UnsendMultipleTweetstormsResultEnum.FULLY_UNSENT ->
                        NotificationEnum.UNSEND_TWEETSTORMS_SUCCESSFUL
                UnsendMultipleTweetstormsResultEnum.SOME_TWEETSTORMS_UNSENT ->
                        NotificationEnum.UNSEND_TWEETSTORMS_FAILED_SOME_TWEETSTORMS_UNSENT
                UnsendMultipleTweetstormsResultEnum.NO_TWEETSTORM_UNSENT ->
                        NotificationEnum.UNSEND_TWEETSTORMS_FAILED_NO_TWEETSTORM_UNSENT
                UnsendMultipleTweetstormsResultEnum.NO_TWEET_UNSENT ->
                        NotificationEnum.UNSEND_TWEETSTORMS_FAILED_NO_TWEET_UNSENT
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(this::class.simpleName, "onCleared()")
    }
}