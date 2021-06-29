package com.muchen.tweetstormmaker.persistence.room

import androidx.room.*
import com.muchen.tweetstormmaker.persistence.room.model.TwitterUser
import com.muchen.tweetstormmaker.persistence.room.model.TwitterUserAndTokens
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomTwitterUserAndTokensDao {
    @Query("Select * From user_and_tokens Limit 1")
    fun getOneTwitterUserAndTokens(): Flow<TwitterUserAndTokens?>

    @Query("Delete From user_and_tokens")
    suspend fun deleteAllTwitterUserAndTokens()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTwitterUserAndTokens(userAndTokens: TwitterUserAndTokens)

    @Delete
    suspend fun deleteTwitterUserAndTokens(userAndTokens: TwitterUserAndTokens)

    @Update
    suspend fun updateTwitterUserAndTokens(userAndTokens: TwitterUserAndTokens)

    @Update(entity = TwitterUserAndTokens::class)
    suspend fun updateTwitterUser(user: TwitterUser)
}