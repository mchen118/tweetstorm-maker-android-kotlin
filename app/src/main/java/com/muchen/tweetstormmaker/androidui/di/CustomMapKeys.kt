package com.muchen.tweetstormmaker.androidui.di

import androidx.lifecycle.ViewModel
import dagger.MapKey
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)
/*
 Directly using @ClassKey would produce compiler error:
 [Dagger/MissingBinding] java.util.Map<java.lang.Class<? extends androidx.lifecycle.ViewModel>,javax.inject.Provider<androidx.lifecycle.ViewModel>>
 cannot be provided without an @Provides-annotated method

 The reason is that @ClassKey provides map keys of type Class<?>, not Class<T extends ViewModel>.
 */