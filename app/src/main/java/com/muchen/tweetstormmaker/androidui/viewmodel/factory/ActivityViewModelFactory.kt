package com.muchen.tweetstormmaker.androidui.viewmodel.factory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Provider

class ActivityViewModelFactory (private val activityViewModelProvider: Map<Class<out ViewModel>,
        @JvmSuppressWildcards Provider<ViewModel>>): ViewModelProvider.Factory {

    @Suppress("unchecked")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        Log.d(this::class.simpleName, "${activityViewModelProvider.keys}")
        return activityViewModelProvider[modelClass]!!.get() as T
    }
}