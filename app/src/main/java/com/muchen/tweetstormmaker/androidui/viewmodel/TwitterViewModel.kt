package com.muchen.tweetstormmaker.androidui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.muchen.tweetstormmaker.androidui.mapper.toIAModel
import com.muchen.tweetstormmaker.androidui.mapper.toUIModel
import com.muchen.tweetstormmaker.androidui.model.AccessTokens
import com.muchen.tweetstormmaker.androidui.model.Draft
import com.muchen.tweetstormmaker.androidui.model.DraftContent
import com.muchen.tweetstormmaker.androidui.model.NotificationEnum
import com.muchen.tweetstormmaker.androidui.model.SentStatusEnum
import com.muchen.tweetstormmaker.interfaceadapter.TextToTweetsProcessor
import com.muchen.tweetstormmaker.interfaceadapter.combineIntoTwitterUserAndTokens
import com.muchen.tweetstormmaker.interfaceadapter.model.DraftSentStatus
import com.muchen.tweetstormmaker.interfaceadapter.model.AccessTokens as IAAccessTokens
import com.muchen.tweetstormmaker.interfaceadapter.model.TwitterUserAndTokens as IATwitterUserAndTokens
import com.muchen.tweetstormmaker.interfaceadapter.model.SendTweetstormUseCaseInput
import com.muchen.tweetstormmaker.interfaceadapter.model.TwitterUser as IATwitterUser
import com.muchen.tweetstormmaker.interfaceadapter.model.UnsendMultipleTweetstormsResultEnum
import com.muchen.tweetstormmaker.interfaceadapter.model.UnsendMultipleTweetstormsUseCaseOutput
import com.muchen.tweetstormmaker.interfaceadapter.model.UnsendTweetStormResultEnum
import com.muchen.tweetstormmaker.interfaceadapter.model.UnsendTweetstormUseCaseOutput
import com.muchen.tweetstormmaker.interfaceadapter.repository.IPersistence
import com.muchen.tweetstormmaker.interfaceadapter.repository.ITwitterService
import com.muchen.tweetstormmaker.interfaceadapter.toCSVString
import com.muchen.tweetstormmaker.interfaceadapter.toDraftSentStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TwitterViewModel(private val persistence: IPersistence,
                       private val twitterService: ITwitterService)
    : ViewModel() {

    val twitterUserAndTokens =
        persistence.getOneTwitterUserAndTokens().toUIModel().asLiveData()

    val authorizationUrl = MutableLiveData<String?>(null)

    val showNotification = MutableLiveData<NotificationEnum?>(null)

    val showProgressIndicator: LiveData<Boolean>
        get() = _showProgressIndicator

    private val _showProgressIndicator = MutableLiveData<Boolean>(false)

    private val scope = viewModelScope

    fun logout() {
        authorizationUrl.value = null
        scope.launch(Dispatchers.Main) {
            persistence.deleteAllTwitterUserAndTokens()
            twitterService.revertBackToApiTokens()
        }
    }

    fun startLogin() {
        _showProgressIndicator.value = true
        scope.launch(Dispatchers.Main) {
            val url = twitterService.retrieveAuthorizationUrl()
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
            var accessTokens: IAAccessTokens? = null
            var twitterUser: IATwitterUser? = null
            var twitterUserAndTokens: IATwitterUserAndTokens? = null
            accessTokens = twitterService.retrieveAccessTokens(pin)
            if (accessTokens != null) {
                twitterUser = twitterService.retrieveTwitterUser()
                if (twitterUser != null) {
                    twitterUserAndTokens = combineIntoTwitterUserAndTokens(twitterUser, accessTokens)
                }
            }

            _showProgressIndicator.value = false
            when (twitterUserAndTokens) {
                null -> showNotification.value = NotificationEnum.LOGIN_FAILED
                else -> {
                    persistence.insertTwitterUserAndTokens(
                            twitterUserAndTokens)
                    showNotification.value = NotificationEnum.LOGIN_SUCCESSFUL
                }
            }
        }
    }

    fun updateTokensWith(accessTokens: AccessTokens) {
        scope.launch(Dispatchers.Main) {
            twitterService.setAccessTokens(accessTokens.toIAModel())
        }
    }

    fun refreshTwitterUser() {
        scope.launch(Dispatchers.IO) {
            val twitterUser = twitterService.retrieveTwitterUser()
            if (twitterUser != null) persistence.updateTwitterUser(twitterUser)
        }
    }

    private suspend fun _sendTweetStorm(input: SendTweetstormUseCaseInput): DraftSentStatus {
        val sentStatusIdList = ArrayList<String>()
        val processor = TextToTweetsProcessor(input.draftContent.content,
            input.twitterHandle,
            input.twitterHandlePostfix,
            input.tweetNumberPrefix,
            input.numberingTweets,
            twitterService.TWEET_MAX_WEIGHTED_LENGTH,
            twitterService.SHORTENED_URL_LENGTH)
        var previousStatusId: String? = null

        while (processor.hasNextTweet()) {
            previousStatusId = twitterService.sendTweet(processor.nextTweet(), previousStatusId)
            if (previousStatusId == null) break
            else sentStatusIdList.add(previousStatusId)
        }

        val output = DraftSentStatus(input.draftContent.timeCreated, com.muchen.tweetstormmaker.interfaceadapter.model.SentStatusEnum.LOCAL,
            sentStatusIdList.toCSVString())

        if (previousStatusId == null) {
            if (sentStatusIdList.isEmpty()) {
                output.sentStatus = com.muchen.tweetstormmaker.interfaceadapter.model.SentStatusEnum.LOCAL
            } else {
                output.sentStatus = com.muchen.tweetstormmaker.interfaceadapter.model.SentStatusEnum.PARTIALLY_SENT
            }
        } else {
            output.sentStatus = com.muchen.tweetstormmaker.interfaceadapter.model.SentStatusEnum.FULLY_SENT
        }
        return output
    }

    fun sendTweetStorm(draftContent: DraftContent, numberingTweets: Boolean) {
        _showProgressIndicator.value = true
        scope.launch(Dispatchers.Main) {
            val input = SendTweetstormUseCaseInput(draftContent.toIAModel(),
                "@${twitterUserAndTokens.value!!.screenName}",
                numberingTweets = numberingTweets)
            val updatedSentStatus = _sendTweetStorm(input)

            persistence.updateDraftSentStatus(updatedSentStatus)
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

    private suspend fun _unsendTweetStorm(input: com.muchen.tweetstormmaker.interfaceadapter.model.Draft)
            : UnsendTweetstormUseCaseOutput {
        var hasEncounteredFailure = false
        val remainingStatusIdList = ArrayList<String>(input.sentIds.split(","))
        for (i in remainingStatusIdList.size -1 downTo 0) {
            when (twitterService.findTweet(remainingStatusIdList[i])) {
                true -> continue
                false -> remainingStatusIdList.removeAt(i)
                null -> {
                    hasEncounteredFailure = true
                    break
                }
            }
        }

        if (hasEncounteredFailure) {
            Log.e(TAG, "has encountered failure")
            val newDraftSentStatus = input.toDraftSentStatus().apply {
                sentIds = remainingStatusIdList.toCSVString()
            }
            return UnsendTweetstormUseCaseOutput(newDraftSentStatus, UnsendTweetStormResultEnum.NO_TWEET_UNSENT)
        }

        var resultEnum = UnsendTweetStormResultEnum.FULLY_UNSENT
        val remainingStatusIdListSize = remainingStatusIdList.size
        Log.d(TAG, "remaining tweets: $remainingStatusIdListSize")
        for (i in remainingStatusIdListSize - 1 downTo 0) {
            if (twitterService.deleteTweet(remainingStatusIdList[i])) {
                remainingStatusIdList.removeAt(i)
            } else {
                resultEnum = when (i) {
                    remainingStatusIdListSize - 1 -> UnsendTweetStormResultEnum.NO_TWEET_UNSENT
                    else -> UnsendTweetStormResultEnum.PARTIALLY_UNSENT
                }
                break
            }
        }

        val newDraftSentStatus = input.toDraftSentStatus().apply {
            sentIds = remainingStatusIdList.toCSVString()
            if (resultEnum == UnsendTweetStormResultEnum.PARTIALLY_UNSENT) {
                sentStatus = com.muchen.tweetstormmaker.interfaceadapter.model.SentStatusEnum.PARTIALLY_SENT
            } else if (resultEnum == UnsendTweetStormResultEnum.FULLY_UNSENT) {
                sentStatus = com.muchen.tweetstormmaker.interfaceadapter.model.SentStatusEnum.LOCAL
            }
        }
        return UnsendTweetstormUseCaseOutput(newDraftSentStatus, resultEnum)
    }

    fun unsendTweetstorm(tweetstorm: Draft, keepInDraft: Boolean) {
        _showProgressIndicator.value = true
        scope.launch(Dispatchers.Main) {
            val output = _unsendTweetStorm(tweetstorm.toIAModel())
            if (!keepInDraft && (output.resultEnum == UnsendTweetStormResultEnum.FULLY_UNSENT)) {
                persistence.deleteDraftByTimeCreated(tweetstorm.timeCreated)
            } else {
                persistence.updateDraftSentStatus(output.updatedDraftSentStatus)
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

    private suspend fun _unsendTweetstorms(input: List<com.muchen.tweetstormmaker.interfaceadapter.model.Draft>)
            : UnsendMultipleTweetstormsUseCaseOutput {
        var outputEnum = UnsendMultipleTweetstormsResultEnum.FULLY_UNSENT
        val outputList = ArrayList<DraftSentStatus>()
        for (i in input.indices) {
            val result = _unsendTweetStorm(input[i])
            persistence.updateDraftSentStatus(result.updatedDraftSentStatus)
            outputList.add(result.updatedDraftSentStatus)
            when (result.resultEnum) {
                UnsendTweetStormResultEnum.FULLY_UNSENT -> continue
                UnsendTweetStormResultEnum.PARTIALLY_UNSENT -> {
                    outputEnum = if (i == 0) {
                        UnsendMultipleTweetstormsResultEnum.NO_TWEETSTORM_UNSENT
                    } else {
                        UnsendMultipleTweetstormsResultEnum.SOME_TWEETSTORMS_UNSENT
                    }
                    break
                }
                UnsendTweetStormResultEnum.NO_TWEET_UNSENT -> {
                    outputEnum = UnsendMultipleTweetstormsResultEnum.NO_TWEET_UNSENT
                    break
                }
            }
        }
        return UnsendMultipleTweetstormsUseCaseOutput(outputList, outputEnum)
    }

    fun unsendTweetstorms(tweetstorms: List<Draft>) {
        _showProgressIndicator.value = true
        scope.launch(Dispatchers.Main) {
            val output = _unsendTweetstorms(tweetstorms.toIAModel())
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

    companion object {
        const val TAG = "TwitterViewModel"
    }
}