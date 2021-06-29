package com.muchen.tweetstormmaker.androidui.di

import javax.inject.Scope

@Scope
@Retention(value = AnnotationRetention.RUNTIME)
annotation class FragmentScope

@Scope
@Retention(value = AnnotationRetention.RUNTIME)
annotation class ActivityScope

@Scope
@Retention(value = AnnotationRetention.RUNTIME)
annotation class ApplicationScope