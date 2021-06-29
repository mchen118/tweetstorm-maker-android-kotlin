package com.muchen.tweetstormmaker.androidui.viewmodel

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.muchen.tweetstormmaker.androidui.mapper.toIAModel
import com.muchen.tweetstormmaker.androidui.mapper.toUIModel
import com.muchen.tweetstormmaker.androidui.model.Draft
import com.muchen.tweetstormmaker.androidui.model.DraftContent
import com.muchen.tweetstormmaker.interfaceadapter.usecase.database.DraftsCRUDUseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DraftsViewModel(private val draftsCRUDUseCases: DraftsCRUDUseCases) : ViewModel() {

    val localDrafts: LiveData<List<Draft>> =
            draftsCRUDUseCases.getLocalDrafts().toUIModel().asLiveData()

    val partiallySentDrafts: LiveData<List<Draft>> =
            draftsCRUDUseCases.getPartiallySentDrafts().toUIModel().asLiveData()

    val sentDrafts: LiveData<List<Draft>> =
            draftsCRUDUseCases.getSentDrafts().toUIModel().asLiveData()

    fun getDraftByTimeCreated(timeCreated: Long): LiveData<Draft?> =
            draftsCRUDUseCases.getDraftByTimeCreated(timeCreated).toUIModel().asLiveData()

    private val scope = viewModelScope

    fun insertDraft(draft: Draft) {
        scope.launch(Dispatchers.IO) {
            draftsCRUDUseCases.insertDraft(draft.toIAModel())
        }
    }

    fun deleteDraftByTimeCreated(time: Long) {
        scope.launch(Dispatchers.IO) {
            draftsCRUDUseCases.deleteDraftByTimeCreated(time)
        }
    }

    fun updateDraftContent(partialDraft: DraftContent) {
        scope.launch(Dispatchers.IO) {
            draftsCRUDUseCases.updateDraftContent(partialDraft.toIAModel())
        }
    }

    @VisibleForTesting
    fun deleteAllDrafts() {
        scope.launch(Dispatchers.IO) {
            draftsCRUDUseCases.deleteAllDrafts()
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(this::class.simpleName, "onCleared()")
    }
}