package com.radziejewskig.todo.di.components

import android.app.Application
import com.radziejewskig.todo.App
import com.radziejewskig.todo.di.modules.*
import com.radziejewskig.todo.feature.MainActivity
import com.radziejewskig.todo.feature.addedit.AddEditFragment
import com.radziejewskig.todo.feature.list.ListFragment
import com.radziejewskig.todo.feature.selectphoto.SelectPhotoFragment
import com.radziejewskig.todo.feature.takephoto.TakePhotoFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        DataModule::class,
        FirebaseModule::class,
        CommonUiModule::class,
        BuilderModule::class,
    ]
)
interface ApplicationComponent {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance
            application: Application
        ): ApplicationComponent
    }

    //App
    fun inject(app: App)

    //Activity
    fun inject(activity: MainActivity)

    //Fragments
    fun inject(fragment: ListFragment)
    fun inject(fragment: AddEditFragment)
    fun inject(fragment: TakePhotoFragment)
    fun inject(fragment: SelectPhotoFragment)

}
