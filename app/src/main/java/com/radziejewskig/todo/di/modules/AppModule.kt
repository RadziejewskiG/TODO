package com.radziejewskig.todo.di.modules

import android.app.Application
import android.content.Context
import com.radziejewskig.todo.utils.FileUtils
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun providesApplicationContext(application: Application): Context = application

    @Provides
    @Singleton
    fun providesFileUtils(context: Context): FileUtils {
        return FileUtils(context)
    }

}
