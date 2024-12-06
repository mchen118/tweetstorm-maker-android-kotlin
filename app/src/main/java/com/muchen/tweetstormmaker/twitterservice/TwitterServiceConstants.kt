package com.muchen.tweetstormmaker.twitterservice

object TwitterServiceConstants {

    const val API_BASE_URL = "https://api.x.com/"
    const val API_REQUEST_TOKEN_ENDPOINT_URL = "https://api.x.com/oauth/request_token"
    const val API_AUTHORIZE_ENDPOINT_URL = "https://api.x.com/oauth/authorize"
    const val API_ACCESS_TOKEN_ENDPOINT_URL = "https://api.x.com/oauth/access_token"
    const val API_OAUTH_PATH = "/oauth/"
    const val API_NON_OAUTH_PATH = "/1.1/"

    const val API_FIELD_STATUS = "status"
    const val API_FIELD_REPLY_TO_STATUS_ID = "in_reply_to_status_id"
    const val API_TWEET_MAX_WEIGHTED_LENGTH = 280
    const val API_SHORTENED_URL_LENGTH = 23

    const val API_DEFAULT_LOGIN_CALL_TIMEOUT_IN_MILLISECONDS = 3000L
    const val API_DEFAULT_OTHER_CALL_TIMEOUT_IN_MILLISECONDS = 3000L
}