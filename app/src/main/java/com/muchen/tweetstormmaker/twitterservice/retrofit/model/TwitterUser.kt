package com.muchen.tweetstormmaker.twitterservice.retrofit.model

import com.google.gson.annotations.SerializedName

class TwitterUser(
    @SerializedName("id_str")
    var userId: String = "",

    @SerializedName("name")
    var name: String = "",

    @SerializedName("screen_name")
    var screenName: String = "",

    @SerializedName("profile_image_url_https")
    var profileImageURLHttps: String = ""
)