package com.muchen.tweetstormmaker.twitterservice

import com.muchen.tweetstormmaker.interfaceadapter.model.AccessTokens
import com.muchen.tweetstormmaker.interfaceadapter.model.TwitterUser
import com.muchen.tweetstormmaker.interfaceadapter.repository.ITwitterService
import com.muchen.tweetstormmaker.twitterservice.mapper.toIAModel
import com.muchen.tweetstormmaker.twitterservice.mapper.toTSModel
import com.muchen.tweetstormmaker.twitterservice.retrofit.RetrofitTwitterApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TwitterServiceImpl private constructor(private val twitterApiClient: RetrofitTwitterApiClient)
    : ITwitterService {

    companion object {

        @Volatile private var soleInstance: ITwitterService? = null

        fun getInstance(twitterApiClient: RetrofitTwitterApiClient): ITwitterService {
            return soleInstance ?: synchronized(this) {
                soleInstance ?: TwitterServiceImpl(twitterApiClient)
            }
        }
    }

    override val TWEET_MAX_WEIGHTED_LENGTH = TwitterServiceConstants.API_TWEET_MAX_WEIGHTED_LENGTH

    override val SHORTENED_URL_LENGTH = TwitterServiceConstants.API_SHORTENED_URL_LENGTH

    override fun getAccessTokens() =
            twitterApiClient.getAccessTokens().toIAModel()

    override fun setAccessTokens(accessTokens: AccessTokens) =
            twitterApiClient.setAccessTokens(accessTokens.toTSModel())

    override fun revertBackToApiTokens() =
            twitterApiClient.revertBackToApiTokens()

    override suspend fun retrieveAuthorizationUrl(): String? {
        return withContext(Dispatchers.IO) {
            twitterApiClient.retrieveAuthorizationUrl()
        }
    }

    override suspend fun retrieveAccessTokens(pin: String): AccessTokens? {
        return withContext(Dispatchers.IO) {
            twitterApiClient.retrieveAccessTokens(pin)?.toIAModel()
        }
    }

    override suspend fun retrieveTwitterUser(): TwitterUser? {
        return withContext(Dispatchers.IO) {
            twitterApiClient.retrieveTwitterUser()?.toIAModel()
        }
    }

    override suspend fun sendTweet(tweet: String, replyToTweetId: String?): String? {
        return withContext(Dispatchers.IO) {
            twitterApiClient.sendTweet(tweet, replyToTweetId)
        }
    }

    /**
     * Method assumes tweet with statusId exists on Twitter. Use [findTweet] to verify tweet exists.
     */
    override suspend fun deleteTweet(statusId: String): Boolean {
        return withContext(Dispatchers.IO) {
            twitterApiClient.deleteTweet(statusId)
        }
    }

    override suspend fun findTweet(statusId: String): Boolean? {
        return withContext(Dispatchers.IO) {
            twitterApiClient.findTweet(statusId)
        }
    }
}