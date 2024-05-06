package com.dynamic.island.oasis.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dynamic.island.oasis.data.models.MyApp

@Dao
interface AppsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: MyApp)

    @Query("SELECT * FROM my_app WHERE packageName == :packageName")
    suspend fun load(packageName:String): MyApp?



    @Query("SELECT * FROM my_app ")
    suspend fun loadAll():List<MyApp>

}