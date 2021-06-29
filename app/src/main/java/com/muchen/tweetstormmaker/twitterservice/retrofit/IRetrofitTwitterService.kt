package com.muchen.tweetstormmaker.twitterservice.retrofit

import com.muchen.tweetstormmaker.twitterservice.retrofit.model.TwitterStatusId
import com.muchen.tweetstormmaker.twitterservice.retrofit.model.TwitterUser
import retrofit2.Call
import retrofit2.http.*

interface IRetrofitTwitterService {

    @GET("1.1/account/verify_credentials.json")
    fun fetchUser(): Call<TwitterUser>

    @FormUrlEncoded
    @POST("1.1/statuses/update.json")
    fun postTweet(@Field("status") status: String): Call<TwitterStatusId>

    @FormUrlEncoded
    @POST("1.1/statuses/update.json")
    fun replyToTweet(@FieldMap options: Map<String, String>): Call<TwitterStatusId>

    @GET("1.1/statuses/show/{id}.json")
    fun findTweetWithId(@Path("id", encoded = true) statusId: String): Call<TwitterStatusId>

    @POST("1.1/statuses/destroy/{id}.json")
    fun deleteTweet(@Path("id", encoded = true) statusId: String): Call<TwitterStatusId>
}