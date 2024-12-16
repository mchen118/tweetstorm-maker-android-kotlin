package com.muchen.tweetstormmaker.twitterservice.retrofit.model

import com.google.gson.annotations.SerializedName

class Reply (
    @SerializedName("in_reply_to_tweet_id")
    val inReplyToTweetId: String
)