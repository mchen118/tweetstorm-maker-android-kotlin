package com.muchen.tweetstormmaker.androidui.view.listfragment

import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.viewbinding.ViewBinding
import com.muchen.tweetstormandroid.R
import com.muchen.tweetstormandroid.databinding.FragmentListLocalBinding
import com.muchen.tweetstormandroid.databinding.FragmentListPartiallySentBinding
import com.muchen.tweetstormandroid.databinding.FragmentListSentBinding
import com.muchen.tweetstormmaker.androidui.adatper.DraftListAdapter
import com.muchen.tweetstormmaker.androidui.di.DaggerListFragmentComponent
import com.muchen.tweetstormmaker.androidui.model.Draft
import com.muchen.tweetstormmaker.androidui.view.MainActivity
import javax.inject.Inject

// BaseListFragment shows search action in tool bar and provides injection method and injected members.
abstract class BaseListFragment : Fragment() {

    // has to be overridden by lazy initialization, because initialization depends on DraftsViewModel being injected in onCreateView()
    protected abstract val listLiveData: Lazy<LiveData<List<Draft>>>

    protected abstract val navigateToEditFragment: (NavController, Long) -> Unit

    @Inject
    lateinit var adapter: DraftListAdapter

    protected lateinit var binding: ViewBinding

    protected val draftsViewModel by lazy { (requireActivity() as MainActivity).draftsViewModel }

    private val queryTextListener = object: SearchView.OnQueryTextListener{
        override fun onQueryTextSubmit(query: String?): Boolean {
            val result = if (query.isNullOrBlank()) {
                listLiveData.value.value as List<Draft>
            } else {
                val filter = ArrayList<Draft>()
                for (draft in listLiveData.value.value as List<Draft>) {
                    if (draft.content.contains(query)) filter.add(draft)
                }
                filter
            }
            adapter.submitList(result)
            when (binding) {
                is FragmentListLocalBinding -> {
                    val specificBinding = binding as FragmentListLocalBinding
                    if (result.isEmpty()) {
                        specificBinding.rvDraftList.visibility = View.INVISIBLE
                        specificBinding.textViewEmptyList.visibility = View.VISIBLE
                    } else {
                        specificBinding.rvDraftList.visibility = View.VISIBLE
                        specificBinding.textViewEmptyList.visibility = View.INVISIBLE
                    }
                }
                is FragmentListPartiallySentBinding -> {
                    val specificBinding = binding as FragmentListPartiallySentBinding
                    if (result.isEmpty()) {
                        specificBinding.rvDraftList.visibility = View.INVISIBLE
                        specificBinding.textViewEmptyList.visibility = View.VISIBLE
                    } else {
                        specificBinding.rvDraftList.visibility = View.VISIBLE
                        specificBinding.textViewEmptyList.visibility = View.INVISIBLE
                    }
                }
                is FragmentListSentBinding -> {
                    val specificBinding = binding as FragmentListSentBinding
                    if (result.isEmpty()) {
                        specificBinding.rvDraftList.visibility = View.INVISIBLE
                        specificBinding.textViewEmptyList.visibility = View.VISIBLE
                    } else {
                        specificBinding.rvDraftList.visibility = View.VISIBLE
                        specificBinding.textViewEmptyList.visibility = View.INVISIBLE
                    }
                }
            }
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            onQueryTextSubmit(newText)
            return true
        }
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.menu_item_search).apply{
            isEnabled = true
            isVisible = true
        }
        (menu.findItem(R.id.menu_item_search)?.actionView as SearchView).setOnQueryTextListener(queryTextListener)
    }

    // injection method to be called in concrete subclasses
    protected fun performInjection() {
        DaggerListFragmentComponent.builder()
                .navigationToEditFragment(navigateToEditFragment)
                .build()
                .inject(this)
    }
}