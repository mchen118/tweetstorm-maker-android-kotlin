package com.muchen.tweetstormmaker.twitterservice.retrofit.model

import com.google.gson.annotations.SerializedName

class TwitterApiErrorResponseBody (
    @SerializedName("errors")
    val errors: List<TwitterApiError> = listOf())