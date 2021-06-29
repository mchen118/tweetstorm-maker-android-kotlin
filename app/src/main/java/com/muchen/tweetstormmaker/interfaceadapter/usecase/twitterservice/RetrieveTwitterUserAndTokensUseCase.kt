package com.muchen.tweetstormmaker.interfaceadapter.usecase.twitterservice

import com.muchen.tweetstormmaker.interfaceadapter.model.TwitterUserAndTokens
import com.muchen.tweetstormmaker.interfaceadapter.repository.ITwitterRepository
import com.muchen.tweetstormmaker.interfaceadapter.usecase.InputOutputUseCase


class RetrieveTwitterUserAndTokensUseCase(private val repo: ITwitterRepository):
        InputOutputUseCase<String, TwitterUserAndTokens?> {

    override suspend fun execute(input: String): TwitterUserAndTokens? = repo.retrieveTwitterUserAndTokens(input)
}