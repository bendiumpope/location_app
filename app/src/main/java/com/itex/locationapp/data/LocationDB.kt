package com.itex.locationapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LocationData::class], version = 1)
abstract class LocationDB: RoomDatabase() {

    abstract fun LocationDao(): LocationDao

    companion object{
        private var instance:LocationDB?=null

        fun createDB(context: Context): LocationDB{
            if(instance==null){

                instance = Room.databaseBuilder(context, LocationDB::class.java, "MyLocation")
                    .allowMainThreadQueries()
                    .build()

                return instance!!
            }

            return instance!!
        }

    }

}