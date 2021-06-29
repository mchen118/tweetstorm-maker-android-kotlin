package com.muchen.tweetstormmaker.interfaceadapter.repository

import com.muchen.tweetstormmaker.interfaceadapter.model.AccessTokens
import com.muchen.tweetstormmaker.interfaceadapter.model.TwitterUser

interface ITwitterService {

    val TWEET_MAX_WEIGHTED_LENGTH: Int

    val SHORTENED_URL_LENGTH: Int

    suspend fun retrieveAuthorizationUrl(): String?

    suspend fun retrieveAccessTokens(pin: String): AccessTokens?

    suspend fun retrieveTwitterUser(): TwitterUser?

    suspend fun sendTweet(tweet: String, replyToTweetId: String?): String?

    /**
     * Method assumes tweet with statusId exists on Twitter. Use [findTweet] to verify tweet exists.
     */
    suspend fun deleteTweet(statusId: String): Boolean

    suspend fun findTweet(statusId: String): Boolean?

    fun getAccessTokens(): AccessTokens?

    fun setAccessTokens(accessTokens: AccessTokens)

    fun revertBackToApiTokens()
}