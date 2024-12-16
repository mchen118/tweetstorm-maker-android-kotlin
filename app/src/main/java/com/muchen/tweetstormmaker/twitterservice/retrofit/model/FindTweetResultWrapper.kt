package com.muchen.tweetstormmaker.twitterservice.retrofit.model

import com.google.gson.annotations.SerializedName

class FindTweetResultWrapper (
    @SerializedName("data")
    val data: FindTweetResult
)