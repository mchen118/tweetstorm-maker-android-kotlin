package com.muchen.tweetstormmaker.twitterservice.retrofit

import com.muchen.tweetstormmaker.twitterservice.retrofit.model.DeletedWrapper
import com.muchen.tweetstormmaker.twitterservice.retrofit.model.FindTweetResultWrapper
import com.muchen.tweetstormmaker.twitterservice.retrofit.model.ReplyWrapper
import com.muchen.tweetstormmaker.twitterservice.retrofit.model.TweetIdWrapper
import com.muchen.tweetstormmaker.twitterservice.retrofit.model.TwitterUser
import retrofit2.Call
import retrofit2.http.*

interface IRetrofitTwitterService {

    @GET("1.1/account/verify_credentials.json")
    fun fetchUser(): Call<TwitterUser>

    @Headers("Content-Type: application/json")
    @POST("2/tweets")
    fun postTweet(@Body map: Map<String, String>): Call<TweetIdWrapper>

    @Headers("Content-Type: application/json")
    @POST("2/tweets")
    fun replyToTweet(@Body replyToTweet: ReplyWrapper): Call<TweetIdWrapper>

    @GET("2/tweets/{id}")
    fun findTweetWithId(@Path("id", encoded = true) id: String): Call<FindTweetResultWrapper>

    @DELETE("2/tweets/{id}")
    fun deleteTweet(@Path("id", encoded = true) id: String): Call<DeletedWrapper>
}