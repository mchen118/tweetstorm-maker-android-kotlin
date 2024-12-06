package com.muchen.tweetstormmaker

import com.google.common.truth.Truth.assertThat
import com.muchen.tweetstormmaker.interfaceadapter.InterfaceAdapterConstants.DEFAULT_TWEET_NUMBER_PREFIX
import com.muchen.tweetstormmaker.interfaceadapter.InterfaceAdapterConstants.DEFAULT_TWITTER_HANDLE_POSTFIX
import com.muchen.tweetstormmaker.interfaceadapter.TextToTweetsProcessor
import com.muchen.tweetstormmaker.twitterservice.TwitterServiceConstants.API_SHORTENED_URL_LENGTH
import com.muchen.tweetstormmaker.twitterservice.TwitterServiceConstants.API_TWEET_MAX_WEIGHTED_LENGTH
import kotlinx.coroutines.runBlocking
import org.junit.Test

/** Copied from Twitter Text: The following unicode code point ranges
 * count have weight of 100, and the rest have a weight of 200.
 * 0x0000 (0)    - 0x10FF (4351) Basic Latin to Georgian block
 * 0x2000 (8192) - 0x200D (8205) Spaces in the General Punctuation Block
 * 0x2010 (8208) - 0x201F (8223) Hyphens &amp; Quotes in the General Punctuation Block
 * 0x2032 (8242) - 0x2037 (8247) Quotes in the General Punctuation Block
 * supports counting emoji as one weighted character
 */

class UnitTestOnTextToTweetsProcessor {
    // handle + handlePostFix has a length of 15
    private val handle = "@twitter_hmmmm"
    private val handlePostFix = DEFAULT_TWITTER_HANDLE_POSTFIX
    private val numberPrefix = DEFAULT_TWEET_NUMBER_PREFIX
    private val tweetMaxWeightedLength = API_TWEET_MAX_WEIGHTED_LENGTH
    private val shortenedUrlLength = API_SHORTENED_URL_LENGTH

    @Test
    fun process_blank_text() {
        val blackText = " \t"
        val processor = TextToTweetsProcessor(blackText, handle, handlePostFix,
                    numberPrefix, true, tweetMaxWeightedLength, shortenedUrlLength)

        assertThat(processor.hasNextTweet()).isFalse()
    }

    @Test
    fun process_short_text() {
        val text1 = "hello"
        val processor1 = TextToTweetsProcessor(text1, handle, handlePostFix,
                numberPrefix, true, tweetMaxWeightedLength, shortenedUrlLength)
        runBlocking {
            assertThat(processor1.nextTweet()).isEqualTo(text1 + numberPrefix + "1")
        }
        val processor2 = TextToTweetsProcessor(text1, handle, handlePostFix,
                numberPrefix, false, tweetMaxWeightedLength, shortenedUrlLength)
        runBlocking {
            assertThat(processor2.nextTweet()).isEqualTo(text1)
        }
    }

    @Test
    fun process_long_text() {
        val iliadOpening = "Sing, O goddess, the anger of Achilles son of Peleus, " +
                "that brought countless ills upon the Achaeans. Many a brave soul did it send " +
                "hurrying down to Hades, and many a hero did it yield a prey to dogs and vultures, " +
                "for so were the counsels of Jove fulfilled from the day on #which5842 the son of Atreus, " +
                "king of men, and great Achilles, first fell out with one another."

        val expectedFirstTweet1 = "Sing, O goddess, the anger of Achilles son of Peleus, " +
                "that brought countless ills upon the Achaeans. Many a brave soul did it send " +
                "hurrying down to Hades, and many a hero did it yield a prey to dogs and vultures, " +
                "for so were the counsels of Jove fulfilled from the day on " + numberPrefix + "1"

        val expectedSecondTweet1 = handle + handlePostFix + "#which5842 the son of Atreus, king of men, and " +
                "great Achilles, first fell out with one another." + numberPrefix + "2"

        val processor1 = TextToTweetsProcessor(iliadOpening, handle, handlePostFix,
                numberPrefix, true, tweetMaxWeightedLength, shortenedUrlLength)
        runBlocking {
            assertThat(processor1.nextTweet()).isEqualTo(expectedFirstTweet1)
            assertThat(processor1.nextTweet()).isEqualTo(expectedSecondTweet1)
        }
        val expectedFirstTweet2 = "Sing, O goddess, the anger of Achilles son of Peleus, " +
                "that brought countless ills upon the Achaeans. Many a brave soul did it send " +
                "hurrying down to Hades, and many a hero did it yield a prey to dogs and vultures, " +
                "for so were the counsels of Jove fulfilled from the day on "

        val expectedSecondTweet2 = handle + handlePostFix + "#which5842 the son of Atreus, king of men, and " +
                "great Achilles, first fell out with one another."

        val processor2 = TextToTweetsProcessor(iliadOpening, handle, handlePostFix,
                numberPrefix, false, tweetMaxWeightedLength, shortenedUrlLength)
        runBlocking {
            assertThat(processor2.nextTweet()).isEqualTo(expectedFirstTweet2)
            assertThat(processor2.nextTweet()).isEqualTo(expectedSecondTweet2)
        }
    }

    @Test
    fun process_chinese_characters() {
        // "𠁆" included test the algorithm's ability to handle a supplementary character
        val dongNiaoEntries = "-环颈鸻，是鸻形目鸻科鸻属的鸟类。台湾称为东方环颈鸻。因为其后颈基部白色，" +
                "并向颈侧延伸，与前颈白色相连形成白色领圈，故而得名。该物种的模式产地在埃及。一般性常见鸟。\n" +
                "戴胜，是犀鸟目戴胜科戴胜属的鸟类。又名胡哱哱、花蒲扇、山和尚、鸡冠鸟、臭姑鸪、咕咕翅。" +
                "头顶花冠似折扇，嘴极为细长\uD840\uDC46向下弯曲，常被误认为是啄木鸟，实际上啄木鸟嘴粗短。" +
                "戴胜在地面找到虫子后，猛地甩头将虫子抛起，并张开嘴吞入。广布于欧、亚、非三洲。" +
                "中国新疆维吾尔自治区西部、东北地区，台湾、海南等省，西藏自治区都有分布。在长江以北为夏候鸟和旅鸟，" +
                "在长江以南为留鸟。戴胜是以色列的国鸟，是中国三有保护鸟类。"

        // expectedFirstTweet1 has 142 chars, 138 of which are Chinese
        val expectedFirstTweet1 = "-环颈鸻，是鸻形目鸻科鸻属的鸟类。台湾称为东方环颈鸻。因为其后颈基部白色，" +
                "并向颈侧延伸，与前颈白色相连形成白色领圈，故而得名。该物种的模式产地在埃及。一般性常见鸟。\n" +
                "戴胜，是犀鸟目戴胜科戴胜属的鸟类。又名胡哱哱、花蒲扇、山和尚、鸡冠鸟、臭姑鸪、咕咕翅。" +
                "头顶花冠似折扇，" + numberPrefix + "1"

        // expectedSecondTweet1 has 140 chars, 122 of which are Chinese
        val expectedSecondTweet1 = handle + handlePostFix + "嘴极为细长\uD840\uDC46向下弯曲，常被误认为是啄木鸟，实际上啄木鸟嘴粗短。" +
                "戴胜在地面找到虫子后，猛地甩头将虫子抛起，并张开嘴吞入。广布于欧、亚、非三洲。" +
                "中国新疆维吾尔自治区西部、东北地区，台湾、海南等省，西藏自治区都有分布。在长江以北为夏候鸟和旅鸟，" +
                "在长江以南为留鸟。" + numberPrefix + "2"

        // expectedThirdTweet1 has 39 characters, 21 of which are Chinese
        val expectedThirdTweet1 = handle + handlePostFix + "戴胜是以色列的国鸟，是中国三有保护鸟类。" +
                numberPrefix + "3"

        val processor1 = TextToTweetsProcessor(dongNiaoEntries, handle, handlePostFix,
                numberPrefix, true, tweetMaxWeightedLength, shortenedUrlLength)
        runBlocking {
            assertThat(processor1.nextTweet()).isEqualTo(expectedFirstTweet1)
            assertThat(processor1.nextTweet()).isEqualTo(expectedSecondTweet1)
            assertThat(processor1.nextTweet()).isEqualTo(expectedThirdTweet1)
        }
        val expectedFirstTweet2 = "-环颈鸻，是鸻形目鸻科鸻属的鸟类。台湾称为东方环颈鸻。因为其后颈基部白色，" +
                "并向颈侧延伸，与前颈白色相连形成白色领圈，故而得名。该物种的模式产地在埃及。一般性常见鸟。\n" +
                "戴胜，是犀鸟目戴胜科戴胜属的鸟类。又名胡哱哱、花蒲扇、山和尚、鸡冠鸟、臭姑鸪、咕咕翅。" +
                "头顶花冠似折扇，"

        val expectedSecondTweet2 = handle + handlePostFix + "嘴极为细长\uD840\uDC46向下弯曲，常被误认为是啄木鸟，实际上啄木鸟嘴粗短。" +
                "戴胜在地面找到虫子后，猛地甩头将虫子抛起，并张开嘴吞入。广布于欧、亚、非三洲。" +
                "中国新疆维吾尔自治区西部、东北地区，台湾、海南等省，西藏自治区都有分布。在长江以北为夏候鸟和旅鸟，" +
                "在长江以南为留鸟。"

        val expectedThirdTweet2 = handle + handlePostFix + "戴胜是以色列的国鸟，是中国三有保护鸟类。"

        val processor2 = TextToTweetsProcessor(dongNiaoEntries, handle, handlePostFix,
                numberPrefix, false, tweetMaxWeightedLength, shortenedUrlLength)
        runBlocking {
            assertThat(processor2.nextTweet()).isEqualTo(expectedFirstTweet2)
            assertThat(processor2.nextTweet()).isEqualTo(expectedSecondTweet2)
            assertThat(processor2.nextTweet()).isEqualTo(expectedThirdTweet2)
        }
    }

    @Test
    fun process_long_url() {
        val stringWithLongUrl = "Hello World. https://www.google.com.hk/search?q=The+wrath+sing%2C+goddess%2C" +
                "&newwindow=1" +
                "&hl=zh-CN&source=hp&ei=OCL6YN2ZM8GCoAS26oqAAg" +
                "&iflsig=AINFCbYAAAAAYPowSGciEjtteuWqRn6rc8W32fQ3DWRG" +
                "&oq=The+wrath+sing%2C+goddess%2C" +
                "&gs_lcp=Cgdnd3Mtd2l6EANQgANYgANgohJoAXAAeAGAAY8CiAGIBJIBBTAuMi4xmAEAoAECoAEBqgEHZ3dzLXdperABAA" +
                "&sclient=gws-wiz" +
                "&ved=0ahUKEwjdlb_li_jxAhVBAYgKHTa1AiAQ4dUDCAc" +
                "&uact=5"

        val expectedFirstTweet = stringWithLongUrl

        val processor = TextToTweetsProcessor(stringWithLongUrl, handle, handlePostFix,
                numberPrefix, false, tweetMaxWeightedLength, shortenedUrlLength)
        runBlocking {
            assertThat(processor.nextTweet()).isEqualTo(expectedFirstTweet)
        }
    }

    @Test
    fun process_entities_separated_by_a_non_latin_word() {
        val longPadding = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaa"

        val stringWithTwoEntities1 = "$longPadding @jack环 颈 鸻google.com #hello world \$CUIQ"

        val expectedFirstTweet1 = "$longPadding @jack环 颈"

        val expectedSecondTweet1 = handle + handlePostFix + " 鸻google.com #hello world \$CUIQ"

        val processor1 = TextToTweetsProcessor(stringWithTwoEntities1, handle, handlePostFix,
                numberPrefix, false, tweetMaxWeightedLength, shortenedUrlLength)
        runBlocking {
            assertThat(processor1.nextTweet()).isEqualTo(expectedFirstTweet1)
            assertThat(processor1.nextTweet()).isEqualTo(expectedSecondTweet1)
        }
        val stringWithTwoEntities2 = "$longPadding @jack环颈鸻google.com #hello world \$CUIQ"

        val expectedFirstTweet2 = "$longPadding @jack"

        val expectedSecondTweet2 = handle + handlePostFix + "环颈鸻google.com #hello world \$CUIQ"

        val processor2 = TextToTweetsProcessor(stringWithTwoEntities2, handle, handlePostFix,
                numberPrefix, false, tweetMaxWeightedLength, shortenedUrlLength)
        runBlocking {
            assertThat(processor2.nextTweet()).isEqualTo(expectedFirstTweet2)
            assertThat(processor2.nextTweet()).isEqualTo(expectedSecondTweet2)
        }
    }

    @Test
    fun process_long_word() {
        val longWord = "Loooooooooooooooooooooooooooooooooooooo" +
                "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                "ooooooooooooooooooooooooooooooooooooooooooooooooooooog"

        val expectedFirstTweet = "Loooooooooooooooooooooooooooooooooooooo" +
                "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                "oooooooooooooooooooooooooooooooooo"

        val expectedSecondTweet = handle + handlePostFix + "ooooooooooooooooooog"

        val processor = TextToTweetsProcessor(longWord, handle, handlePostFix,
                    numberPrefix, false, tweetMaxWeightedLength, shortenedUrlLength)

        assertThat(longWord.length).isEqualTo(300)
        runBlocking {
            assertThat(processor.nextTweet()).isEqualTo(expectedFirstTweet)
            assertThat(processor.nextTweet()).isEqualTo(expectedSecondTweet)
        }
    }

    @Test
    fun process_chinese_character_blob() {
        val longChineseBlob = "环颈鸻是鸻形目鸻科鸻属的鸟类台湾称为东方环颈鸻因为其后颈基部白色" +
                "环颈鸻是鸻形目鸻科鸻属的鸟类台湾称为东方环颈鸻因为其后颈基部白色" +
                "环颈鸻是鸻形目鸻科鸻属的鸟类台湾称为东方环颈鸻因为其后颈基部白色" +
                "环颈鸻是鸻形目鸻科鸻属的鸟类台湾称为东方环颈鸻因为其后颈基部白色" +
                "环颈鸻是鸻形目鸻科鸻属的鸟类台湾称为东方环颈鸻因为其后颈基部白色"

        val longChineseCharacterString = "#LongChineseBlob $longChineseBlob"

        val expectedFirstTweet = "#LongChineseBlob 环颈鸻是鸻形目鸻科鸻属的鸟类台湾称为东方环颈鸻因为其后颈基部白色" +
                "环颈鸻是鸻形目鸻科鸻属的鸟类台湾称为东方环颈鸻因为其后颈基部白色" +
                "环颈鸻是鸻形目鸻科鸻属的鸟类台湾称为东方环颈鸻因为其后颈基部白色" +
                "环颈鸻是鸻形目鸻科鸻属的鸟类台湾称为东方环颈鸻因为其后颈基部白色" +
                "环颈鸻"

        val expectedSecondTweet = handle + handlePostFix + "是鸻形目鸻科鸻属的鸟类台湾称为东方环颈鸻因为其后颈基部白色"

        val processor = TextToTweetsProcessor(longChineseCharacterString, handle, handlePostFix,
            numberPrefix, false, tweetMaxWeightedLength, shortenedUrlLength)

        assertThat(longChineseBlob.length).isEqualTo(160)
        runBlocking {
            assertThat(processor.nextTweet()).isEqualTo(expectedFirstTweet)
            assertThat(processor.nextTweet()).isEqualTo(expectedSecondTweet)
        }
    }
}