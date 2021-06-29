package com.muchen.tweetstormmaker.androidui.adatper

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.twitter.twittertext.Extractor

const val TAG = "BindingAdapter"

@BindingAdapter(value = ["twitterEntityTextColor"])
fun stylizeTwitterEntitiesInText(view: TextView, color: Int) {
    val text: String = view.text.toString()
    Log.d(TAG, "text: $text")
    if (text.isNotBlank()) {
        val twitterEntityList = Extractor().extractEntitiesWithIndices(text)
        if (twitterEntityList.isNotEmpty()) {
            val spannableString = SpannableString(text)
            for (entity in twitterEntityList) {
                /* Extractor.Entity.getStart() returns the inclusive start index and
                   Extractor.Entity.getEnd() returns the exclusive end index. */
                Log.d(TAG, "twitter entity: ${text.substring(entity.start, entity.end)}" +
                        ", start: ${entity.start}, end: ${entity.end}")
                if (entity.type == Extractor.Entity.Type.URL) {
                    /* SpannableString.setSpan(...) follows the same indexing convention as
                       String.substring(int, int) */
                    spannableString.setSpan(UnderlineSpan(), entity.start, entity.end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                spannableString.setSpan(ForegroundColorSpan(color), entity.start, entity.end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            view.text = spannableString
        } else {
            Log.d(TAG, "twitter entity: null")
            view.text = text
        }
    }
}