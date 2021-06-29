package com.muchen.tweetstormmaker.persistence.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.muchen.tweetstormmaker.persistence.PersistenceConstants.DATABASE_NAME
import com.muchen.tweetstormmaker.persistence.room.model.Draft
import com.muchen.tweetstormmaker.persistence.room.model.TwitterUserAndTokens

@Database(entities = [Draft::class, TwitterUserAndTokens::class], version = 2, exportSchema = false)
abstract class RoomAppDatabase : RoomDatabase() {

    abstract fun draftDao(): RoomDraftsDao

    abstract fun twitterUserAndTokensDao(): RoomTwitterUserAndTokensDao

    // singleton
    companion object {

        @Volatile private var instance: RoomAppDatabase? = null

        fun getInstance(context: Context): RoomAppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) : RoomAppDatabase {
            return Room.databaseBuilder(context.applicationContext, RoomAppDatabase::class.java, DATABASE_NAME)
                       .fallbackToDestructiveMigration()
                       .build()
        }
    }
}