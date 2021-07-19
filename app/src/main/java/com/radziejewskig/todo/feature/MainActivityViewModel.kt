package com.radziejewskig.todo.feature

import androidx.lifecycle.SavedStateHandle
import com.radziejewskig.todo.base.ActivityViewModel
import com.radziejewskig.todo.base.CommonState
import com.radziejewskig.todo.di.AssistedSavedStateViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivityViewModel @AssistedInject constructor (
    @Assisted private val handle: SavedStateHandle
): ActivityViewModel<CommonState>(handle) {

    override fun setupInitialState() = CommonState()

    @AssistedFactory
    interface Factory: AssistedSavedStateViewModelFactory<MainActivityViewModel>

    val navigationAndStatusBarHeight = MutableStateFlow(Pair(0, 0))
}
