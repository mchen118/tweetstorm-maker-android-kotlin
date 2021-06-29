package com.muchen.tweetstormmaker.interfaceadapter.repository

import androidx.annotation.VisibleForTesting
import com.muchen.tweetstormmaker.interfaceadapter.model.Draft
import com.muchen.tweetstormmaker.interfaceadapter.model.DraftContent
import com.muchen.tweetstormmaker.interfaceadapter.model.DraftSentStatus
import kotlinx.coroutines.flow.Flow

interface IDraftsRepository {

    fun getLocalDrafts(): Flow<List<Draft>>

    fun getSentDrafts(): Flow<List<Draft>>

    fun getPartiallySentDrafts(): Flow<List<Draft>>

    fun getDraftByTimeCreated(time: Long): Flow<Draft?>

    suspend fun insertDraft(draft : Draft)

    suspend fun deleteDraftByTimeCreated(time : Long)

    @VisibleForTesting
    suspend fun deleteAllDrafts()

    suspend fun updateDraft(draft: Draft)

    suspend fun updateDraftContent(partialDraft: DraftContent)

    suspend fun updateDraftSentStatus(partialDraft: DraftSentStatus)
}