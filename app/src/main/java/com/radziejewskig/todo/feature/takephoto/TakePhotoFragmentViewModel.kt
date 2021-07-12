package com.radziejewskig.todo.feature.takephoto

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import com.radziejewskig.todo.base.FragmentViewModel
import com.radziejewskig.todo.data.StorageRepository
import com.radziejewskig.todo.di.AssistedSavedStateViewModelFactory
import com.radziejewskig.todo.extension.withMain
import com.radziejewskig.todo.utils.ParcelableString
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class TakePhotoFragmentViewModel @AssistedInject constructor(
    private val storageRepository: StorageRepository,
    @Assisted private val handle: SavedStateHandle
): FragmentViewModel<TakePhotoState>(handle) {

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
                    withMain {
                        TakePhotoEvent.IMAGE_SAVED.emit(ParcelableString(result))
                    }
                }
            }
        }
    }

}
