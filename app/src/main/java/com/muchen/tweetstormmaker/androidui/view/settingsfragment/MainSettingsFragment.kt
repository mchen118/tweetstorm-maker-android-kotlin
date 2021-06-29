package com.muchen.tweetstormmaker.androidui.view.settingsfragment

import android.os.Bundle
import android.view.Menu
import androidx.preference.PreferenceFragmentCompat
import com.muchen.tweetstormmaker.R

class MainSettingsFragment: PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
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
}