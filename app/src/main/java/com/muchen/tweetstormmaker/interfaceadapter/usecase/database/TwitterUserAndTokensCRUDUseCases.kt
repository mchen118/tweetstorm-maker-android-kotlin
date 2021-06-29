package com.muchen.tweetstormmaker.interfaceadapter.usecase.database

import com.muchen.tweetstormmaker.interfaceadapter.model.TwitterUserAndTokens
import com.muchen.tweetstormmaker.interfaceadapter.repository.ITwitterRepository

class TwitterUserAndTokensCRUDUseCases(private val repo: ITwitterRepository) {

    fun getTwitterUserAndTokens() = repo.getTwitterUserAndTokens()

    suspend fun refreshTwitterUser() = repo.refreshTwitterUser()

    suspend fun clearTwitterUserAndTokens() = repo.deleteAllTwitterUserAndTokens()

    suspend fun insertTwitterUserAndTokens(twitterUserAndTokens: TwitterUserAndTokens) =
            repo.insertTwitterUserAndTokens(twitterUserAndTokens)

}