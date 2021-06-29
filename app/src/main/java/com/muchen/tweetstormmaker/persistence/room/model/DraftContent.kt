package com.muchen.tweetstormmaker.persistence.room.model

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class DraftContent(@PrimaryKey
                        @ColumnInfo(name = "time_created") val timeCreated : Long,

                        @ColumnInfo(name = "content")
                        var content: String = "")