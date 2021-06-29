package com.muchen.tweetstormmaker.persistence.room.model

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.muchen.tweetstormmaker.persistence.PersistenceConstants.PERSISTENCE_DEFAULT_SENT_STATUS

data class DraftSentStatus (@PrimaryKey
                            @ColumnInfo(name = "time_created") val timeCreated : Long,

                            @ColumnInfo(name = "sent_status")
                            var sentStatus: Int = PERSISTENCE_DEFAULT_SENT_STATUS,

                            @ColumnInfo(name = "sent_ids")
                            var sentIds: String = "")