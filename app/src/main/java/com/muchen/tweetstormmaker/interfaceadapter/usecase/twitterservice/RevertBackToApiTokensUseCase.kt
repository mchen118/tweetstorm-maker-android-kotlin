package com.muchen.tweetstormmaker.interfaceadapter.usecase.twitterservice

import com.muchen.tweetstormmaker.interfaceadapter.repository.ITwitterRepository
import com.muchen.tweetstormmaker.interfaceadapter.usecase.NoInputNoOutputUseCase

class RevertBackToApiTokensUseCase(private val repo: ITwitterRepository) : NoInputNoOutputUseCase {

    override suspend fun execute() = repo.revertBackToApiTokens()
}