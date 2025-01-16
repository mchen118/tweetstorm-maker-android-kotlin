package com.muchen.tweetstormmaker

import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.muchen.tweetstormandroid.R
import androidx.navigation.ui.R.string.nav_app_bar_open_drawer_description
import androidx.navigation.ui.R.string.nav_app_bar_navigate_up_description
import com.muchen.tweetstormmaker.androidui.AndroidUIConstants.DEFAULT_NUMBERING_TWEETS_VALUE
import com.muchen.tweetstormmaker.androidui.adatper.DraftListAdapter
import com.muchen.tweetstormmaker.interfaceadapter.model.Draft
import com.muchen.tweetstormmaker.interfaceadapter.model.SentStatusEnum
import com.muchen.tweetstormmaker.androidui.view.MainActivity
import com.muchen.tweetstormmaker.androidui.view.listfragment.LocalListFragment
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class UITestOnMainActivityLocalFunctionality {

    private val testText1 = "1"
    private val testText2 = "2"
    private val testText3 = "3"
    private val testText4 = "4"
    private val emptyText = ""
    private val localDraft1 = Draft(1L, testText1)
    private val draft2 = Draft(2L, testText3, SentStatusEnum.FULLY_SENT, emptyText)
    private val draft3 = Draft(3L, testText4, SentStatusEnum.FULLY_SENT, emptyText)

    private val btnCompose = onView(withId(R.id.btn_compose))
    private val btnDiscardLocal = onView(withId(R.id.btn_discard_local))
    private val btnDiscardNonLocal = onView(withId(R.id.btn_discard_non_local))
    private val btnDiscardAllSent = onView(withId(R.id.btn_discard_all_sent_drafts))
    private val editTextDraft = onView(withId(R.id.edit_text_draft))
    private val iconSearch = onView(withId(R.id.menu_item_search))
    private val iconDrawerMenu = onView(withContentDescription(nav_app_bar_open_drawer_description))
    private val iconNavigateUp = onView(withContentDescription(nav_app_bar_navigate_up_description))
    private val itemLocal = onView(withText(R.string.drawer_item_title_local_drafts))
    private val itemPartiallySent = onView(withText(R.string.drawer_item_title_partially_sent_tweetstorms))
    private val itemSent = onView(withText(R.string.drawer_item_title_sent_tweetstorms))
    private val itemMainSettings = onView(withText(R.string.drawer_item_title_main_settings))
    private val recyclerViewDraftList = onView(withId(R.id.rv_draft_list))
    // The id of the recycler view in a PreferenceFragmentCompat is androidx.preference.R.id.recycler_view.
    private val recyclerViewMainSettings = onView(withId(androidx.preference.R.id.recycler_view))
    // The id of the EditText of a SearchView is com.google.android.material.R.id.search_src_text.
    private val searchAutoComplete = onView(withId(com.google.android.material.R.id.search_src_text))
    private val textViewEmptyList = onView(withId(R.id.text_view_empty_list))
    private val viewRoot = onView(isRoot())

    private lateinit var localListFragment: LocalListFragment

    @get:Rule
    var activityScenarioRule = activityScenarioRule<MainActivity>()

    @Before
    fun clearDbAndAddDraft(){
        activityScenarioRule.scenario.onActivity {
            localListFragment = it.supportFragmentManager.fragments.last()
                    .childFragmentManager.fragments.last() as LocalListFragment
            localListFragment.deleteAllDrafts()
        }
        localListFragment.insertDraft(localDraft1)
        sleep(750)
    }

//    @Test
//    fun When_drawer_icon_is_clicked_Then_nav_drawer_is_displayed_in_default_look() {
//        iconDrawerMenu.perform(click())
//
//        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
//        itemLocal.check(matches(isChecked()))
//        itemPartiallySent.check(matches(isNotChecked()))
//        itemSent.check(matches(isNotChecked()))
//    }

    @Test
    fun When_search_terms_are_enterred_Then_the_appropriate_drafts_are_dispalyed() {
        iconSearch.perform(click())
        searchAutoComplete.perform(replaceText("1"))
        sleep(500)

        recyclerViewDraftList.check(matches(hasDescendant(withText(testText1))))
        recyclerViewDraftList.check(matches(not(hasDescendant(withText(testText2)))))

        searchAutoComplete.perform(replaceText("a"))
        sleep(500)

        recyclerViewDraftList.check(matches(hasChildCount(0)))
        textViewEmptyList.check(matches(isDisplayed()))
    }

    @Test
    fun When_COMPOSE_button_is_clicked_in_local_list_Then_new_draft_is_created() {
        btnCompose.perform(click())
        editTextDraft.perform(replaceText(testText1))
        viewRoot.perform(pressBack())
        sleep(500)

        recyclerViewDraftList.check(matches(hasChildCount(2)))
    }

    @Test
    fun When_navigate_up_icon_is_clicked_in_local_edit_and_draft_is_not_empty_Then_draft_is_kept() {
        recyclerViewDraftList.perform(actionOnItem<DraftListAdapter.DraftViewHolder>(
                hasDescendant(withText(testText1)), click()
            )
        )
        editTextDraft.perform(replaceText(testText2))
        sleep(500)
        iconNavigateUp.perform(click())

        recyclerViewDraftList.check(matches((hasDescendant(withText(testText2)))))
        textViewEmptyList.check(matches(not(isDisplayed())))
    }

    @Test
    fun When_navigate_up_icon_is_clicked_in_local_edit_and_draft_is_empty_Then_draft_is_deleted() {
        recyclerViewDraftList.perform(actionOnItem<DraftListAdapter.DraftViewHolder>(
                hasDescendant(withText(testText1)), click()
            )
        )
        editTextDraft.perform(replaceText(emptyText))
        iconNavigateUp.perform(click())

        sleep(500)
        recyclerViewDraftList.check(matches(not(hasDescendant(withText(testText1)))))
        textViewEmptyList.check(matches(isDisplayed()))
    }

    @Test
    fun When_DISCARD_DRAFT_button_is_clicked_in_local_edit_Then_draft_is_deleted() {
        recyclerViewDraftList.perform(actionOnItem<DraftListAdapter.DraftViewHolder>(
                        hasDescendant(withText(testText1)), click()
                )
        )
        editTextDraft.perform(replaceText(testText2))
        btnDiscardLocal.perform(click())
        sleep(500)

        recyclerViewDraftList.check(matches(not(hasDescendant(withText(testText1)))))
        textViewEmptyList.check(matches(isDisplayed()))
    }

    @Test
    fun When_recycler_view_item_is_swiped_left_Then_that_item_is_deleted() {
        recyclerViewDraftList.perform(actionOnItem<DraftListAdapter.DraftViewHolder>(
                        hasDescendant(withText(testText1)), swipeLeft()
                )
        )
        sleep(500)

        recyclerViewDraftList.check(matches(not(hasDescendant(withText(testText1)))))
        textViewEmptyList.check(matches(isDisplayed()))
    }

    @Test
    fun When_DISCARD_ALL_LOCAL_COPIES_button_is_clicked_in_sent_list_Then_all_sent_drafts_are_deleted() {
        localListFragment.insertDraft(draft2)
        localListFragment.insertDraft(draft3)
        sleep(500)

        iconDrawerMenu.perform(click())
        itemSent.perform(click())
        btnDiscardAllSent.perform(click())
        sleep(500)

        recyclerViewDraftList.check(matches(hasChildCount(0)))
        textViewEmptyList.check(matches(isDisplayed()))
    }

    @Test
    fun When_DISCARD_LOCAL_COPY_button_is_clicked_in_non_local_edit_Then_draft_is_deleted() {
        localListFragment.insertDraft(draft2)
        sleep(500)

        iconDrawerMenu.perform(click())
        itemSent.perform(click())
        
        recyclerViewDraftList.perform(actionOnItem<DraftListAdapter.DraftViewHolder>(
                    hasDescendant(withText(draft2.content)), click()
                )
        )
        
        btnDiscardNonLocal.perform((click()))

        recyclerViewDraftList.check(matches(not(hasDescendant(withText(draft2.content)))))
        textViewEmptyList.check(matches(isDisplayed()))
    }

    @Test
    fun When_numbering_tweets_toggle_is_clicked_Then_numbering_tweets_preference_changes() {
        iconDrawerMenu.perform(click())
        itemMainSettings.perform(click())

        var numberingTweetsBefore: Boolean? = null
        activityScenarioRule.scenario.onActivity {
            numberingTweetsBefore = PreferenceManager.getDefaultSharedPreferences(it)
                      .getBoolean(it.getString(R.string.preference_key_numbering_tweets), DEFAULT_NUMBERING_TWEETS_VALUE)
        }

        recyclerViewMainSettings.perform(actionOnItem<RecyclerView.ViewHolder>(
                        hasDescendant(withText(R.string.preference_title_numbering_tweets)), click()
                )
        )

        var numberingTweetsAfter: Boolean? = null
        activityScenarioRule.scenario.onActivity {
            numberingTweetsAfter = PreferenceManager.getDefaultSharedPreferences(it)
                .getBoolean(it.getString(R.string.preference_key_numbering_tweets), DEFAULT_NUMBERING_TWEETS_VALUE)
        }

        assertThat(numberingTweetsBefore != null &&
                numberingTweetsAfter != null &&
                numberingTweetsBefore != numberingTweetsAfter).isTrue()
    }
}