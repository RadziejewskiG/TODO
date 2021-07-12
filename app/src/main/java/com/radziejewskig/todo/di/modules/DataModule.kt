package com.radziejewskig.todo.di.modules

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.radziejewskig.todo.data.StorageRepository
import com.radziejewskig.todo.data.TaskRepository
import com.radziejewskig.todo.data.source.network.StorageRepositoryImpl
import com.radziejewskig.todo.data.source.network.TaskRepositoryImpl
import com.radziejewskig.todo.utils.FileUtils
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataModule {

    @Provides
    @Singleton
    fun providesTaskRepository(firestore: FirebaseFirestore): TaskRepository = TaskRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun providesStorageRepository(storage: FirebaseStorage, fileUtils: FileUtils): StorageRepository = StorageRepositoryImpl(storage, fileUtils)

}
