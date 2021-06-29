package com.muchen.tweetstormmaker.interfaceadapter.repository

import com.muchen.tweetstormmaker.interfaceadapter.model.AccessTokens
import com.muchen.tweetstormmaker.interfaceadapter.model.TwitterUserAndTokens
import kotlinx.coroutines.flow.Flow

interface ITwitterRepository {

    val TWEET_MAX_WEIGHTED_LENGTH: Int

    val SHORTENED_URL_LENGTH: Int

    fun getTwitterUserAndTokens(): Flow<TwitterUserAndTokens?>

    suspend fun deleteAllTwitterUserAndTokens()

    suspend fun insertTwitterUserAndTokens(twitterUserAndTokens: TwitterUserAndTokens)

    suspend fun retrieveAuthorizationUrl(): String?

    suspend fun retrieveTwitterUserAndTokens(pin: String): TwitterUserAndTokens?

    suspend fun refreshTwitterUser()

    suspend fun sendTweet(tweet: String, replyToStatusId: String?): String?

    suspend fun deleteTweet(statusId: String): Boolean

    suspend fun findTweet(statusId: String): Boolean?

    fun getAccessTokens(): AccessTokens?

    fun setAccessTokens(accessTokens: AccessTokens)

    fun revertBackToApiTokens()
}