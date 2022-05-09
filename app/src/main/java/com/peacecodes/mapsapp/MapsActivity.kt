package com.peacecodes.mapsapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.peacecodes.mapsapp.databinding.ActivityMapsBinding
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val TAG = MapsActivity::class.java.simpleName
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = LocationListener {
            location -> map.clear()
            val myLocation = LatLng(location.latitude, location.longitude)
            map.addMarker(MarkerOptions()
                .position(myLocation)
                .title(getString(R.string.my_current_location)))
            map.moveCamera(CameraUpdateFactory.newLatLng(myLocation))
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED){
            loadCurrentLocation()
        }
        else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
        mapLongClick(map)
        poiClick(map)
        mapStyle(map)
    }
    @SuppressLint("MissingPermission")
    private fun loadCurrentLocation(){
        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,0.0f, locationListener
        )
        val previousLocation = locationManager.getLastKnownLocation(
            LocationManager.GPS_PROVIDER
        ) ?: return
        val myLocation = LatLng(previousLocation.latitude, previousLocation.longitude)
        map.addMarker(MarkerOptions()
            .position(myLocation)
            .title(getString(R.string.my_current_location)))
        map.moveCamera(CameraUpdateFactory.newLatLng(myLocation))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0]
                == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED
                    ){
                        loadCurrentLocation()
                    }
                    else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            REQUEST_LOCATION_PERMISSION
                        )
                    }
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }

    fun mapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this
                , R.raw.map_style)
            )
            if (!success){
                Log.e(TAG, "mapStyle: Unsuccessful")
            } 
        }catch (e: Resources.NotFoundException){
            Log.e(TAG, "mapStyle: Style not found", e)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_types, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.terrain -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        R.id.satellite -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.hybrid -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    fun mapLongClick(map:GoogleMap){
        map.setOnMapLongClickListener {
            map.addMarker(MarkerOptions()
                .position(it)
                .title(getString(R.string.marker))
                .snippet("${it.latitude}, ${it.longitude}")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )
        }
    }
    fun poiClick(map: GoogleMap) {
        map.setOnPoiClickListener {
            poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
        }

    }
}