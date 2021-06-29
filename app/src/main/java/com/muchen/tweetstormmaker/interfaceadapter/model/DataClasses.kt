package com.muchen.tweetstormmaker.interfaceadapter.model

import com.muchen.tweetstormmaker.interfaceadapter.InterfaceAdapterConstants.DEFAULT_TWEET_NUMBER_PREFIX
import com.muchen.tweetstormmaker.interfaceadapter.InterfaceAdapterConstants.DEFAULT_TWITTER_HANDLE_POSTFIX

data class SendTweetstormUseCaseInput(val draftContent: DraftContent,
                                      val twitterHandle: String,
                                      val twitterHandlePostfix: String = DEFAULT_TWITTER_HANDLE_POSTFIX,
                                      val tweetNumberPrefix: String = DEFAULT_TWEET_NUMBER_PREFIX,
                                      val numberingTweets: Boolean)

data class UnsendTweetstormUseCaseOutput(val updatedDraftSentStatus: DraftSentStatus,
                                         val resultEnum: UnsendTweetStormResultEnum)

data class UnsendMultipleTweetstormsUseCaseOutput(val updatedDraftSentStatusList: List<DraftSentStatus>,
                                                  val resultEnum: UnsendMultipleTweetstormsResultEnum)

data class Draft (val timeCreated : Long,
                  var content: String = "",
                  var sentStatus: SentStatusEnum = SentStatusEnum.LOCAL,
                  var sentIds: String = "")

data class DraftContent(val timeCreated : Long,
                        var content: String)

data class DraftSentStatus (val timeCreated : Long,
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

data class TwitterUser(val userId: String,
                       var name: String,
                       var screenName: String,
                       var profileImageURLHttps: String)