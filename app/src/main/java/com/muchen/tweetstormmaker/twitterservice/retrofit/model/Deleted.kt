package com.muchen.tweetstormmaker.twitterservice.retrofit.model

import com.google.gson.annotations.SerializedName

class Deleted (
    @SerializedName("deleted")
    val deleted: Boolean
)