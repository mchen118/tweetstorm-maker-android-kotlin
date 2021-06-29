package com.muchen.tweetstormmaker.persistence.room.model

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

class TwitterUser(@PrimaryKey
                  @ColumnInfo(name = "user_id")
                  val userId: String,

                  @ColumnInfo(name = "name")
                  var name: String,

                  @ColumnInfo(name = "screen_name")
                  var screenName: String,

                  @ColumnInfo(name = "profile_image_url_https")
                  var profileImageURLHttps: String)
