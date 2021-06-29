package com.muchen.tweetstormmaker.interfaceadapter.repository

import androidx.annotation.VisibleForTesting
import com.muchen.tweetstormmaker.interfaceadapter.model.*
import kotlinx.coroutines.flow.Flow

interface IPersistence {

    fun getDraftsBySentStatus(sentStatusEnum: SentStatusEnum): Flow<List<Draft>>

    fun getDraftByTimeCreated(time: Long): Flow<Draft?>

    @VisibleForTesting
    suspend fun deleteAllDrafts()

    suspend fun deleteDraftByTimeCreated(time: Long)

    suspend fun deleteDraft(draft: Draft)

    suspend fun insertDraft(draft: Draft)

    suspend fun updateDraft(draft: Draft)

    suspend fun updateDraftContent(content: DraftContent)

    suspend fun updateDraftSentStatus(sentStatus: DraftSentStatus)

    fun getOneTwitterUserAndTokens(): Flow<TwitterUserAndTokens?>

    suspend fun deleteAllTwitterUserAndTokens()

    suspend fun deleteTwitterUserAndTokens(userAndTokens: TwitterUserAndTokens)

    suspend fun insertTwitterUserAndTokens(userAndTokens: TwitterUserAndTokens)

    suspend fun updateTwitterUserAndTokens(userAndTokens: TwitterUserAndTokens)

    suspend fun updateTwitterUser(user: TwitterUser)
}