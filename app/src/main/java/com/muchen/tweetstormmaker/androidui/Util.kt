package com.muchen.tweetstormmaker.androidui

import com.muchen.tweetstormmaker.androidui.model.AccessTokens
import com.muchen.tweetstormmaker.androidui.model.TwitterUserAndTokens

fun TwitterUserAndTokens.toAccessTokens(): AccessTokens {
    return AccessTokens(accessToken, accessTokenSecret)
}