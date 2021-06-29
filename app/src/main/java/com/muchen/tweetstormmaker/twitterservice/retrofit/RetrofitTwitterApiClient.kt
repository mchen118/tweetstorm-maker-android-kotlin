package com.muchen.tweetstormmaker.twitterservice.retrofit

import android.util.Log
import com.google.gson.Gson
import com.muchen.tweetstormmaker.twitterservice.TwitterServiceConstants.API_ACCESS_TOKEN_ENDPOINT_URL
import com.muchen.tweetstormmaker.twitterservice.TwitterServiceConstants.API_AUTHORIZE_ENDPOINT_URL
import com.muchen.tweetstormmaker.twitterservice.TwitterServiceConstants.API_BASE_URL
import com.muchen.tweetstormmaker.twitterservice.TwitterServiceConstants.API_DEFAULT_LOGIN_CALL_TIMEOUT_IN_MILLISECONDS
import com.muchen.tweetstormmaker.twitterservice.TwitterServiceConstants.API_DEFAULT_OTHER_CALL_TIMEOUT_IN_MILLISECONDS
import com.muchen.tweetstormmaker.twitterservice.TwitterServiceConstants.API_FIELD_REPLY_TO_STATUS_ID
import com.muchen.tweetstormmaker.twitterservice.TwitterServiceConstants.API_FIELD_STATUS
import com.muchen.tweetstormmaker.twitterservice.TwitterServiceConstants.API_REQUEST_TOKEN_ENDPOINT_URL
import com.muchen.tweetstormmaker.twitterservice.model.AccessTokens
import com.muchen.tweetstormmaker.twitterservice.retrofit.model.TwitterApiErrorResponseBody
import com.muchen.tweetstormmaker.twitterservice.retrofit.model.TwitterStatusId
import com.muchen.tweetstormmaker.twitterservice.retrofit.model.TwitterUser
import oauth.signpost.OAuth
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer
import se.akerfeldt.okhttp.signpost.OkHttpOAuthProvider
import se.akerfeldt.okhttp.signpost.SigningInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

open class RetrofitTwitterApiClient(private val API_KEY: String,
                                    private val API_SECRET: String,
                                    nonOAuthBaseUrl: String = API_BASE_URL,
                                    requestTokenEndPoint: String = API_REQUEST_TOKEN_ENDPOINT_URL,
                                    accessTokenEndPoint: String = API_ACCESS_TOKEN_ENDPOINT_URL,
                                    authorizeEndPoint: String = API_AUTHORIZE_ENDPOINT_URL,
                                    loginCallTimeout: Long = API_DEFAULT_LOGIN_CALL_TIMEOUT_IN_MILLISECONDS,
                                    otherCallTimeout: Long = API_DEFAULT_OTHER_CALL_TIMEOUT_IN_MILLISECONDS) {

    private var consumer: OkHttpOAuthConsumer = OkHttpOAuthConsumer(API_KEY, API_SECRET)
    private var provider: OkHttpOAuthProvider = OkHttpOAuthProvider(
        requestTokenEndPoint,
        accessTokenEndPoint,
        authorizeEndPoint,
        OkHttpClient.Builder()
            .callTimeout(loginCallTimeout, TimeUnit.MILLISECONDS)
            .build()
    )

    private val retrofitTwitterService by lazy {
        val client = OkHttpClient.Builder()
                .addInterceptor(SigningInterceptor(consumer))
                .callTimeout(otherCallTimeout, TimeUnit.MILLISECONDS)
                .build()

        Retrofit.Builder()
                .baseUrl(nonOAuthBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(IRetrofitTwitterService::class.java)
    }

    fun getAccessTokens(): AccessTokens = AccessTokens(consumer.token, consumer.tokenSecret)

    fun setAccessTokens(accessTokens: AccessTokens) =
        consumer.setTokenWithSecret(accessTokens.accessToken, accessTokens.accessTokenSecret)

    fun revertBackToApiTokens() = consumer.setTokenWithSecret(API_KEY, API_SECRET)

    fun retrieveAuthorizationUrl(): String? {
        return try {
            provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND)
        } catch (e: java.lang.Exception) {
            mLog(this::class.java.simpleName, "Exception happened in retrieving authorization url: ${e.message}", "E")
            null
        }
    }

    fun retrieveAccessTokens(pin: String): AccessTokens? {
        return try {
            provider.retrieveAccessToken(consumer, pin)
            AccessTokens(consumer.token, consumer.tokenSecret)
        } catch (e: java.lang.Exception) {
            mLog(this::class.java.simpleName, "Exception happened in retrieving access tokens: ${e.message}", "E")
            null
        }
    }

    fun retrieveTwitterUser(): TwitterUser? {
        val response: Response<TwitterUser>

        try {
            response = retrofitTwitterService.fetchUser().execute()
        } catch (e: IOException) {
            mLog(this::class.java.simpleName, "IOException happened in retrieving Twitter user info: ${e.message}", "E")
            return null
        }

        return if (response.isSuccessful && response.body() != null) {
            response.body() as TwitterUser
        } else {
            logTwitterApiError(this::class.java.simpleName, "Get Twitter User Info", response.errorBody(), response.code())
            null
        }
    }

    fun sendTweet(tweet: String, replyToTweetId: String?): String? {
        val response: Response<TwitterStatusId>
        try {
            response =
                if (replyToTweetId == null) {
                    retrofitTwitterService.postTweet(tweet).execute()
                } else {
                    val status = Pair(API_FIELD_STATUS, tweet)
                    val replyId = Pair(API_FIELD_REPLY_TO_STATUS_ID, replyToTweetId)
                    retrofitTwitterService.replyToTweet(mapOf(status, replyId)).execute()
                }
        } catch (e: IOException) {
            mLog(this::class.java.simpleName, "IOException happened while sending tweet: ${e.message}", "E")
            return null
        }

        return if (response.isSuccessful && response.body() != null) {
            (response.body() as TwitterStatusId).statusId
        } else {
            logTwitterApiError(this::class.java.simpleName, "Send tweet", response.errorBody(), response.code())
            null
        }

    }

    /**
     * Method assumes tweet with [statusId] exists on Twitter. Use [findTweet] to verify tweet exists.
     */
    fun deleteTweet(statusId: String): Boolean {
        val response: Response<TwitterStatusId>
        try {
            response = retrofitTwitterService.deleteTweet(statusId).execute()
        } catch (e: IOException) {
            mLog(this::class.java.simpleName, "IOException happened while deleting tweet: ${e.message}", "E")
            return false
        }

        return if (response.isSuccessful && response.body() != null) {
            (response.body() as TwitterStatusId).statusId == statusId
        } else {
            logTwitterApiError(this::class.java.simpleName, "Delete tweet", response.errorBody(), response.code())
            false
        }
    }

    fun findTweet(statusId: String): Boolean? {
        val response: Response<TwitterStatusId>
        try {
            response = retrofitTwitterService.findTweetWithId(statusId).execute()
        } catch (e: IOException) {
            mLog(this::class.java.simpleName, "IOException happened while finding tweet: ${e.message}", "E")
            return null
        }

        return if (response.isSuccessful && response.body() != null) {
            (response.body() as TwitterStatusId).statusId == statusId
        } else {
            logTwitterApiError(this::class.java.simpleName, "Find tweet", response.errorBody(), response.code())
            false
        }
    }

    /**
     * Function can be overridden to decouple from Android's Log class.
     */
    protected open fun mLog(tag: String, message: String, type: String) {
        when (type) {
            "E" -> Log.e(tag, message)
            "D" -> Log.d(tag, message)
            "I" -> Log.i(tag, message)
            "V" -> Log.v(tag, message)
            "W" -> Log.w(tag, message)
            else -> Log.d(tag, message)
        }
    }

    private fun logTwitterApiError(tag: String, requestType: String, errorBody: ResponseBody?, httpCode: Int) {
        val errorBodyReader = errorBody?.charStream()
        if (errorBodyReader == null) {
            mLog(tag, "$requestType failed with HTTP response code: $httpCode.", "E")
        } else {
            val body = Gson().fromJson(errorBodyReader, TwitterApiErrorResponseBody::class.java)
            val stringBuilder = StringBuilder()
            if (body != null) {
                for (error in body.errors) {
                    stringBuilder.append("Twitter Error ${error.code}: ${error.message}\n")
                }
                stringBuilder.delete(stringBuilder.length - 1, stringBuilder.length)
            }
            mLog(tag, "$requestType failed with HTTP response code: $httpCode.\n\t\t" +
                    "Details: $stringBuilder", "E")
        }
    }
}
