package com.radziejewskig.todo.feature.takephoto

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import com.radziejewskig.todo.base.FragmentViewModel
import com.radziejewskig.todo.data.StorageRepository
import com.radziejewskig.todo.di.AssistedSavedStateViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class TakePhotoFragmentViewModel @AssistedInject constructor(
    private val storageRepository: StorageRepository,
    @Assisted private val handle: SavedStateHandle
): FragmentViewModel<TakePhotoState, TakePhotoEvent>(handle) {

    @AssistedFactory
    interface Factory: AssistedSavedStateViewModelFactory<TakePhotoFragmentViewModel>

    fun assignNewPhotoPath(photoPath: String) {
        state.mutate { currentPhotoPath = photoPath }
    }

    fun saveImage(bitmap: Bitmap?) {
        bitmap?.let {
            launchLoading {
                val result = storageRepository.uploadImage(bitmap)
                result?.let {
                    TakePhotoEvent.ImageSaved(it).emit()
                }
            }
        }
    }

}
