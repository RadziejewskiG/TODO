package com.radziejewskig.todo.feature.selectphoto

import androidx.lifecycle.SavedStateHandle
import com.radziejewskig.todo.base.CommonEvent
import com.radziejewskig.todo.base.CommonState
import com.radziejewskig.todo.base.FragmentViewModel
import com.radziejewskig.todo.data.StorageRepository
import com.radziejewskig.todo.di.AssistedSavedStateViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@ExperimentalCoroutinesApi
class SelectPhotoFragmentViewModel @AssistedInject constructor(
    private val storageRepository: StorageRepository,
    @Assisted private val handle: SavedStateHandle
): FragmentViewModel<CommonState, CommonEvent>(handle) {

    @AssistedFactory
    interface Factory: AssistedSavedStateViewModelFactory<SelectPhotoFragmentViewModel>

    override fun setupInitialState() = CommonState()

    private val _photos = MutableSharedFlow<List<String>>(replay = 1)
    val photos = _photos.asSharedFlow()

    override fun initialAct() {
        loadImages()
    }

    private fun loadImages() {
        launchLoading {
            val imagesUrls = storageRepository.loadImages()
            _photos.emit(imagesUrls)
        }
    }

}
