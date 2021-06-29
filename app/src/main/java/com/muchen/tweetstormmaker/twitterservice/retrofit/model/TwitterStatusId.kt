package com.muchen.tweetstormmaker.twitterservice.retrofit.model

import com.google.gson.annotations.SerializedName

class TwitterStatusId(
    @SerializedName("id_str")
    var statusId: String = "")