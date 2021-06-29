package com.muchen.tweetstormmaker.interfaceadapter

import com.muchen.tweetstormmaker.interfaceadapter.model.*

fun Draft.toDraftSentStatus(): DraftSentStatus {
    return DraftSentStatus(timeCreated, sentStatus, sentIds)
}

fun Draft.toDraftContent(): DraftContent {
    return DraftContent(timeCreated, content)
}

fun TwitterUserAndTokens.toAccessTokens(): AccessTokens {
    return AccessTokens(accessToken, accessTokenSecret)
}

fun combineIntoTwitterUserAndTokens(user: TwitterUser, tokens: AccessTokens): TwitterUserAndTokens {
    return TwitterUserAndTokens(user.userId, user.name, user.screenName, user.profileImageURLHttps, tokens.accessToken, tokens.accessTokenSecret)
}

fun List<String>.toCSVString(): String = this.joinToString(",")