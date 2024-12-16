package com.muchen.tweetstormmaker.twitterservice.retrofit.model

import com.google.gson.annotations.SerializedName

class DeletedWrapper (
    @SerializedName("data")
    val data: Deleted
)