package com.muchen.tweetstormmaker.interfaceadapter.usecase.database

import androidx.annotation.VisibleForTesting
import com.muchen.tweetstormmaker.interfaceadapter.model.Draft
import com.muchen.tweetstormmaker.interfaceadapter.model.DraftContent
import com.muchen.tweetstormmaker.interfaceadapter.model.DraftSentStatus
import com.muchen.tweetstormmaker.interfaceadapter.repository.IDraftsRepository

class DraftsCRUDUseCases(private val repo: IDraftsRepository) {

    fun getLocalDrafts() = repo.getLocalDrafts()

    fun getPartiallySentDrafts() = repo.getPartiallySentDrafts()

    fun getSentDrafts() = repo.getSentDrafts()

    fun getDraftByTimeCreated(timeCreated: Long) = repo.getDraftByTimeCreated(timeCreated)

    suspend fun insertDraft(draft: Draft) = repo.insertDraft(draft)

    suspend fun updateDraft(draft: Draft) = repo.updateDraft(draft)

    suspend fun updateDraftContent(partialDraft: DraftContent) =
            repo.updateDraftContent(partialDraft)

    suspend fun updateDraftSentStatus(partialDraft: DraftSentStatus) =
            repo.updateDraftSentStatus(partialDraft)

    suspend fun deleteDraftByTimeCreated(time: Long) = repo.deleteDraftByTimeCreated(time)

    @VisibleForTesting
    suspend fun deleteAllDrafts() = repo.deleteAllDrafts()
}