package com.muchen.tweetstormmaker.persistence.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_and_tokens")
data class TwitterUserAndTokens(@PrimaryKey
                                @ColumnInfo(name = "user_id")
                                val userId: String,

                                @ColumnInfo(name = "name")
                                var name: String,

                                @ColumnInfo(name = "screen_name")
                                var screenName: String,

                                @ColumnInfo(name = "profile_image_url_https")
                                var profileImageURLHttps: String,

                                @ColumnInfo(name = "access_token")
                                var accessToken: String,

                                @ColumnInfo(name = "access_token_secret")
                                var accessTokenSecret: String)
