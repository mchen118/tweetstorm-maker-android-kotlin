package com.muchen.tweetstormmaker.persistence

import com.muchen.tweetstormmaker.persistence.model.SentStatusEnum

object PersistenceConstants {

    const val DATABASE_NAME = "drafts" // for backwards compatibility

    val PERSISTENCE_DEFAULT_SENT_STATUS = SentStatusEnum.LOCAL.ordinal
}