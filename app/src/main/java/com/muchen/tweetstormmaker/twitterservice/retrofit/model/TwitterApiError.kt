package com.muchen.tweetstormmaker.twitterservice.retrofit.model

import com.google.gson.annotations.SerializedName

class TwitterApiError (
    @SerializedName("code")
    val code: String = "",

    @SerializedName("message")
    val message: String = "")