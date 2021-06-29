package com.muchen.tweetstormmaker.twitterservice.mapper

private typealias IAAccessTokens = com.muchen.tweetstormmaker.interfaceadapter.model.AccessTokens
private typealias IATwitterUser = com.muchen.tweetstormmaker.interfaceadapter.model.TwitterUser

private typealias TSAccessTokens = com.muchen.tweetstormmaker.twitterservice.model.AccessTokens
private typealias TSTwitterUSer = com.muchen.tweetstormmaker.twitterservice.retrofit.model.TwitterUser

fun IAAccessTokens.toTSModel(): TSAccessTokens {
    return TSAccessTokens(accessToken, accessTokenSecret)
}

fun TSAccessTokens.toIAModel(): IAAccessTokens {
    return IAAccessTokens(accessToken, accessTokenSecret)
}

fun TSTwitterUSer.toIAModel() : IATwitterUser {
    return IATwitterUser(userId, name, screenName, profileImageURLHttps)
}