package com.itex.locationapp.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface LocationDao {

    @Insert
    fun insertLocation(LocationData: LocationData)

    @Update
    fun updateLocation(LocationData: LocationData)

    @Query("SELECT * FROM locationTable")
    fun fetchLocationData(): LiveData<List<LocationData>>

    @Query("DELETE FROM locationTable")
    fun deleteAll()
}