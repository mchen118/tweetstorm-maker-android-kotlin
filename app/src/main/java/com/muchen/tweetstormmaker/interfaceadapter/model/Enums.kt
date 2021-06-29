package com.muchen.tweetstormmaker.interfaceadapter.model

enum class SentStatusEnum {
    LOCAL,
    PARTIALLY_SENT,
    FULLY_SENT
}

enum class UnsendTweetStormResultEnum {
    NO_TWEET_UNSENT,
    PARTIALLY_UNSENT,
    FULLY_UNSENT
}

enum class UnsendMultipleTweetstormsResultEnum {
    NO_TWEET_UNSENT,
    NO_TWEETSTORM_UNSENT,
    SOME_TWEETSTORMS_UNSENT,
    FULLY_UNSENT
}