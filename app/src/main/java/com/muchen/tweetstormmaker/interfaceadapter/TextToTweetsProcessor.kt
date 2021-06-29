package com.muchen.tweetstormmaker.interfaceadapter

import com.twitter.twittertext.Extractor
import com.twitter.twittertext.TwitterTextParser
import java.util.*
import kotlin.Comparator

/**
 * The correct way of using this class is by iteratively calling [hasNextTweet] and [nextTweet] in
 * ordered pairs.
 *
 * Each generated tweet has this format: [twitterHandle] + [twitterHandlePostfix] +
 * *body* + [tweetNumberPrefix] + *tweet number*.
 * - [twitterHandle] and [twitterHandlePostfix] are not included for the first tweet
 * in the tweetstorm, but otherwise they are always included.
 * - [tweetNumberPrefix] and tweet number are always included unless [numberingTweets] is false
 * - *body* is determined by the algorithm to avoid having parts of one Twitter entity (hashtags,
 * cashtags, mentions, and urls) or one word in more than one tweet, and to not exceed the weighed
 * maximum length for each tweet.
 *
 * @param text the full text that the user wants to tweet out as a tweetstorm/thread
 * @param twitterHandle user's Twitter handle, which starts with @
 * @param twitterHandlePostfix string that is appended after [twitterHandle] in tweet construction
 * @param tweetNumberPrefix string that is appended before *tweet number* in tweet construction
 * @param numberingTweets whether to number each tweet in a tweetstorm
 * @param tweetMaxWeightedLength maximum weighed length of a tweet
 * @param shortenedUrlMaxLength maximum length of a t.co url
 */
class TextToTweetsProcessor(private val text: String,
                            private val twitterHandle: String,
                            private val twitterHandlePostfix: String,
                            private val tweetNumberPrefix: String,
                            private val numberingTweets: Boolean,
                            private val tweetMaxWeightedLength: Int,
                            private val shortenedUrlMaxLength: Int) {
    /* Any variable named startIndex is inclusive, while any variable named endIndex is exclusive,
       unless specified otherwise to conform with indexing convention of [String.substring].*/
    private var startIndex = 0
    private var endIndex = 0
    private var tweetNumber = 1
    private val textPartitioner = when (numberingTweets) {
        true -> TextPartitioner(twitterHandle.length, twitterHandlePostfix.length,
                tweetNumberPrefix.length)
        false -> TextPartitioner(twitterHandle.length, twitterHandlePostfix.length,
                0)
    }

    /**
     * Use the method to determine whether a [nextTweet] call can be invoked sanely.
     * @return Whether [text] has been exhausted
     */
    fun hasNextTweet(): Boolean = (endIndex < text.length && text.isNotBlank())

    /**
     * @return content of the next tweet
     */
    fun nextTweet(): String {
        endIndex = textPartitioner.calculateEndIndex(startIndex, tweetNumber)

        var nextTweet = text.substring(startIndex, endIndex)
        if (tweetNumber != 1) nextTweet = twitterHandle + twitterHandlePostfix + nextTweet
        if (numberingTweets) nextTweet = nextTweet + tweetNumberPrefix + tweetNumber

        startIndex = endIndex
        tweetNumber ++

        return nextTweet
    }

    /**
     * Inner class that exposes one API that calculates end index for [TextToTweetsProcessor]
     * Outer class's members that are used by it includes [text], [numberingTweets],
     * [tweetMaxWeightedLength], and [shortenedUrlMaxLength]
     *
     * @param twitterHandleLength length of user's Twitter handle
     * @param twitterHandlePostfixLength length of Twitter handle's postfix
     * @param tweetNumberPrefixLength length of tweet number's prefix
     */
    private inner class TextPartitioner(private val twitterHandleLength: Int,
                                        private val twitterHandlePostfixLength: Int,
                                        private val tweetNumberPrefixLength: Int) {

        private val sortedEntityList by lazy {
            // Extractor.extractEntitiesWithIndices() does not allow overlapping entities
            sortEntityList(Extractor().extractEntitiesWithIndices(text))
        }

        private var currentLongWord: IntRange? = null

        /**
         * This is the API that is exposed to [TextToTweetsProcessor]
         *
         * @param startIndex start index
         * @param tweetNumber tweet number
         * @return end index
         */
        fun calculateEndIndex(startIndex: Int, tweetNumber: Int): Int {
            val tentativeEndIndex = getTentativeEndIndex(startIndex)
            return finalizeEndIndex(startIndex, tentativeEndIndex, tweetNumber)
        }

        /**
         * @param list list of twitter entities
         * @return list of twitter entities sorted in ascending order based on each entity's start index
         */
        private fun sortEntityList(list: List<Extractor.Entity>): List<Extractor.Entity> {
            val entityComparator = Comparator<Extractor.Entity> { o1, o2 ->
                val start1 = o1.start
                val start2 = o2.start
                when {
                    start1 == start2 -> 0
                    start1 < start2 -> -1
                    else -> 1
                }
            }
            // Collections.sort uses merge sort algorithm
            Collections.sort(list, entityComparator)
            return list
        }

        /**
         * @param startIndex start index
         * @return end index without taking [tweetMaxWeightedLength] into account
         */
        private fun getTentativeEndIndex(startIndex: Int): Int {
            // ensure tentativeEndIndex never goes out of bound
            var tentativeEndIndex = text.length.coerceAtMost(startIndex + tweetMaxWeightedLength)
            // in case there are supplementary characters that take up two indices per code point
            tentativeEndIndex = avoidSplittingSurrogatePair(tentativeEndIndex)
            tentativeEndIndex = avoidSplittingEntitiesOrLastWord(startIndex, tentativeEndIndex)
            println("getTentativeEndIndex [$startIndex, $tentativeEndIndex), last char: ${text[tentativeEndIndex - 1]}")
            return tentativeEndIndex
        }

        /**
         * @param endIndex end index
         * @return if necessary, a new end index that does not point to a high surrogate
         */
        private fun avoidSplittingSurrogatePair(endIndex: Int): Int {
            return if (text[endIndex - 1].isHighSurrogate()) {
                endIndex - 1
            } else {
                endIndex
            }
        }

        /**
         * @param startIndex start index
         * @param endIndex end index
         * @param tweetNumber the current tweet number
         * @return new end Index that does take [tweetMaxWeightedLength] into account
         */
        private fun finalizeEndIndex(startIndex: Int, endIndex: Int, tweetNumber: Int): Int {
            var adjustedEndIndex = endIndex

            val currentText = text.substring(startIndex, endIndex)
            var parseResult = TwitterTextParser.parseTweet(currentText)

            var offset = 0
            if (tweetNumber > 1) offset += twitterHandleLength + twitterHandlePostfixLength
            if (numberingTweets) offset += tweetNumberPrefixLength + tweetNumber.toString().length

            while (parseResult.weightedLength > tweetMaxWeightedLength - offset) {
                adjustedEndIndex = text.offsetByCodePoints(adjustedEndIndex, -1)
                adjustedEndIndex = avoidSplittingEntitiesOrLastWord(startIndex, adjustedEndIndex)
                parseResult = TwitterTextParser.parseTweet(text.substring(startIndex, adjustedEndIndex))
                println("finalizeEndIndex while loop: $startIndex, $adjustedEndIndex")
            }

            return adjustedEndIndex
        }

        /**
         * @param startIndex start index
         * @param endIndex end index
         * @return new end index that does not split the last word or any twitter entity
         */
        private fun avoidSplittingEntitiesOrLastWord(startIndex: Int, endIndex: Int): Int {
            val entityBeforeIndex = findClosestEntityBefore(endIndex)

            if (entityBeforeIndex == null) {
                return if (sortedEntityList.isEmpty()) {
                    avoidSplittingLastWord(startIndex, endIndex, null)
                } else {
                    avoidSplittingLastWord(startIndex, endIndex, sortedEntityList.first())
                }
            }

            val entityBefore = sortedEntityList[entityBeforeIndex]

            return when {
                endIndex > entityBefore.end -> {
                    val entityAfter = if (entityBeforeIndex == sortedEntityList.size - 1) {
                        null
                    } else {
                        sortedEntityList[entityBeforeIndex + 1]
                    }
                    avoidSplittingLastWord(entityBefore.end, endIndex, entityAfter)
                }
                endIndex == entityBefore.end -> endIndex
                else -> {
                    // when entityBefore.start <= endIndex < entityBefore.end
                    if (entityBefore.type == Extractor.Entity.Type.URL
                        && (endIndex - entityBefore.start + 1) >= shortenedUrlMaxLength
                    ) {
                        /* If endIndex is in the middle of a URL, because of Twitter's mandatory
                           link shortening policy (which currently shortens a URL to a
                           max length of 23), it actually decreases or does not increase the
                           weighted length of a tweet to include the whole URL in the next tweet if
                           no larger than 23 characters of the URL is already included.*/
                        entityBefore.end
                    } else {
                        entityBefore.start
                    }
                }
            }
        }

        /**
         * @param index index in [text]
         * @return index of the [Extractor.Entity] with the largest index in [sortedEntities] whose [Extractor.Entity.start] is less than [index], or null if none exists
         */
        private fun findClosestEntityBefore(index: Int): Int? {
            val inclusiveIndex = index - 1
            if (sortedEntityList.isEmpty() || sortedEntityList.first().start > inclusiveIndex) {
                return null
            }

            // mid is in [0, 0, 1, 1, 2, 2...] for end index [0, 1, 2, 3, 4, 5...]
            var start = 0
            var end = sortedEntityList.size - 1
            // integer division
            var mid = (end + start) / 2
            var prev = mid
            var result = mid

            while (start <= end) {
                result = mid
                when {
                    inclusiveIndex < sortedEntityList[mid].start -> {
                        if (inclusiveIndex > sortedEntityList[prev].start) {
                            result = prev
                            break
                        } else {
                            end = mid - 1
                            prev = mid
                            mid = (end + start) / 2
                        }
                    }
                    inclusiveIndex == sortedEntityList[mid].start -> break
                    else -> {
                        start = mid + 1
                        prev = mid
                        mid = (end + start) / 2
                    }
                }
            }

            return result
        }

        /**
         * @param startIndex start index
         * @param endIndex end index
         * @param entityAfter twitter entity after [endIndex], or null if no such entity exists
         * @return new end index that does not split the last word
         */
        private fun avoidSplittingLastWord(startIndex: Int, endIndex: Int,
                                           entityAfter: Extractor.Entity?): Int {
            /* optimization: each long word is only calculated once and kept only as long as
               they are needed until being replaced by the next long word */
            if (currentLongWord?.contains(endIndex) == true) return endIndex

            /* nextChar is null if endIndex equals the last index of text + 1, or if endIndex equals
               the beginning of an entity */
            val nextChar = if (endIndex == text.length || entityAfter?.start == endIndex) {
                null
            } else {
                text.codePointAt(endIndex)
            }

            val lastChar = text.codePointBefore(endIndex)

            var adjustedEndIndex = endIndex

            if (Character.isLetter(lastChar) && nextChar != null && Character.isLetter(nextChar)) {
                // in case endIndex is in a long word
                val upperBound = entityAfter?.start ?: text.length - 1
                val lowerBound = startIndex
                var longWordEndIndex = endIndex
                var longWordStartIndex = endIndex
                println("avoidSplittingLastWord longWordEndIndex: $longWordEndIndex, upperBound: $upperBound, text length: ${text.length}")
                while (longWordEndIndex < upperBound) {
                    if (Character.isLetter(text.codePointAt(longWordEndIndex))) {
                        longWordEndIndex = text.offsetByCodePoints(longWordEndIndex, 1)
                    } else {
                        println("avoidSplittingLastWord codePointAt $longWordEndIndex: ${text.codePointAt(longWordEndIndex)} is not a letter")
                        break
                    }
                    println("avoidSplittingLastWord longWordEndIndex: $longWordEndIndex, upperBound: $upperBound")
                }
                println("avoidSplittingLastWord longWordStartIndex: $longWordStartIndex, lowerBound: $lowerBound")
                // make longWordEndIndex exclusive
                longWordEndIndex ++
                while (longWordStartIndex > lowerBound) {
                    if (Character.isLetter(text.codePointBefore(longWordStartIndex))) {
                        longWordStartIndex = text.offsetByCodePoints(longWordStartIndex, -1)
                    } else {
                        println("avoidSplittingLastWord codePointBefore $longWordStartIndex: ${text.codePointBefore(longWordStartIndex)} is not a letter")
                        break
                    }
                    println("avoidSplittingLastWord longWordStartIndex: $longWordStartIndex, lowerBound: $lowerBound")
                }
                println("avoidSplittingLastWord longWord: [$longWordStartIndex, $longWordEndIndex), last char: ${text[longWordEndIndex - 1]}")
                val longWordParseResult = TwitterTextParser.parseTweet(
                        text.substring(longWordStartIndex, longWordEndIndex))
                if (longWordParseResult.weightedLength > tweetMaxWeightedLength) {
                    /* In case endIndex splits a word whose max weighted length is larger than
                       the max weighted length of a tweet, just split it anyway. */
                    currentLongWord = longWordStartIndex..longWordEndIndex
                    return endIndex
                }

                while (Character.isLetter(text.codePointBefore(adjustedEndIndex)) && adjustedEndIndex != startIndex) {
                    adjustedEndIndex = text.offsetByCodePoints(adjustedEndIndex, -1)
                }
            }

            return adjustedEndIndex
        }
    }
}
