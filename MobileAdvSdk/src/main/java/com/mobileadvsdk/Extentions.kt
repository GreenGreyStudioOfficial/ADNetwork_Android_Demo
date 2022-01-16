package com.mobileadvsdk

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.kodein.di.Kodein.Builder
import org.kodein.di.KodeinAware
import org.kodein.di.direct
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance

inline fun <reified VM : ViewModel, T> T.viewModelInstance(): Lazy<VM> where T : KodeinAware, T : FragmentActivity =
    lazy { ViewModelProvider(this, direct.instance())[VM::class.java] }

inline fun <reified T : ViewModel> Builder.bindViewModel(overrides: Boolean? = null): Builder.TypeBinder<T> =
    bind<T>(T::class.java.simpleName, overrides)