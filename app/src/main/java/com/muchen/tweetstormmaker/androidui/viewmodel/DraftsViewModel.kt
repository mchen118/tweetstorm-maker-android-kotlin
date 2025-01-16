package com.muchen.tweetstormmaker.androidui.viewmodel

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.muchen.tweetstormmaker.interfaceadapter.model.Draft
import com.muchen.tweetstormmaker.interfaceadapter.model.DraftContent
import com.muchen.tweetstormmaker.interfaceadapter.model.SentStatusEnum
import com.muchen.tweetstormmaker.interfaceadapter.repository.IPersistence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DraftsViewModel(private val persistence: IPersistence) : ViewModel() {

    val localDrafts: LiveData<List<Draft>> =
        persistence.getDraftsBySentStatus(SentStatusEnum.LOCAL).asLiveData()

    val partiallySentDrafts: LiveData<List<Draft>> =
        persistence.getDraftsBySentStatus(SentStatusEnum.PARTIALLY_SENT).asLiveData()

    val sentDrafts: LiveData<List<Draft>> =
        persistence.getDraftsBySentStatus(SentStatusEnum.FULLY_SENT).asLiveData()

    fun getDraftByTimeCreated(timeCreated: Long): LiveData<Draft?> =
        persistence.getDraftByTimeCreated(timeCreated).asLiveData()

    private val scope = viewModelScope

    fun insertDraft(draft: Draft) {
        scope.launch(Dispatchers.IO) {
            Log.d(TAG, "insert Draft: ${draft.timeCreated}")
            persistence.insertDraft(draft)
        }
    }

    fun deleteDraftByTimeCreated(time: Long) {
        scope.launch(Dispatchers.IO) {
            Log.d(TAG, "delete Draft: ${time}")
            persistence.deleteDraftByTimeCreated(time)
        }
    }

    fun updateDraftContent(partialDraft: DraftContent) {
        scope.launch(Dispatchers.IO) {
            Log.d(TAG, "update Draft content: ${partialDraft.timeCreated}")
            persistence.updateDraftContent(partialDraft)
        }
    }

    @VisibleForTesting
    fun deleteAllDrafts() {
        scope.launch(Dispatchers.IO) {
            persistence.deleteAllDrafts()
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(this::class.simpleName, "onCleared()")
    }

    companion object {
        const val TAG = "DraftsViewModel"
    }
}