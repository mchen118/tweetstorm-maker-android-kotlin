package com.muchen.tweetstormmaker

import com.google.common.truth.Truth.assertThat
import com.muchen.tweetstormmaker.interfaceadapter.model.AccessTokens
import com.muchen.tweetstormmaker.interfaceadapter.model.TwitterUser
import com.muchen.tweetstormmaker.interfaceadapter.repository.ITwitterService
import com.muchen.tweetstormmaker.twitterservice.TwitterServiceConstants.API_NON_OAUTH_PATH
import com.muchen.tweetstormmaker.twitterservice.TwitterServiceConstants.API_OAUTH_PATH
import com.muchen.tweetstormmaker.twitterservice.TwitterServiceImpl
import com.muchen.tweetstormmaker.twitterservice.retrofit.RetrofitTwitterApiClient
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class IntegrationTestOnTwitterServiceImpl {

    // static
    private class TestRetrofitTwitterApiClient(API_KEY: String, API_SECRET: String,
                                       baseUrl: String, requestTokenEndPoint: String,
                                       accessTokenEndPoint: String, authorizeEndPoint: String,
                                       loginCallTimeout: Long, otherCallTimeout: Long)
    : RetrofitTwitterApiClient(API_KEY, API_SECRET, baseUrl, requestTokenEndPoint,
            accessTokenEndPoint, authorizeEndPoint, loginCallTimeout, otherCallTimeout) {

        /**
         * Calling [println] to decouple from Android, and to make JVM tests possible.
         */
        override fun mLog(tag: String, message: String, type: String) = println("$type/$tag: $message")
    }

    private val timeout = 100L
    private val mockServer = MockWebServer()
    private lateinit var nonOauthBaseUrl: String
    private lateinit var oAuthBaseUrl: String

    // use Lazy<> so that assignment of serviceImpl can be moved out of @Test methods and
    // into a @Before method
    private val requestTokenEndPointUrl by lazy { oAuthBaseUrl + "request_token" }
    private val accessTokenEndPointUrl by lazy { oAuthBaseUrl + "access_token" }
    private val authorizeEndPointUrl by lazy { oAuthBaseUrl + "authorize" }
    private lateinit var serviceImpl: Lazy<ITwitterService>

    @Before
    fun createMockServer() {
        mockServer.start()
        serviceImpl = lazy {
            TwitterServiceImpl.getInstance(
                TestRetrofitTwitterApiClient("", "",
                    nonOauthBaseUrl, requestTokenEndPointUrl, accessTokenEndPointUrl,
                            authorizeEndPointUrl, timeout, timeout)
            )
        }
    }

    @After
    fun tearDownMockServer() {
        mockServer.shutdown()
    }

    @Test
    fun retrieveAuthorizationUrl_success() {
        mockServer.enqueue(retrieveAuthorizationUrlResponseSuccess)
        nonOauthBaseUrl = ""
        oAuthBaseUrl = mockServer.url(API_OAUTH_PATH).toString()
        // assertThat(baseUrl).containsMatch(Pattern.compile("http://localhost:\\d+$API_OAUTH_PATH"))

        runBlocking {
            val resultUrl = serviceImpl.value.retrieveAuthorizationUrl()

            assertThat(resultUrl).isEqualTo("$authorizeEndPointUrl?$expectedAuthorizationUrlQuery")
        }
    }

    @Test
    fun retrieveAuthorizationUrl_failure() {
        mockServer.enqueue(retrieveAuthorizationUrlResponseFailure)
        nonOauthBaseUrl = ""
        oAuthBaseUrl = mockServer.url(API_OAUTH_PATH).toString()

        runBlocking {
            val resultUrl = serviceImpl.value.retrieveAuthorizationUrl()

            assertThat(resultUrl).isNull()
        }
    }

    @Test
    fun retrieveAccessTokens_success() {
        mockServer.apply {
            enqueue(retrieveAuthorizationUrlResponseSuccess)
            enqueue(retrieveAccessTokenResponseSuccess)
        }
        nonOauthBaseUrl = ""
        oAuthBaseUrl = mockServer.url(API_OAUTH_PATH).toString()

        runBlocking {
            val resultUrl = serviceImpl.value.retrieveAuthorizationUrl()
            val resultAccessTokens = serviceImpl.value.retrieveAccessTokens("random_string")

            assertThat(resultUrl).isEqualTo("$authorizeEndPointUrl?$expectedAuthorizationUrlQuery")
            assertThat(resultAccessTokens).isNotNull()
            assertThat(resultAccessTokens?.accessToken).isEqualTo(expectedAccessTokens.accessToken)
            assertThat(resultAccessTokens?.accessTokenSecret).isEqualTo(expectedAccessTokens.accessTokenSecret)
        }
    }

    @Test
    fun retrieveAccessTokens_failure() {
        mockServer.apply {
            enqueue(retrieveAuthorizationUrlResponseSuccess)
            enqueue(retrieveAccessTokensResponseFailure)
        }
        nonOauthBaseUrl = ""
        oAuthBaseUrl = mockServer.url(API_OAUTH_PATH).toString()

        runBlocking {
            val resultUrl = serviceImpl.value.retrieveAuthorizationUrl()
            val resultAccessTokens = serviceImpl.value.retrieveAccessTokens("any")

            assertThat(resultUrl).isEqualTo("$authorizeEndPointUrl?$expectedAuthorizationUrlQuery")
            assertThat(resultAccessTokens).isNull()
        }
    }

    @Test
    fun retrieveTwitterUser_success() {
        mockServer.enqueue(retrieveTwitterUserResponseSuccess)
        nonOauthBaseUrl = mockServer.url(API_NON_OAUTH_PATH).toString()
        oAuthBaseUrl = ""

        runBlocking {
            val resultTwitterUser =  serviceImpl.value.retrieveTwitterUser()

            assertThat(resultTwitterUser).isEqualTo(expectedTwitterUser)
        }
    }

    @Test
    fun retrieveTwitterUser_failure() {
        mockServer.enqueue(retrieveTwitterUserResponseFailure)
        nonOauthBaseUrl = mockServer.url(API_NON_OAUTH_PATH).toString()
        oAuthBaseUrl = ""

        runBlocking {
            val resultTwitterUser = serviceImpl.value.retrieveTwitterUser()

            assertThat(resultTwitterUser).isNull()
        }
    }

    @Test
    fun sendTweet_success() {
        mockServer.enqueue(sentTweetResponseSuccess)
        nonOauthBaseUrl = mockServer.url(API_NON_OAUTH_PATH).toString()
        oAuthBaseUrl = ""

        runBlocking {
            val resultStatusIdString = serviceImpl.value.sendTweet("x", null)

            assertThat(resultStatusIdString).isEqualTo(expectedSentTweetStatusId)
        }
    }

    @Test
    fun sendTweet_failure() {
        mockServer.enqueue(sendTweetResponseFailure)
        nonOauthBaseUrl = mockServer.url(API_NON_OAUTH_PATH).toString()
        oAuthBaseUrl = ""

        runBlocking {
            val resultStatusIdString = serviceImpl.value.sendTweet("x", null)

            assertThat(resultStatusIdString).isNull()
        }
    }

    @Test
    fun deleteTweet_success() {
        mockServer.enqueue(deleteTweetResponseSuccess)
        nonOauthBaseUrl = mockServer.url(API_NON_OAUTH_PATH).toString()
        oAuthBaseUrl = ""

        runBlocking {
            val resultBoolean = serviceImpl.value.deleteTweet(expectedDeletedTweetStatusId)

            assertThat(resultBoolean).isTrue()
        }
    }

    @Test
    fun deleteTweet_failure() {
        mockServer.enqueue(deleteTweetResponseFailure)
        nonOauthBaseUrl = mockServer.url(API_NON_OAUTH_PATH).toString()
        oAuthBaseUrl = ""

        runBlocking {
            val resultBoolean = serviceImpl.value.deleteTweet("90090")

            assertThat(resultBoolean).isFalse()
        }
    }

    @Test
    fun findTweet_success() {
        mockServer.enqueue(findTweetResponseSuccess)
        nonOauthBaseUrl = mockServer.url(API_NON_OAUTH_PATH).toString()
        oAuthBaseUrl = ""

        runBlocking {
            val resultBoolean = serviceImpl.value.findTweet(expectedFoundTweetStatusId)

            assertThat(resultBoolean).isTrue()
        }
    }

    @Test
    fun findTweet_failure_not_found() {
        mockServer.enqueue(findTweetResponseFailure)
        nonOauthBaseUrl = mockServer.url(API_NON_OAUTH_PATH).toString()
        oAuthBaseUrl = ""

        runBlocking {
            val resultBoolean = serviceImpl.value.findTweet("32156")

            assertThat(resultBoolean).isFalse()
        }
    }

    @Test
    fun findTweet_failure_IOException() {
        nonOauthBaseUrl = mockServer.url(API_NON_OAUTH_PATH).toString()
        oAuthBaseUrl = ""

        runBlocking {
            val resultBoolean = serviceImpl.value.findTweet("321313")

            assertThat(resultBoolean).isNull()
        }
    }

    companion object {
        // response code is set to 200 by default by calling the MockResponse() constructor
        private val retrieveAuthorizationUrlResponseSuccess = MockResponse().setBody("oauth_token=" +
                "Z6eEdO8MOmk394WozF5oKyuAv855l4Mlqo7hhlSLik" +
                "&oauth_token_secret=Kd75W4OQfb2oJTV0vzGzeXftVAwgMnEK9MumzYcM" +
                "&oauth_callback_confirmed=true")
        private val expectedAuthorizationUrlQuery = "oauth_token=" +
                "Z6eEdO8MOmk394WozF5oKyuAv855l4Mlqo7hhlSLik"
        private val retrieveAuthorizationUrlResponseFailure = MockResponse().setResponseCode(400)

        private val retrieveAccessTokenResponseSuccess = MockResponse().setBody("oauth_token=" +
                "6253282-eWudHldSbIaelX7swmsiHImEL4KinwaGloHANdrY" +
                "&oauth_token_secret=2EEfA6BG5ly3sR3XjE0IBSnlQu4ZrUzPiYTmrkVU" +
                "&user_id=6253282" +
                "&screen_name=twitterapi")
        private val expectedAccessTokens = AccessTokens(
            "6253282-eWudHldSbIaelX7swmsiHImEL4KinwaGloHANdrY",
            "2EEfA6BG5ly3sR3XjE0IBSnlQu4ZrUzPiYTmrkVU")
        private val retrieveAccessTokensResponseFailure = MockResponse().setResponseCode(400)

        private val retrieveTwitterUserResponseSuccess = MockResponse()
            .setBody("{\n" +
                    "    \"contributors_enabled\": true,\n" +
                    "    \"created_at\": \"Sat May 09 17:58:22 +0000 2009\",\n" +
                    "    \"default_profile\": false,\n" +
                    "    \"default_profile_image\": false,\n" +
                    "    \"description\": \"I taught your phone that thing you like.  The Mobile Partner Engineer @Twitter. \",\n" +
                    "    \"favourites_count\": 588,\n" +
                    "    \"follow_request_sent\": null,\n" +
                    "    \"followers_count\": 10625,\n" +
                    "    \"following\": null,\n" +
                    "    \"friends_count\": 1181,\n" +
                    "    \"geo_enabled\": true,\n" +
                    "    \"id\": 38895958,\n" +
                    "    \"id_str\": \"38895958\",\n" +
                    "    \"is_translator\": false,\n" +
                    "    \"lang\": \"en\",\n" +
                    "    \"listed_count\": 190,\n" +
                    "    \"location\": \"San Francisco\",\n" +
                    "    \"name\": \"Sean Cook\",\n" +
                    "    \"notifications\": null,\n" +
                    "    \"profile_background_color\": \"1A1B1F\",\n" +
                    "    \"profile_background_image_url\": \"http://a0.twimg.com/profile_background_images/495742332/purty_wood.png\",\n" +
                    "    \"profile_background_image_url_https\": \"https://si0.twimg.com/profile_background_images/495742332/purty_wood.png\",\n" +
                    "    \"profile_background_tile\": true,\n" +
                    "    \"profile_image_url\": \"http://a0.twimg.com/profile_images/1751506047/dead_sexy_normal.JPG\",\n" +
                    "    \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/1751506047/dead_sexy_normal.JPG\",\n" +
                    "    \"profile_link_color\": \"2FC2EF\",\n" +
                    "    \"profile_sidebar_border_color\": \"181A1E\",\n" +
                    "    \"profile_sidebar_fill_color\": \"252429\",\n" +
                    "    \"profile_text_color\": \"666666\",\n" +
                    "    \"profile_use_background_image\": true,\n" +
                    "    \"protected\": false,\n" +
                    "    \"screen_name\": \"theSeanCook\",\n" +
                    "    \"show_all_inline_media\": true,\n" +
                    "    \"status\": {\n" +
                    "        \"contributors\": null,\n" +
                    "        \"coordinates\": {\n" +
                    "            \"coordinates\": [\n" +
                    "                -122.45037293,\n" +
                    "                37.76484123\n" +
                    "            ],\n" +
                    "            \"type\": \"Point\"\n" +
                    "        },\n" +
                    "        \"created_at\": \"Tue Aug 28 05:44:24 +0000 2012\",\n" +
                    "        \"favorited\": false,\n" +
                    "        \"geo\": {\n" +
                    "            \"coordinates\": [\n" +
                    "                37.76484123,\n" +
                    "                -122.45037293\n" +
                    "            ],\n" +
                    "            \"type\": \"Point\"\n" +
                    "        },\n" +
                    "        \"id\": 240323931419062272,\n" +
                    "        \"id_str\": \"240323931419062272\",\n" +
                    "        \"in_reply_to_screen_name\": \"messl\",\n" +
                    "        \"in_reply_to_status_id\": 240316959173009410,\n" +
                    "        \"in_reply_to_status_id_str\": \"240316959173009410\",\n" +
                    "        \"in_reply_to_user_id\": 18707866,\n" +
                    "        \"in_reply_to_user_id_str\": \"18707866\",\n" +
                    "        \"place\": {\n" +
                    "            \"attributes\": {},\n" +
                    "            \"bounding_box\": {\n" +
                    "                \"coordinates\": [\n" +
                    "                    [\n" +
                    "                        [\n" +
                    "                            -122.45778216,\n" +
                    "                            37.75932999\n" +
                    "                        ],\n" +
                    "                        [\n" +
                    "                            -122.44248216,\n" +
                    "                            37.75932999\n" +
                    "                        ],\n" +
                    "                        [\n" +
                    "                            -122.44248216,\n" +
                    "                            37.76752899\n" +
                    "                        ],\n" +
                    "                        [\n" +
                    "                            -122.45778216,\n" +
                    "                            37.76752899\n" +
                    "                        ]\n" +
                    "                    ]\n" +
                    "                ],\n" +
                    "                \"type\": \"Polygon\"\n" +
                    "            },\n" +
                    "            \"country\": \"United States\",\n" +
                    "            \"country_code\": \"US\",\n" +
                    "            \"full_name\": \"Ashbury Heights, San Francisco\",\n" +
                    "            \"id\": \"866269c983527d5a\",\n" +
                    "            \"name\": \"Ashbury Heights\",\n" +
                    "            \"place_type\": \"neighborhood\",\n" +
                    "            \"url\": \"http://api.twitter.com/1/geo/id/866269c983527d5a.json\"\n" +
                    "        },\n" +
                    "        \"retweet_count\": 0,\n" +
                    "        \"retweeted\": false,\n" +
                    "        \"source\": \"Twitter for  iPhone\",\n" +
                    "        \"text\": \"@messl congrats! So happy for all 3 of you.\",\n" +
                    "        \"truncated\": false\n" +
                    "    },\n" +
                    "    \"statuses_count\": 2609,\n" +
                    "    \"time_zone\": \"Pacific Time (US & Canada)\",\n" +
                    "    \"url\": null,\n" +
                    "    \"utc_offset\": -28800,\n" +
                    "    \"verified\": false\n" +
                    "}\n")
        private val expectedTwitterUser = TwitterUser("38895958", "Sean Cook", "theSeanCook",
            "https://si0.twimg.com/profile_images/1751506047/dead_sexy_normal.JPG")
        private val retrieveTwitterUserResponseFailure = MockResponse().setResponseCode(400)
            .setBody("{\"errors\":[{\"code\":215,\"message\":\"Bad Authentication data.\"}]}")

        private val findTweetResponseSuccess = MockResponse()
            .setBody("{\n" +
                    "  \"created_at\": \"Wed Oct 10 20:19:24 +0000 2018\",\n" +
                    "  \"id\": 1050118621198921728,\n" +
                    "  \"id_str\": \"1050118621198921728\",\n" +
                    "  \"text\": \"To make room for more expression, we will now count all emojis as equal—including those with gender\u200D\u200D\u200D and skin t… https://t.co/MkGjXf9aXm\",\n" +
                    "  \"truncated\": true,\n" +
                    "  \"entities\": {\n" +
                    "    \"hashtags\": [],\n" +
                    "    \"symbols\": [],\n" +
                    "    \"user_mentions\": [],\n" +
                    "    \"urls\": [\n" +
                    "      {\n" +
                    "        \"url\": \"https://t.co/MkGjXf9aXm\",\n" +
                    "        \"expanded_url\": \"https://twitter.com/i/web/status/1050118621198921728\",\n" +
                    "        \"display_url\": \"twitter.com/i/web/status/1…\",\n" +
                    "        \"indices\": [\n" +
                    "          117,\n" +
                    "          140\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"source\": \"Twitter Web Client\",\n" +
                    "  \"in_reply_to_status_id\": null,\n" +
                    "  \"in_reply_to_status_id_str\": null,\n" +
                    "  \"in_reply_to_user_id\": null,\n" +
                    "  \"in_reply_to_user_id_str\": null,\n" +
                    "  \"in_reply_to_screen_name\": null,\n" +
                    "  \"user\": {\n" +
                    "    \"id\": 6253282,\n" +
                    "    \"id_str\": \"6253282\",\n" +
                    "    \"name\": \"Twitter API\",\n" +
                    "    \"screen_name\": \"TwitterAPI\",\n" +
                    "    \"location\": \"San Francisco, CA\",\n" +
                    "    \"description\": \"The Real Twitter API. Tweets about API changes, service issues and our Developer Platform. Don't get an answer? It's on my website.\",\n" +
                    "    \"url\": \"https://t.co/8IkCzCDr19\",\n" +
                    "    \"entities\": {\n" +
                    "      \"url\": {\n" +
                    "        \"urls\": [\n" +
                    "          {\n" +
                    "            \"url\": \"https://t.co/8IkCzCDr19\",\n" +
                    "            \"expanded_url\": \"https://developer.twitter.com\",\n" +
                    "            \"display_url\": \"developer.twitter.com\",\n" +
                    "            \"indices\": [\n" +
                    "              0,\n" +
                    "              23\n" +
                    "            ]\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      },\n" +
                    "      \"description\": {\n" +
                    "        \"urls\": []\n" +
                    "      }\n" +
                    "    },\n" +
                    "    \"protected\": false,\n" +
                    "    \"followers_count\": 6128663,\n" +
                    "    \"friends_count\": 12,\n" +
                    "    \"listed_count\": 12900,\n" +
                    "    \"created_at\": \"Wed May 23 06:01:13 +0000 2007\",\n" +
                    "    \"favourites_count\": 32,\n" +
                    "    \"utc_offset\": null,\n" +
                    "    \"time_zone\": null,\n" +
                    "    \"geo_enabled\": null,\n" +
                    "    \"verified\": true,\n" +
                    "    \"statuses_count\": 3659,\n" +
                    "    \"lang\": \"null\",\n" +
                    "    \"contributors_enabled\": null,\n" +
                    "    \"is_translator\": null,\n" +
                    "    \"is_translation_enabled\": null,\n" +
                    "    \"profile_background_color\": \"null\",\n" +
                    "    \"profile_background_image_url\": \"null\",\n" +
                    "    \"profile_background_image_url_https\": \"null\",\n" +
                    "    \"profile_background_tile\": null,\n" +
                    "    \"profile_image_url\": \"null\",\n" +
                    "    \"profile_image_url_https\": \"https://pbs.twimg.com/profile_images/942858479592554497/BbazLO9L_normal.jpg\",\n" +
                    "    \"profile_banner_url\": \"https://pbs.twimg.com/profile_banners/6253282/1497491515\",\n" +
                    "    \"profile_link_color\": \"null\",\n" +
                    "    \"profile_sidebar_border_color\": \"null\",\n" +
                    "    \"profile_sidebar_fill_color\": \"null\",\n" +
                    "    \"profile_text_color\": \"null\",\n" +
                    "    \"profile_use_background_image\": null,\n" +
                    "    \"has_extended_profile\": null,\n" +
                    "    \"default_profile\": false,\n" +
                    "    \"default_profile_image\": false,\n" +
                    "    \"following\": null,\n" +
                    "    \"follow_request_sent\": null,\n" +
                    "    \"notifications\": null,\n" +
                    "    \"translator_type\": \"null\"\n" +
                    "  },\n" +
                    "  \"geo\": null,\n" +
                    "  \"coordinates\": null,\n" +
                    "  \"place\": null,\n" +
                    "  \"contributors\": null,\n" +
                    "  \"is_quote_status\": false,\n" +
                    "  \"retweet_count\": 161,\n" +
                    "  \"favorite_count\": 296,\n" +
                    "  \"favorited\": false,\n" +
                    "  \"retweeted\": false,\n" +
                    "  \"possibly_sensitive\": false,\n" +
                    "  \"possibly_sensitive_appealable\": false,\n" +
                    "  \"lang\": \"en\"\n" +
                    "}")
        private val expectedFoundTweetStatusId = "1050118621198921728"
        private val findTweetResponseFailure = MockResponse().setResponseCode(404)

        private val sentTweetResponseSuccess = MockResponse()
            .setBody("{\n" +
                    "  \"created_at\": \"Wed Oct 10 20:19:24 +0000 2018\",\n" +
                    "  \"id\": 1050118621198921700,\n" +
                    "  \"id_str\": \"1050118621198921728\",\n" +
                    "  \"text\": \"To make room for more expression, we will now count all emojis as equal—including those with gender\u200D\u200D\u200D \u200D\u200Dand skin t… https://t.co/MkGjXf9aXm\",\n" +
                    "  \"source\": \"Twitter Web Client\",\n" +
                    "  \"truncated\": true,\n" +
                    "  \"in_reply_to_status_id\": null,\n" +
                    "  \"in_reply_to_status_id_str\": null,\n" +
                    "  \"in_reply_to_user_id\": null,\n" +
                    "  \"in_reply_to_user_id_str\": null,\n" +
                    "  \"in_reply_to_screen_name\": null,\n" +
                    "  \"user\": {\n" +
                    "    \"id\": 6253282,\n" +
                    "    \"id_str\": \"6253282\",\n" +
                    "    \"name\": \"Twitter API\",\n" +
                    "    \"screen_name\": \"TwitterAPI\",\n" +
                    "    \"location\": \"San Francisco, CA\",\n" +
                    "    \"url\": \"https://developer.twitter.com\",\n" +
                    "    \"description\": \"The Real Twitter API. Tweets about API changes, service issues and our Developer Platform. Don't get an answer? It's on my website.\",\n" +
                    "    \"translator_type\": \"null\",\n" +
                    "    \"derived\": {\n" +
                    "      \"locations\": [\n" +
                    "        {\n" +
                    "          \"country\": \"United States\",\n" +
                    "          \"country_code\": \"US\",\n" +
                    "          \"locality\": \"San Francisco\",\n" +
                    "          \"region\": \"California\",\n" +
                    "          \"sub_region\": \"San Francisco County\",\n" +
                    "          \"full_name\": \"San Francisco, California, United States\",\n" +
                    "          \"geo\": {\n" +
                    "            \"coordinates\": [\n" +
                    "              -122.41942,\n" +
                    "              37.77493\n" +
                    "            ],\n" +
                    "            \"type\": \"point\"\n" +
                    "          }\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    \"protected\": false,\n" +
                    "    \"verified\": true,\n" +
                    "    \"followers_count\": 6172196,\n" +
                    "    \"friends_count\": 12,\n" +
                    "    \"listed_count\": 13003,\n" +
                    "    \"favourites_count\": 31,\n" +
                    "    \"statuses_count\": 3650,\n" +
                    "    \"created_at\": \"Wed May 23 06:01:13 +0000 2007\",\n" +
                    "    \"utc_offset\": null,\n" +
                    "    \"time_zone\": null,\n" +
                    "    \"geo_enabled\": false,\n" +
                    "    \"lang\": \"en\",\n" +
                    "    \"contributors_enabled\": false,\n" +
                    "    \"is_translator\": null,\n" +
                    "    \"profile_background_color\": \"null\",\n" +
                    "    \"profile_background_image_url\": \"null\",\n" +
                    "    \"profile_background_image_url_https\": \"null\",\n" +
                    "    \"profile_background_tile\": null,\n" +
                    "    \"profile_link_color\": \"null\",\n" +
                    "    \"profile_sidebar_border_color\": \"null\",\n" +
                    "    \"profile_sidebar_fill_color\": \"null\",\n" +
                    "    \"profile_text_color\": \"null\",\n" +
                    "    \"profile_use_background_image\": null,\n" +
                    "    \"profile_image_url\": \"null\",\n" +
                    "    \"profile_image_url_https\": \"https://pbs.twimg.com/profile_images/942858479592554497/BbazLO9L_normal.jpg\",\n" +
                    "    \"profile_banner_url\": \"https://pbs.twimg.com/profile_banners/6253282/1497491515\",\n" +
                    "    \"default_profile\": false,\n" +
                    "    \"default_profile_image\": false,\n" +
                    "    \"following\": null,\n" +
                    "    \"follow_request_sent\": null,\n" +
                    "    \"notifications\": null\n" +
                    "  },\n" +
                    "  \"geo\": null,\n" +
                    "  \"coordinates\": null,\n" +
                    "  \"place\": null,\n" +
                    "  \"contributors\": null,\n" +
                    "  \"is_quote_status\": false,\n" +
                    "  \"extended_tweet\": {\n" +
                    "    \"full_text\": \"To make room for more expression, we will now count all emojis as equal—including those with gender\u200D\u200D\u200D \u200D\u200Dand skin tone modifiers \uD83D\uDC4D\uD83C\uDFFB\uD83D\uDC4D\uD83C\uDFFD\uD83D\uDC4D\uD83C\uDFFF. This is now reflected in Twitter-Text, our Open Source library. nnUsing Twitter-Text? See the forum post for detail: https://t.co/Nx1XZmRCXA\",\n" +
                    "    \"display_text_range\": [\n" +
                    "      0,\n" +
                    "      277\n" +
                    "    ],\n" +
                    "    \"entities\": {\n" +
                    "      \"hashtags\": [],\n" +
                    "      \"urls\": [\n" +
                    "        {\n" +
                    "          \"url\": \"https://t.co/Nx1XZmRCXA\",\n" +
                    "          \"expanded_url\": \"https://twittercommunity.com/t/new-update-to-the-twitter-text-library-emoji-character-count/114607\",\n" +
                    "          \"display_url\": \"twittercommunity.com/t/new-update-t…\",\n" +
                    "          \"unwound\": {\n" +
                    "            \"url\": \"https://twittercommunity.com/t/new-update-to-the-twitter-text-library-emoji-character-count/114607\",\n" +
                    "            \"status\": 200,\n" +
                    "            \"title\": \"New update to the Twitter-Text library: Emoji character count\",\n" +
                    "            \"description\": \"Over the years, we have made several updates to the way that people can communicate on Twitter. One of the more notable changes made last year was to increase the number of characters per Tweet from 140 to 280 characters. Today, we continue to expand people’s ability to express themselves by announcing a change to the way that we count emojis. Due to the differences in the way written text and emojis are encoded, many emojis (including emojis where you can apply gender and skin tone) have count...\"\n" +
                    "          },\n" +
                    "          \"indices\": [\n" +
                    "            254,\n" +
                    "            277\n" +
                    "          ]\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"user_mentions\": [],\n" +
                    "      \"symbols\": []\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"quote_count\": 0,\n" +
                    "  \"reply_count\": 0,\n" +
                    "  \"retweet_count\": 0,\n" +
                    "  \"favorite_count\": 0,\n" +
                    "  \"entities\": {\n" +
                    "    \"hashtags\": [],\n" +
                    "    \"urls\": [\n" +
                    "      {\n" +
                    "        \"url\": \"https://t.co/MkGjXf9aXm\",\n" +
                    "        \"expanded_url\": \"https://twitter.com/i/web/status/1050118621198921728\",\n" +
                    "        \"display_url\": \"twitter.com/i/web/status/1…\",\n" +
                    "        \"indices\": [\n" +
                    "          117,\n" +
                    "          140\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"user_mentions\": [],\n" +
                    "    \"symbols\": []\n" +
                    "  },\n" +
                    "  \"favorited\": false,\n" +
                    "  \"retweeted\": false,\n" +
                    "  \"possibly_sensitive\": false,\n" +
                    "  \"filter_level\": \"low\",\n" +
                    "  \"lang\": \"en\"\n" +
                    "}\n")
        private val expectedSentTweetStatusId = "1050118621198921728"
        private val sendTweetResponseFailure = MockResponse().setResponseCode(400)

        private val deleteTweetResponseSuccess = MockResponse()
            .setBody("{\n" +
                    "  \"coordinates\": null,\n" +
                    "  \"favorited\": false,\n" +
                    "  \"created_at\": \"Wed Aug 29 16:54:38 +0000 2012\",\n" +
                    "  \"truncated\": false,\n" +
                    "  \"id_str\": \"240854986559455234\",\n" +
                    "  \"entities\": {\n" +
                    "    \"urls\": [\n" +
                    "      {\n" +
                    "        \"expanded_url\": \"http://venturebeat.com/2012/08/29/vimeo-dropbox/#.UD5JLsYptSs.twitter\",\n" +
                    "        \"url\": \"http://t.co/7UlkvZzM\",\n" +
                    "        \"indices\": [\n" +
                    "          69,\n" +
                    "          89\n" +
                    "        ],\n" +
                    "        \"display_url\": \"venturebeat.com/2012/08/29/vim…\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"hashtags\": [\n" +
                    "\n" +
                    "    ],\n" +
                    "    \"user_mentions\": [\n" +
                    "\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"in_reply_to_user_id_str\": null,\n" +
                    "  \"text\": \"Hello World\",\n" +
                    "  \"contributors\": null,\n" +
                    "  \"retweet_count\": 1,\n" +
                    "  \"id\": 240854986559455234,\n" +
                    "  \"in_reply_to_status_id_str\": null,\n" +
                    "  \"geo\": null,\n" +
                    "  \"retweeted\": false,\n" +
                    "  \"in_reply_to_user_id\": null,\n" +
                    "  \"possibly_sensitive\": false,\n" +
                    "  \"place\": null,\n" +
                    "  \"user\": {\n" +
                    "    \"name\": \"Jason Costa\",\n" +
                    "    \"profile_sidebar_border_color\": \"86A4A6\",\n" +
                    "    \"profile_sidebar_fill_color\": \"A0C5C7\",\n" +
                    "    \"profile_background_tile\": false,\n" +
                    "    \"profile_image_url\": \"http://a0.twimg.com/profile_images/1751674923/new_york_beard_normal.jpg\",\n" +
                    "    \"created_at\": \"Wed May 28 00:20:15 +0000 2008\",\n" +
                    "    \"location\": \"\",\n" +
                    "    \"is_translator\": true,\n" +
                    "    \"follow_request_sent\": false,\n" +
                    "    \"id_str\": \"14927800\",\n" +
                    "    \"profile_link_color\": \"FF3300\",\n" +
                    "    \"entities\": {\n" +
                    "      \"url\": {\n" +
                    "        \"urls\": [\n" +
                    "          {\n" +
                    "            \"expanded_url\": \"http://www.jason-costa.blogspot.com/\",\n" +
                    "            \"url\": \"http://t.co/YCA3ZKY\",\n" +
                    "            \"indices\": [\n" +
                    "              0,\n" +
                    "              19\n" +
                    "            ],\n" +
                    "            \"display_url\": \"jason-costa.blogspot.com\"\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      },\n" +
                    "      \"description\": {\n" +
                    "        \"urls\": [\n" +
                    "\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    },\n" +
                    "    \"default_profile\": false,\n" +
                    "    \"contributors_enabled\": false,\n" +
                    "    \"url\": \"http://t.co/YCA3ZKY\",\n" +
                    "    \"favourites_count\": 883,\n" +
                    "    \"utc_offset\": -28800,\n" +
                    "    \"id\": 14927800,\n" +
                    "    \"profile_image_url_https\": \"https://si0.twimg.com/profile_images/1751674923/new_york_beard_normal.jpg\",\n" +
                    "    \"profile_use_background_image\": true,\n" +
                    "    \"listed_count\": 150,\n" +
                    "    \"profile_text_color\": \"333333\",\n" +
                    "    \"protected\": false,\n" +
                    "    \"lang\": \"en\",\n" +
                    "    \"followers_count\": 8760,\n" +
                    "    \"time_zone\": \"Pacific Time (US & Canada)\",\n" +
                    "    \"profile_background_image_url_https\": \"https://si0.twimg.com/images/themes/theme6/bg.gif\",\n" +
                    "    \"verified\": false,\n" +
                    "    \"profile_background_color\": \"709397\",\n" +
                    "    \"notifications\": false,\n" +
                    "    \"description\": \"Platform at Twitter\",\n" +
                    "    \"geo_enabled\": true,\n" +
                    "    \"statuses_count\": 5531,\n" +
                    "    \"default_profile_image\": false,\n" +
                    "    \"friends_count\": 166,\n" +
                    "    \"profile_background_image_url\": \"http://a0.twimg.com/images/themes/theme6/bg.gif\",\n" +
                    "    \"show_all_inline_media\": true,\n" +
                    "    \"screen_name\": \"jasoncosta\",\n" +
                    "    \"following\": false\n" +
                    "  },\n" +
                    "  \"possibly_sensitive_editable\": true,\n" +
                    "  \"source\": \"Tweet Button\",\n" +
                    "  \"in_reply_to_screen_name\": null,\n" +
                    "  \"in_reply_to_status_id\": null\n" +
                    "}")
        private val expectedDeletedTweetStatusId = "240854986559455234"
        private val deleteTweetResponseFailure = MockResponse().setResponseCode(400)
    }
}