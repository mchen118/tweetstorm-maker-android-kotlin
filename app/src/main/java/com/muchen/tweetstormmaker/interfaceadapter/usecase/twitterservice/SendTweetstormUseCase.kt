package com.muchen.tweetstormmaker.interfaceadapter.usecase.twitterservice

import com.muchen.tweetstormmaker.interfaceadapter.TextToTweetsProcessor
import com.muchen.tweetstormmaker.interfaceadapter.model.DraftSentStatus
import com.muchen.tweetstormmaker.interfaceadapter.model.SendTweetstormUseCaseInput
import com.muchen.tweetstormmaker.interfaceadapter.model.SentStatusEnum
import com.muchen.tweetstormmaker.interfaceadapter.repository.ITwitterRepository
import com.muchen.tweetstormmaker.interfaceadapter.toCSVString
import com.muchen.tweetstormmaker.interfaceadapter.usecase.InputOutputUseCase

class SendTweetstormUseCase(private val repo: ITwitterRepository)
    : InputOutputUseCase<SendTweetstormUseCaseInput, DraftSentStatus> {

    override suspend fun execute(input: SendTweetstormUseCaseInput): DraftSentStatus {
        val sentStatusIdList = ArrayList<String>()
        val processor = TextToTweetsProcessor(input.draftContent.content,
                                              input.twitterHandle,
                                              input.twitterHandlePostfix,
                                              input.tweetNumberPrefix,
                                              input.numberingTweets,
                                              repo.TWEET_MAX_WEIGHTED_LENGTH,
                                              repo.SHORTENED_URL_LENGTH)
        var previousStatusId: String? = null

        while(processor.hasNextTweet()) {
            previousStatusId = repo.sendTweet(processor.nextTweet(), previousStatusId)
            if (previousStatusId == null) break
            else sentStatusIdList.add(previousStatusId)
        }

        val output = DraftSentStatus(input.draftContent.timeCreated, SentStatusEnum.LOCAL,
                sentStatusIdList.toCSVString())

        if (previousStatusId == null) {
            if (sentStatusIdList.isEmpty()) {
                output.sentStatus = SentStatusEnum.LOCAL
            } else {
                output.sentStatus = SentStatusEnum.PARTIALLY_SENT
            }
        } else {
            output.sentStatus = SentStatusEnum.FULLY_SENT
        }
        return output
    }
}