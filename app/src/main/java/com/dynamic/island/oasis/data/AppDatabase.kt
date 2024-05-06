package com.dynamic.island.oasis.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dynamic.island.oasis.data.dao.AppsDao
import com.dynamic.island.oasis.data.models.MyApp

@Database(
    entities = [MyApp::class],
    version = 8
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appsDao(): AppsDao
}