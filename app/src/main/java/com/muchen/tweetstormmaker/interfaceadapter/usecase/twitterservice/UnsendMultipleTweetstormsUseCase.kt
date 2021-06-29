package com.muchen.tweetstormmaker.interfaceadapter.usecase.twitterservice

import com.muchen.tweetstormmaker.interfaceadapter.model.*
import com.muchen.tweetstormmaker.interfaceadapter.repository.IDraftsRepository
import com.muchen.tweetstormmaker.interfaceadapter.repository.ITwitterRepository
import com.muchen.tweetstormmaker.interfaceadapter.usecase.InputOutputUseCase

class UnsendMultipleTweetstormsUseCase(private val twitterApiRepo: ITwitterRepository,
                                       private val draftsRepo: IDraftsRepository)
    : InputOutputUseCase<List<Draft>, UnsendMultipleTweetstormsUseCaseOutput> {

    override suspend fun execute(input: List<Draft>): UnsendMultipleTweetstormsUseCaseOutput {
        var outputEnum = UnsendMultipleTweetstormsResultEnum.FULLY_UNSENT
        val outputList = ArrayList<DraftSentStatus>()
        for (i in input.indices) {
            val result = UnsendTweetstormUseCase(twitterApiRepo).execute(input[i])
            draftsRepo.updateDraftSentStatus(result.updatedDraftSentStatus)
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
}