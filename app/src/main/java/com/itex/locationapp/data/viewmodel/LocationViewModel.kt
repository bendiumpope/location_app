package com.itex.locationapp.data.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.itex.locationapp.data.LocationDB
import com.itex.locationapp.data.LocationData

class LocationViewModel: ViewModel() {

    private val location: MutableLiveData<List<LocationData>> by lazy {
        MutableLiveData<List<LocationData>>().also{
            loadLocationDatas()
        }
    }

    fun getLocationDatas(context: Context): LiveData<List<LocationData>> {
        return LocationDB.createDB(context).LocationDao().fetchLocationData()
    }

    fun setLocationDatas(LocationData: LocationData, context: Context){

        return LocationDB.createDB(context).LocationDao().insertLocation(LocationData)
    }

    fun updateLocationData(LocationData: LocationData, context: Context){

        return LocationDB.createDB(context).LocationDao().updateLocation(LocationData)
    }

    fun deleteLocationDatas(context: Context){
        return LocationDB.createDB(context).LocationDao().deleteAll()
    }

    private fun loadLocationDatas(){

    }
}