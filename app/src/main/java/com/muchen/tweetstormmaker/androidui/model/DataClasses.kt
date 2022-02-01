package com.muchen.tweetstormmaker.androidui.model

data class Draft(val timeCreated: Long,
                 var content: String = "",
                 var sentStatus: SentStatusEnum = SentStatusEnum.LOCAL,
                 var sentIds: String = "")

data class DraftContent(val timeCreated: Long,
                        var content: String)

data class DraftSentStatus(val timeCreated: Long,
                           var sentStatus: SentStatusEnum,
                           var sentIds: String)

data class AccessTokens(val accessToken: String,
                        val accessTokenSecret: String)

data class TwitterUserAndTokens(val userId: String,
                                var name: String,
                                var screenName: String,
                                var profileImageURLHttps: String,
                                var accessToken: String,
                                var accessTokenSecret: String)