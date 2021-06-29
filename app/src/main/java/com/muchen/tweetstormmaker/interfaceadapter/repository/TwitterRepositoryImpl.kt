package com.muchen.tweetstormmaker.interfaceadapter.repository

import com.muchen.tweetstormmaker.interfaceadapter.combineIntoTwitterUserAndTokens
import com.muchen.tweetstormmaker.interfaceadapter.model.AccessTokens
import com.muchen.tweetstormmaker.interfaceadapter.model.TwitterUserAndTokens

class TwitterRepositoryImpl (private val persistence: IPersistence,
                             private val twitterService: ITwitterService): ITwitterRepository{

    override val TWEET_MAX_WEIGHTED_LENGTH = twitterService.TWEET_MAX_WEIGHTED_LENGTH

    override val SHORTENED_URL_LENGTH = twitterService.SHORTENED_URL_LENGTH

    override fun getTwitterUserAndTokens() = persistence.getOneTwitterUserAndTokens()

    override suspend fun deleteAllTwitterUserAndTokens() = persistence.deleteAllTwitterUserAndTokens()

    override suspend fun insertTwitterUserAndTokens(twitterUserAndTokens: TwitterUserAndTokens) = persistence.insertTwitterUserAndTokens(twitterUserAndTokens)

    override suspend fun retrieveAuthorizationUrl() = twitterService.retrieveAuthorizationUrl()

    override suspend fun retrieveTwitterUserAndTokens(pin: String): TwitterUserAndTokens? {
        val accessTokens = twitterService.retrieveAccessTokens(pin) ?: return null
        val twitterUser = twitterService.retrieveTwitterUser() ?: return null
        return combineIntoTwitterUserAndTokens(twitterUser, accessTokens)
    }

    override suspend fun refreshTwitterUser() {
        val twitterUser = twitterService.retrieveTwitterUser()
        if (twitterUser != null) persistence.updateTwitterUser(twitterUser)
    }

    override fun getAccessTokens() = twitterService.getAccessTokens()

    override fun setAccessTokens(accessTokens: AccessTokens) = twitterService.setAccessTokens(accessTokens)

    override fun revertBackToApiTokens() = twitterService.revertBackToApiTokens()

    override suspend fun sendTweet(tweet: String, replyToStatusId: String?) = twitterService.sendTweet(tweet, replyToStatusId)

    override suspend fun deleteTweet(statusId: String) = twitterService.deleteTweet(statusId)

    override suspend fun findTweet(statusId: String) = twitterService.findTweet(statusId)
}