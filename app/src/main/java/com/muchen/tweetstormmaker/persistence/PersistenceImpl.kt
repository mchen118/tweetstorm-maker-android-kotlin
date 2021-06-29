package com.muchen.tweetstormmaker.persistence

import com.muchen.tweetstormmaker.interfaceadapter.model.*
import com.muchen.tweetstormmaker.interfaceadapter.repository.IPersistence
import com.muchen.tweetstormmaker.persistence.mapper.toIAModel
import com.muchen.tweetstormmaker.persistence.mapper.toPModel
import com.muchen.tweetstormmaker.persistence.room.RoomAppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PersistenceImpl constructor(private val roomAppDatabase: RoomAppDatabase): IPersistence {

    companion object {
        @Volatile private var soleInstance: IPersistence? = null

        fun getInstance(roomAppDatabase: RoomAppDatabase): IPersistence {
            return soleInstance ?: synchronized(this) {
                soleInstance ?: PersistenceImpl(roomAppDatabase)
            }
        }
    }

    override fun getDraftsBySentStatus(sentStatusEnum: SentStatusEnum) =
            roomAppDatabase.draftDao().getDraftsBySentStatus(sentStatusEnum.toPModel()).toIAModel()

    override fun getDraftByTimeCreated(time: Long) =
            roomAppDatabase.draftDao().getDraftByTimeCreated(time).toIAModel()

    override suspend fun deleteAllDrafts() =
            roomAppDatabase.draftDao().deleteAllDrafts()

    override suspend fun deleteDraftByTimeCreated(time: Long) {
        return withContext(Dispatchers.IO) {
            roomAppDatabase.draftDao().deleteDraftByTimeCreated(time)
        }
    }

    override suspend fun deleteDraft(draft: Draft) {
        return withContext(Dispatchers.IO) {
            roomAppDatabase.draftDao().deleteDraft(draft.toPModel())
        }
    }

    override suspend fun insertDraft(draft: Draft) {
        return withContext(Dispatchers.IO) {
            roomAppDatabase.draftDao().insertDraft(draft.toPModel())
        }
    }

    override suspend fun updateDraft(draft: Draft) {
        return withContext(Dispatchers.IO) {
            roomAppDatabase.draftDao().updateDraft(draft.toPModel())
        }
    }

    override suspend fun updateDraftContent(content: DraftContent) {
        return withContext(Dispatchers.IO) {
            roomAppDatabase.draftDao().updateDraftContent(content.toPModel())
        }
    }

    override suspend fun updateDraftSentStatus(sentStatus: DraftSentStatus) {
        return withContext(Dispatchers.IO) {
            roomAppDatabase.draftDao().updateDraftSentStatus(sentStatus.toPModel())
        }
    }

    override fun getOneTwitterUserAndTokens() =
            roomAppDatabase.twitterUserAndTokensDao().getOneTwitterUserAndTokens().toIAModel()

    override suspend fun deleteAllTwitterUserAndTokens() {
        return withContext(Dispatchers.IO) {
            roomAppDatabase.twitterUserAndTokensDao().deleteAllTwitterUserAndTokens()
        }
    }

    override suspend fun deleteTwitterUserAndTokens(userAndTokens: TwitterUserAndTokens) {
        return withContext(Dispatchers.IO) {
            roomAppDatabase.twitterUserAndTokensDao().deleteTwitterUserAndTokens(userAndTokens.toPModel())
        }
    }

    override suspend fun insertTwitterUserAndTokens(userAndTokens: TwitterUserAndTokens) {
        return withContext(Dispatchers.IO) {
            roomAppDatabase.twitterUserAndTokensDao().insertTwitterUserAndTokens(userAndTokens.toPModel())
        }
    }

    override suspend fun updateTwitterUserAndTokens(userAndTokens: TwitterUserAndTokens) {
        return withContext(Dispatchers.IO) {
            roomAppDatabase.twitterUserAndTokensDao().updateTwitterUserAndTokens(userAndTokens.toPModel())
        }
    }

    override suspend fun updateTwitterUser(user: TwitterUser) {
        return withContext(Dispatchers.IO) {
            roomAppDatabase.twitterUserAndTokensDao().updateTwitterUser(user.toPModel())
        }
    }
}