package com.muchen.tweetstormmaker.interfaceadapter.usecase.twitterservice

import com.muchen.tweetstormmaker.interfaceadapter.model.AccessTokens
import com.muchen.tweetstormmaker.interfaceadapter.repository.ITwitterRepository
import com.muchen.tweetstormmaker.interfaceadapter.usecase.InputUseCase

class UpdateAccessTokensUseCase(private val repo: ITwitterRepository) : InputUseCase<AccessTokens> {

    override suspend fun execute(input: AccessTokens) = repo.setAccessTokens(input)
}