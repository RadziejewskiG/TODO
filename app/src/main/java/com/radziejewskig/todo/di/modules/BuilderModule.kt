package com.radziejewskig.todo.di.modules

import androidx.lifecycle.ViewModel
import com.radziejewskig.todo.di.AssistedSavedStateViewModelFactory
import com.radziejewskig.todo.di.ViewModelKey
import com.radziejewskig.todo.feature.MainActivityViewModel
import com.radziejewskig.todo.feature.addedit.AddEditFragmentViewModel
import com.radziejewskig.todo.feature.list.ListFragmentViewModel
import com.radziejewskig.todo.feature.selectphoto.SelectPhotoFragmentViewModel
import com.radziejewskig.todo.feature.takephoto.TakePhotoFragmentViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class BuilderModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    abstract fun bindsMainActivityViewModel(f: MainActivityViewModel.Factory): AssistedSavedStateViewModelFactory<out ViewModel>

    @Binds
    @IntoMap
    @ViewModelKey(ListFragmentViewModel::class)
    abstract fun bindsListFragmentViewModel(f: ListFragmentViewModel.Factory): AssistedSavedStateViewModelFactory<out ViewModel>

    @Binds
    @IntoMap
    @ViewModelKey(AddEditFragmentViewModel::class)
    abstract fun bindsAddEditFragmentViewModel(f: AddEditFragmentViewModel.Factory): AssistedSavedStateViewModelFactory<out ViewModel>

    @Binds
    @IntoMap
    @ViewModelKey(TakePhotoFragmentViewModel::class)
    abstract fun bindsTakePhotoFragmentViewModel(f: TakePhotoFragmentViewModel.Factory): AssistedSavedStateViewModelFactory<out ViewModel>

    @Binds
    @IntoMap
    @ViewModelKey(SelectPhotoFragmentViewModel::class)
    abstract fun bindsSelectPhotoFragmentViewModel(f: SelectPhotoFragmentViewModel.Factory): AssistedSavedStateViewModelFactory<out ViewModel>

}