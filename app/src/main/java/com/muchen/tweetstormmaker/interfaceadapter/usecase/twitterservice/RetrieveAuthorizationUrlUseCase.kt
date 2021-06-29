package com.muchen.tweetstormmaker.interfaceadapter.usecase.twitterservice

import com.muchen.tweetstormmaker.interfaceadapter.repository.ITwitterRepository
import com.muchen.tweetstormmaker.interfaceadapter.usecase.OutputUseCase

class RetrieveAuthorizationUrlUseCase(private val repo: ITwitterRepository) : OutputUseCase<String?> {

    override suspend fun execute() = repo.retrieveAuthorizationUrl()
}