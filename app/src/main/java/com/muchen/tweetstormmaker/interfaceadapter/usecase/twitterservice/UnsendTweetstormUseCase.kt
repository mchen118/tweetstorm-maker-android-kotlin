package com.muchen.tweetstormmaker.interfaceadapter.usecase.twitterservice

import android.util.Log
import com.muchen.tweetstormmaker.interfaceadapter.model.Draft
import com.muchen.tweetstormmaker.interfaceadapter.model.SentStatusEnum
import com.muchen.tweetstormmaker.interfaceadapter.model.UnsendTweetStormResultEnum
import com.muchen.tweetstormmaker.interfaceadapter.model.UnsendTweetstormUseCaseOutput
import com.muchen.tweetstormmaker.interfaceadapter.repository.ITwitterRepository
import com.muchen.tweetstormmaker.interfaceadapter.toCSVString
import com.muchen.tweetstormmaker.interfaceadapter.toDraftSentStatus
import com.muchen.tweetstormmaker.interfaceadapter.usecase.InputOutputUseCase

class UnsendTweetstormUseCase(private val repo: ITwitterRepository)
    : InputOutputUseCase<Draft, UnsendTweetstormUseCaseOutput> {

    override suspend fun execute(input: Draft): UnsendTweetstormUseCaseOutput {
        var hasEncounteredFailure = false
        val remainingStatusIdList = ArrayList<String>(input.sentIds.split(","))
        for (i in remainingStatusIdList.size -1 downTo 0) {
            when (repo.findTweet(remainingStatusIdList[i])) {
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
            if (repo.deleteTweet(remainingStatusIdList[i])) remainingStatusIdList.removeAt(i)
            else {
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
                sentStatus = SentStatusEnum.PARTIALLY_SENT
            } else if (resultEnum == UnsendTweetStormResultEnum.FULLY_UNSENT) {
                sentStatus = SentStatusEnum.LOCAL
            }
        }
        return UnsendTweetstormUseCaseOutput(newDraftSentStatus, resultEnum)
    }

    companion object {
        const val TAG = "UnsendTweetstormUseCase"
    }
}