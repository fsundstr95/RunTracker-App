package com.example.runtracker2.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters


@Database(entities = [RunData::class], version = 1)
@TypeConverters(Converters::class)
abstract class RunDatabase : RoomDatabase(){

    abstract fun getRunDao(): RunDAO
}