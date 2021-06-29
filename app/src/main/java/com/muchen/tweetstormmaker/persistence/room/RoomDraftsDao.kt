package com.muchen.tweetstormmaker.persistence.room

import androidx.room.*
import com.muchen.tweetstormmaker.persistence.room.model.Draft
import com.muchen.tweetstormmaker.persistence.room.model.DraftContent
import com.muchen.tweetstormmaker.persistence.room.model.DraftSentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDraftsDao {

    @Query("Select * From tweetstorm_drafts Where sent_status = :sentStatus Order By time_created Desc")
    fun getDraftsBySentStatus(sentStatus: Int): Flow<List<Draft>>

    @Query("Select * From tweetstorm_drafts Where time_created = :time ")
    fun getDraftByTimeCreated(time: Long): Flow<Draft?>

    @Query("Delete From tweetstorm_drafts where time_created = :time")
    suspend fun deleteDraftByTimeCreated(time: Long)

    @Query("Delete From tweetstorm_drafts")
    suspend fun deleteAllDrafts()

    @Delete
    suspend fun deleteDraft(draft: Draft)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: Draft)

    @Update
    suspend fun updateDraft(draft: Draft)

    @Update(entity = Draft::class)
    suspend fun updateDraftContent(content: DraftContent)

    @Update(entity = Draft::class)
    suspend fun updateDraftSentStatus(partialDraft: DraftSentStatus)
}