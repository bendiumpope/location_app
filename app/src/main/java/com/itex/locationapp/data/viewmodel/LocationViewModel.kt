package com.itex.locationapp.data.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.itex.locationapp.data.LocationDB
import com.itex.locationapp.data.LocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class LocationViewModel: ViewModel() {


    suspend fun getLocationDatas(context: Context): LiveData<List<LocationData>> {

        return withContext(Dispatchers.IO) {
            LocationDB.createDB(context).LocationDao().fetchLocationData()
        }

    }

    suspend fun setLocationDatas(LocationData: LocationData, context: Context){

        return withContext(Dispatchers.IO){

            LocationDB.createDB(context).LocationDao().insertLocation(LocationData)
        }
    }

    fun deleteLocationDatas(context: Context){
        return LocationDB.createDB(context).LocationDao().deleteAll()
    }

}