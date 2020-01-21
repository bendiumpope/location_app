package com.itex.locationapp.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "locationTable")
@Parcelize
data class LocationData(
    @PrimaryKey(autoGenerate = true)
    var id: Int =0,

    @ColumnInfo(name = "latitude") var latitude:Double,

    @ColumnInfo(name="longitude") var longitude:Double

) : Parcelable