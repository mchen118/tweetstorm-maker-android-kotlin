package com.muchen.tweetstormmaker.persistence.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.muchen.tweetstormmaker.persistence.PersistenceConstants.PERSISTENCE_DEFAULT_SENT_STATUS

@Entity(tableName = "tweetstorm_drafts")
data class Draft (@PrimaryKey
                  @ColumnInfo(name = "time_created") val timeCreated : Long,

                  @ColumnInfo(name = "content")
                  var content: String = "",

                  @ColumnInfo(name = "sent_status")
                  var sentStatus: Int = PERSISTENCE_DEFAULT_SENT_STATUS,

                  @ColumnInfo(name = "sent_ids")
                  var sentIds: String = "")