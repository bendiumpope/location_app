package com.itex.locationapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import com.itex.locationapp.data.LocationData
import com.itex.locationapp.data.viewmodel.LocationViewModel
import org.json.JSONObject
import java.io.IOException


class HomeActivity: AppCompatActivity(), OnMapReadyCallback{

    val PERMISSION_ID = 42

    lateinit var startBtn:Button
    lateinit var stopBtn:Button
    lateinit var distanceCovered:TextView
    lateinit var latitude:TextView
    lateinit var longitude:TextView
    lateinit var location:Location
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var modelView: LocationViewModel
    lateinit var mapFragment: SupportMapFragment
    var startLat:Double?= null
    var startLong:Double?= null
    var startLocations: List<LocationData> = arrayListOf()
    private var googleMap: GoogleMap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        startBtn = findViewById(R.id.start_btn)
        stopBtn = findViewById(R.id.stop_btn)
        distanceCovered = findViewById(R.id.distance)
        latitude = findViewById(R.id.latitude)
        longitude = findViewById(R.id.longitude)

        modelView = ViewModelProviders.of(this)[LocationViewModel::class.java]

        mapFragment = supportFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
    }

    override fun onCreateOptionsMenu(menu: Menu):Boolean {
        // Inflate the menu; this adds items to the action bar
        MenuInflater(this).inflate(R.menu.reset_menu, menu)
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.getItemId()) {

            R.id.reset -> {
                AlertDialog.Builder(this).apply{
                    setTitle("Are you sure?")
                    setMessage("You cannot undo this operation")
                    setPositiveButton("Yes"){_, _ ->
                        modelView.deleteLocationDatas(context)

                        latitude.text = "Latitude"
                        longitude.text = "Longitude"
                        distanceCovered.text=""
                        startBtn.visibility=View.VISIBLE
                        stopBtn.visibility=View.GONE

                    }
                    setNegativeButton("No"){_, _ ->

                    }

                }.create().show()

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap?){
        this.googleMap = googleMap
        val latLngOrigin = LatLng(startLocations[0].latitude,startLocations[0].longitude) // Start(Start Location)
        val latLngDestination = LatLng(startLat!!,startLong!!)
        this.googleMap!!.addMarker(MarkerOptions().position(latLngOrigin).title("Start"))
        this.googleMap!!.addMarker(MarkerOptions().position(latLngDestination).title("Stop"))
        this.googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 14.5f))

            val path: MutableList<List<LatLng>> = ArrayList()
            val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=${startLocations[0].latitude},${startLocations[0].longitude}" +
                    "&destination=${startLat},${startLong}&key=AIzaSyDQFtmjF3fkjYkQI04WqQCzcPqUbU4CmDw"

            val directionsRequest = object : StringRequest(Request.Method.GET, urlDirections, Response.Listener<String> { response ->
                val jsonResponse = JSONObject(response)
                // Get routes
                val routes = jsonResponse.getJSONArray("routes")
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                val steps = legs.getJSONObject(0).getJSONArray("steps")
                val distance =steps.getJSONObject(0).getJSONObject("distance").get("text")

                //Setting the distance Covered to the view
                findViewById<TextView>(R.id.distance).text = distance.toString()

                for (i in 0 until steps.length()) {
                    val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                    path.add(PolyUtil.decode(points))
                }
                for (i in 0 until path.size) {
                    this.googleMap!!.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
                }
            }, Response.ErrorListener {
                    _ ->
            }){}
            val requestQueue = Volley.newRequestQueue(this)
            requestQueue.add(directionsRequest)

    }

    fun OnStartClicked(view: View){

        RequestNewLocation()

        if(CheckLocationPermission()){

            if(IsLocationEnabled()){

                startBtn.visibility=View.GONE
                stopBtn.visibility=View.VISIBLE

                modelView.getLocationDatas(this).observe(this, Observer<List<LocationData>>{ locationData ->

                    locationData?.let {

                        startLocations = locationData

                    }
                })

            }
        }

    }

    fun OnStopClicked(view: View){

        if(CheckLocationPermission()){

            if(IsLocationEnabled()){

                RequestNewLocation()
                if(CheckInternetConnectivity()){

                    startBtn.visibility=View.VISIBLE
                    stopBtn.visibility=View.GONE

                    mapFragment.getMapAsync(this)
                }




            }
        }
    }

    private fun CheckInternetConnectivity(): Boolean{

        val cm =getSystemService(Context.CONNECTIVITY_SERVICE)as ConnectivityManager
        val activityNetwork = cm.activeNetworkInfo

        val isConnected =activityNetwork!=null && activityNetwork.isConnectedOrConnecting

        if(!isConnected){

            Toast.makeText(this, "Check Network Connection", Toast.LENGTH_SHORT).show()

            return false
        }
            return true
    }

    //Getting user location
    @SuppressLint("MissingPermission")
    private fun RequestNewLocation(){

        if(CheckLocationPermission()){

            if(IsLocationEnabled()){

                var locationRequest = LocationRequest()
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                locationRequest.interval = 0
                locationRequest.fastestInterval=0
                locationRequest.numUpdates = 1

                var fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationClient.requestLocationUpdates(
                    locationRequest, LocationCallBack, Looper.myLooper()
                )


            }else{

                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()

                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }else{

            RequestPermission()
        }

    }

    private val LocationCallBack = object: LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult?) {

            try{
                location = locationResult!!.lastLocation
                latitude.text = location.latitude.toString()
                longitude.text = location.longitude.toString()
            }catch(e:IOException){
                throw(e)
            }

            startLat = location.latitude
            startLong = location.longitude

            Toast.makeText(this@HomeActivity, "start: ${startLat}, stop: ${startLong}", Toast.LENGTH_LONG).show()

            val captureLocationData =
                LocationData(
                    0,
                    startLat!!,
                    startLong!!

                )

            modelView.setLocationDatas(captureLocationData, this@HomeActivity)
            Toast.makeText(this@HomeActivity, "Location Saved", Toast.LENGTH_SHORT).show()



        }
    }


    //checking if Location permission is enabled
    private fun IsLocationEnabled(): Boolean{
        var locationManager =getSystemService(Context.LOCATION_SERVICE)
                as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(

            LocationManager.NETWORK_PROVIDER
        )
    }


    //Checking for location permission
    private fun CheckLocationPermission():Boolean{
        if(PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_GRANTED &&
                PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PermissionChecker.PERMISSION_GRANTED){

            return true
        }
        return false
    }

    //Asking for Location permission

    private fun RequestPermission(){
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == PERMISSION_ID){

            if(grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED){

                RequestNewLocation()
            }
        }
    }


}
