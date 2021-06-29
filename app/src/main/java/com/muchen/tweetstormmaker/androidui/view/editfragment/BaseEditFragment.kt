package com.muchen.tweetstormmaker.androidui.view.editfragment

import android.view.Menu
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.muchen.tweetstormmaker.R
import com.muchen.tweetstormmaker.androidui.view.MainActivity

abstract class BaseEditFragment : Fragment() {

    // has to be overridden by lazy initialization, because nav args are not available at instance creation
    protected abstract val timeCreated: Lazy<Long>

    protected val draftsViewModel by lazy {
        (requireActivity() as MainActivity).draftsViewModel
    }

    protected val twitterApiViewModel by lazy {
        (requireActivity() as MainActivity).twitterApiViewModel
    }

    protected val hasInternetAccess by lazy {
        (requireActivity() as MainActivity).hasInternetAccess
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.menu_item_search).apply {
            isEnabled = false
            isVisible = false
        }
    }

    protected fun discard() {
        draftsViewModel.deleteDraftByTimeCreated(timeCreated.value)
        findNavController().popBackStack()
    }
}