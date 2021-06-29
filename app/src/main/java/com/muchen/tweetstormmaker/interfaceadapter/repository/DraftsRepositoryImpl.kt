package com.muchen.tweetstormmaker.interfaceadapter.repository

import androidx.annotation.VisibleForTesting
import com.muchen.tweetstormmaker.interfaceadapter.model.Draft
import com.muchen.tweetstormmaker.interfaceadapter.model.DraftContent
import com.muchen.tweetstormmaker.interfaceadapter.model.DraftSentStatus
import com.muchen.tweetstormmaker.interfaceadapter.model.SentStatusEnum

class DraftsRepositoryImpl (private val persistence : IPersistence): IDraftsRepository {

    override fun getLocalDrafts() = persistence.getDraftsBySentStatus(SentStatusEnum.LOCAL)

    override fun getSentDrafts() = persistence.getDraftsBySentStatus(SentStatusEnum.FULLY_SENT)

    override fun getPartiallySentDrafts() = persistence.getDraftsBySentStatus(SentStatusEnum.PARTIALLY_SENT)

    override fun getDraftByTimeCreated(time: Long) = persistence.getDraftByTimeCreated(time)

    override suspend fun insertDraft(draft : Draft) = persistence.insertDraft(draft)

    override suspend fun deleteDraftByTimeCreated(time : Long) = persistence.deleteDraftByTimeCreated(time)

    @VisibleForTesting
    override suspend fun deleteAllDrafts() = persistence.deleteAllDrafts()

    override suspend fun updateDraft(draft: Draft) = persistence.updateDraft(draft)

    override suspend fun updateDraftContent(partialDraft: DraftContent) = persistence.updateDraftContent(partialDraft)

    override suspend fun updateDraftSentStatus(partialDraft: DraftSentStatus) = persistence.updateDraftSentStatus(partialDraft)
}