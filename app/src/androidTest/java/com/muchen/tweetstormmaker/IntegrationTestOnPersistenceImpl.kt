package com.muchen.tweetstormmaker

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.muchen.tweetstormmaker.interfaceadapter.model.*
import com.muchen.tweetstormmaker.persistence.PersistenceImpl
import com.muchen.tweetstormmaker.persistence.room.RoomAppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class IntegrationTestOnPersistenceImpl {

    private lateinit var persistenceImpl: PersistenceImpl
    private lateinit var db: RoomAppDatabase
    private val draft1 = Draft(1L)
    private val draft2 = Draft(2L)
    private val draft3 = Draft(3L)
    private val updatedDraft1 = Draft(1L, "xx")
    private val draftContent1 = DraftContent(1L, "xx")
    private val draftSentStatus2 = DraftSentStatus(2L, SentStatusEnum.LOCAL, "1321321421")
    private val twitterUserAndTokens1 = TwitterUserAndTokens("1", "name", "screen_name", "", "","")
    private val updatedTwitterUserAndTokens1 = TwitterUserAndTokens("1", "name", "screen_name", "", "xxxx","")
    private val twitterUser1 = TwitterUser("1", "name_changed", "screen_name_changed", "")

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, RoomAppDatabase::class.java).build()
        persistenceImpl = PersistenceImpl(db)
    }

    @After
    @Throws(IOException::class)
    fun clearDb() {
        db.close()
    }

    @Test
    fun getDraftsBySentStatus() {
        runBlocking {
            persistenceImpl.insertDraft(draft1)
            persistenceImpl.insertDraft(draft2)
            val resultDraftList = persistenceImpl.getDraftsBySentStatus(SentStatusEnum.LOCAL).first()
            assertThat(resultDraftList).hasSize(2)
        }
    }

    @Test
    fun getDraftByTimeCreated() {
        runBlocking {
            persistenceImpl.insertDraft(draft1)
            persistenceImpl.insertDraft(draft2)
            val resultDraft = persistenceImpl.getDraftByTimeCreated(draft1.timeCreated).first()
            assertThat(resultDraft == draft1).isTrue()
        }
    }

    @Test
    fun deleteDraftByTimeCreated() {
        runBlocking {
            persistenceImpl.insertDraft(draft1)
            persistenceImpl.insertDraft(draft2)
            persistenceImpl.deleteDraftByTimeCreated(draft3.timeCreated)
            val resultDraft = persistenceImpl.getDraftsBySentStatus(SentStatusEnum.LOCAL).first()
            assertThat(resultDraft).hasSize(2)
        }
    }

    @Test
    fun deleteAllDrafts() {
        runBlocking {
            persistenceImpl.insertDraft(draft1)
            persistenceImpl.insertDraft(draft2)
            persistenceImpl.deleteAllDrafts()
            val resultDraft = persistenceImpl.getDraftsBySentStatus(SentStatusEnum.LOCAL).first()
            assertThat(resultDraft).hasSize(0)
        }
    }

    @Test
    fun deleteDraft() {
        runBlocking {
            persistenceImpl.insertDraft(draft1)
            persistenceImpl.insertDraft(draft2)
            persistenceImpl.deleteDraft(draft1)
            val resultDraft = persistenceImpl.getDraftsBySentStatus(SentStatusEnum.LOCAL).first()
            assertThat(resultDraft).hasSize(1)
            assertThat(resultDraft.first()).isEqualTo(draft2)
        }
    }

    @Test
    fun updateDraft() {
        runBlocking {
            persistenceImpl.insertDraft(draft1)
            persistenceImpl.insertDraft(draft2)
            persistenceImpl.updateDraft(updatedDraft1)
            val resultDraft = persistenceImpl.getDraftByTimeCreated(draft1.timeCreated).first()
            assertThat(resultDraft).isEqualTo(updatedDraft1)
        }
    }

    @Test
    fun updateDraftContent() {
        runBlocking {
            persistenceImpl.insertDraft(draft1)
            persistenceImpl.updateDraftContent(draftContent1)
            val resultDraft = persistenceImpl.getDraftByTimeCreated(draft1.timeCreated).first()
            assertThat(resultDraft!!.content).isEqualTo(draftContent1.content)
        }
    }

    @Test
    fun updateDraftSentStatus() {
        runBlocking {
            persistenceImpl.insertDraft(draft2)
            persistenceImpl.updateDraftSentStatus(draftSentStatus2)
            val resultDraft = persistenceImpl.getDraftByTimeCreated(draft2.timeCreated).first()
            assertThat(resultDraft!!.sentStatus).isEqualTo(draftSentStatus2.sentStatus)
        }
    }

    @Test
    fun getAllTwitterUserAndTokens() {
        runBlocking {
            persistenceImpl.insertTwitterUserAndTokens(twitterUserAndTokens1)
            val resultTwitterUserAndTokens = persistenceImpl.getOneTwitterUserAndTokens().first()
            assertThat(resultTwitterUserAndTokens).isEqualTo(twitterUserAndTokens1)
        }
    }

    @Test
    fun deleteAllTwitterUserAndTokens() {
        runBlocking {
            persistenceImpl.insertTwitterUserAndTokens(twitterUserAndTokens1)
            persistenceImpl.deleteAllTwitterUserAndTokens()
            val resultTwitterUserAndTokens = persistenceImpl.getOneTwitterUserAndTokens().first()
            assertThat(resultTwitterUserAndTokens).isNull()
        }
    }

    @Test
    fun deleteTwitterUserAndTokens() {
        runBlocking {
            persistenceImpl.insertTwitterUserAndTokens(twitterUserAndTokens1)
            persistenceImpl.deleteTwitterUserAndTokens(twitterUserAndTokens1)
            val resultTwitterUserAndTokens = persistenceImpl.getOneTwitterUserAndTokens().first()
            assertThat(resultTwitterUserAndTokens).isNull()
        }
    }

    @Test
    fun insertTwitterUserAndTokens() {
        runBlocking {
            persistenceImpl.insertTwitterUserAndTokens(twitterUserAndTokens1)
            val resultTwitterUserAndTokens = persistenceImpl.getOneTwitterUserAndTokens().first()
            assertThat(resultTwitterUserAndTokens).isEqualTo(twitterUserAndTokens1)
        }
    }

    @Test
    fun updateTwitterUserAndTokens() {
        runBlocking {
            persistenceImpl.insertTwitterUserAndTokens(twitterUserAndTokens1)
            persistenceImpl.updateTwitterUserAndTokens(updatedTwitterUserAndTokens1)
            val resultTwitterUserAndTokens = persistenceImpl.getOneTwitterUserAndTokens().first()
            assertThat(resultTwitterUserAndTokens).isEqualTo(updatedTwitterUserAndTokens1)
        }
    }

    @Test
    fun updateTwitterUser() {
        runBlocking {
            persistenceImpl.insertTwitterUserAndTokens(twitterUserAndTokens1)
            persistenceImpl.updateTwitterUser(twitterUser1)
            val resultTwitterUserAndTokens = persistenceImpl.getOneTwitterUserAndTokens().first()
            assertThat(resultTwitterUserAndTokens!!.name).isEqualTo(twitterUser1.name)
        }
    }
}