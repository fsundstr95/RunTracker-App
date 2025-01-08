package com.example.runtracker2.dependencyInjection

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.runtracker2.model.Model
import com.example.runtracker2.model.RepositoryModel
import com.example.runtracker2.model.database.RunDAO
import com.example.runtracker2.model.database.RunDatabase
import com.example.runtracker2.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runtracker2.other.Constants.KEY_NAME
import com.example.runtracker2.other.Constants.KEY_WEIGHT
import com.example.runtracker2.other.Constants.RUN_DATABASE_NAME
import com.example.runtracker2.other.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRunDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(app,RunDatabase::class.java,RUN_DATABASE_NAME).build()

    @Provides
    @Singleton
    fun provideRunDao(db : RunDatabase) = db.getRunDao()

/*
    @Provides
    @Singleton
    fun provideModel(
        @ApplicationContext context: Context
    ) = Model(context)
*/

    @Provides
    @Singleton
    fun provideModel(
        @ApplicationContext context: Context,
        repositoryModel: RepositoryModel,
        sharedPref: SharedPreferences
    ): Model = Model(context, repositoryModel, sharedPref)

    @Provides
    @Singleton
    fun provideRepositoryModel(runDAO: RunDAO): RepositoryModel = RepositoryModel(runDAO)


    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) : SharedPreferences =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME,MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPref: SharedPreferences) = sharedPref.getString(KEY_NAME,"")?:""

    @Singleton
    @Provides
    fun provideWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(KEY_WEIGHT,80f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPref: SharedPreferences) = sharedPref.getBoolean(
        KEY_FIRST_TIME_TOGGLE,true)


}