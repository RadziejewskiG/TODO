package com.radziejewskig.todo

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import com.radziejewskig.todo.di.components.ApplicationComponent
import com.radziejewskig.todo.di.components.DaggerApplicationComponent

class App: Application() {

    val appComponent: ApplicationComponent by lazy {
        DaggerApplicationComponent
            .factory()
            .create(this)
    }

    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)
    }

}

val Activity.appComponent get() = (application as App).appComponent
val Fragment.appComponent get() = (requireActivity().application as App).appComponent
