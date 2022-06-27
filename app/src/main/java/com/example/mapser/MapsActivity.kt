package com.example.mapser

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.mapser.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var locationA: LatLng? = null
    private var countMarkers = 0
    private val destination = 0.002


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        initUi()
    }

    private fun initUi(){
        val btnClear = findViewById<Button>(R.id.btnClear)
        val btnA = findViewById<Button>(R.id.btnA)
        val btnB = findViewById<Button>(R.id.btnB)
        btnClear.setOnClickListener {
            mMap.clear()
            btnA.visibility = VISIBLE
            btnB.visibility = VISIBLE
            countMarkers = 0
        }
        btnA.setOnClickListener {
            createMarker(mMap, getString(R.string.a), it)
        }
        btnB.setOnClickListener {
            createMarker(mMap, getString(R.string.b),it)
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val rostov = LatLng(47.2313, 39.7233)//default location Rostov
        mMap.moveCamera(CameraUpdateFactory.newLatLng(rostov))
    }

    private fun polyline(locationB: LatLng) {
        locationA?.let { locationA ->
            createPolyline(locationA, locationB)
            createPolyline(LatLng(locationA.latitude - destination, locationA.longitude - destination),
                LatLng(locationB.latitude - destination, locationB.longitude - destination))
            createPolyline(LatLng(locationA.latitude + destination, locationA.longitude + destination),
                LatLng(locationB.latitude + destination, locationB.longitude + destination))
        }
    }

    private fun createPolyline(a:LatLng, b: LatLng) {
        val polylineOptions = PolylineOptions()
            .add(LatLng(a.latitude, a.longitude))
            .add(LatLng(b.latitude, b.longitude))
        mMap.addPolyline(polylineOptions)
    }


    private fun createMarker(mMap: GoogleMap, title: String, button: View) {
        if(countMarkers < 2) {
            countMarkers++
            mMap.setOnMapClickListener {
                when(countMarkers) {
                    1 -> locationA = it
                    2 -> polyline(it)
                }
                mMap.addMarker(MarkerOptions().position(it).title(title))
                mMap.setOnMapClickListener {  }
                button.visibility = GONE
            }
        }
    }
}