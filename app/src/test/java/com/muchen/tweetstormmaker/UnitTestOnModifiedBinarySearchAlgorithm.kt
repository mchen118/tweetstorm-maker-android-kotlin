package com.muchen.tweetstormmaker

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UnitTestOnModifiedBinarySearchAlgorithm {

    @Test
    fun test() {
        val list1 = listOf(1)
        val list2 = listOf(1, 3)
        val list3 = listOf(1, 3, 5)

        assertThat(list1.findLargestLessThanOrEqualTo(0)).isEqualTo(null)
        assertThat(list1.findLargestLessThanOrEqualTo(1)).isEqualTo(0)
        assertThat(list1.findLargestLessThanOrEqualTo(2)).isEqualTo(0)

        assertThat(list2.findLargestLessThanOrEqualTo(0)).isEqualTo(null)
        assertThat(list2.findLargestLessThanOrEqualTo(1)).isEqualTo(0)
        assertThat(list2.findLargestLessThanOrEqualTo(2)).isEqualTo(0)
        assertThat(list2.findLargestLessThanOrEqualTo(3)).isEqualTo(1)
        assertThat(list2.findLargestLessThanOrEqualTo(4)).isEqualTo(1)

        assertThat(list3.findLargestLessThanOrEqualTo(0)).isEqualTo(null)
        assertThat(list3.findLargestLessThanOrEqualTo(1)).isEqualTo(0)
        assertThat(list3.findLargestLessThanOrEqualTo(2)).isEqualTo(0)
        assertThat(list3.findLargestLessThanOrEqualTo(3)).isEqualTo(1)
        assertThat(list3.findLargestLessThanOrEqualTo(4)).isEqualTo(1)
        assertThat(list3.findLargestLessThanOrEqualTo(5)).isEqualTo(2)
        assertThat(list3.findLargestLessThanOrEqualTo(6)).isEqualTo(2)
    }

    /**
     * @receiver sorted list of integers with increasing values
     * @return index of of the largest element in [this] that is less than or equal to [value]; null if such element does not exist
     */
    private fun List<Int>.findLargestLessThanOrEqualTo(value: Int): Int? {
        if (this.isEmpty() || this.first() > value) return null

        // mid is in [0, 0, 1, 1, 2, 2...] for end index [0, 1, 2, 3, 4, 5...]
        var start = 0
        var end = this.size - 1
//        var mid = ((end + start) - (end + start) % 2) / 2
        var mid = (start + end) / 2
        var prev = mid
        var result = mid

        while (start <= end) {
            result = mid
            when {
                value < this[mid] -> {
                    if (value > this[prev]) {
                        result = prev
                        break
                    } else {
                        end = mid - 1
                        prev = mid
//                        mid = ((end + start) - (end + start) % 2) / 2
                        mid = (start + end) / 2
                    }
                }
                value == this[mid] -> break
                else -> {
                    start = mid + 1
                    prev = mid
//                    mid = ((end + start) - (end + start) % 2) / 2
                    mid = (start + end) / 2
                }
            }
        }

        return result
    }
}