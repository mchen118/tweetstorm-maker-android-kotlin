package com.muchen.tweetstormmaker.twitterservice.retrofit.model

import com.google.gson.annotations.SerializedName

class ReplyWrapper(
    @SerializedName("text")
    val text: String,
    @SerializedName("reply")
    val reply: Reply
)