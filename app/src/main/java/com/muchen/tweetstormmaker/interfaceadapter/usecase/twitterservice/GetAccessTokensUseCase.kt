package com.muchen.tweetstormmaker.interfaceadapter.usecase.twitterservice

import com.muchen.tweetstormmaker.interfaceadapter.model.AccessTokens
import com.muchen.tweetstormmaker.interfaceadapter.repository.ITwitterRepository
import com.muchen.tweetstormmaker.interfaceadapter.usecase.OutputUseCase

class GetAccessTokensUseCase (private val repo: ITwitterRepository) : OutputUseCase<AccessTokens?> {

    override suspend fun execute() = repo.getAccessTokens()
}