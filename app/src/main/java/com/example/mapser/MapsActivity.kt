package com.example.mapser

import android.location.Location
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
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnCameraMoveListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var locationA: LatLng = LatLng(0.0, 0.0)
    private var locationB: LatLng = LatLng(0.0, 0.0)
    private var countMarkers = 0
    private val destination = 0.002
    private var southWest: LatLng = LatLng(0.0, 0.0) //screenBounds
    private var northEast: LatLng = LatLng(0.0, 0.0) //screenBounds


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        initUi()
    }

    private fun initUi() {
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
            createMarker(mMap, getString(R.string.b), it)
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val rostov = LatLng(47.2313, 39.7233)//default location Rostov
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rostov, 18f))
        mMap.setOnCameraMoveListener(this)
        setScreenBounds()
    }

    private fun setLocations(locationB: LatLng) {
        locationA.let { locationA ->
            createLine(locationA, locationB)
            createLine(
                LatLng(locationA.latitude - destination, locationA.longitude - destination),
                LatLng(locationB.latitude - destination, locationB.longitude - destination)
            )
            createLine(
                LatLng(locationA.latitude + destination, locationA.longitude + destination),
                LatLng(locationB.latitude + destination, locationB.longitude + destination)
            )
        }
    }



    private fun createLine(a: LatLng, b: LatLng) {
        val pointA = LatLng(a.latitude, a.longitude)
        val pointB = LatLng(b.latitude, b.longitude)

        val polylineOptions = PolylineOptions()
        val pointsList = generateNeedingPoints(pointA, pointB)
        pointsList.forEach {
            polylineOptions.add(it)
        }
        mMap.addPolyline(polylineOptions)
    }



    private fun setScreenBounds() {
        val bounds = mMap.projection.visibleRegion.latLngBounds
        southWest = bounds.southwest
        northEast = bounds.northeast
    }

    private fun generateNeedingPoints(a: LatLng, b: LatLng): List<LatLng> {
        setScreenBounds()
        val c: LatLng
        val d: LatLng
        val longitudeC = if (a.longitude < b.longitude) northEast.longitude else southWest.longitude
        val longitudeD = if (a.longitude < b.longitude) southWest.longitude else northEast.longitude
        c = calculateEdgeButton(a, b, longitudeC)
        d = calculateEdgeButton(b, a, longitudeD)
        return mutableListOf(d, a, b, c)
    }


    private fun calculateEdgeButton(a: LatLng, b: LatLng, edgeLng: Double, distortionCoefficient:Double = 1.0): LatLng{
        val longChange = b.longitude - a.longitude
        val latChange = b.latitude - a.latitude
        val slope = latChange / longChange
        val longChangePointC = edgeLng - b.longitude
        val latChangePointC = longChangePointC * slope
        val latitudeC = b.latitude + latChangePointC
        return LatLng(latitudeC * distortionCoefficient, edgeLng)
    }

    private fun createMarker(mMap: GoogleMap, title: String, button: View) {
        if (countMarkers < 2) {
            countMarkers++
            mMap.setOnMapClickListener { latLng ->
                when (countMarkers) {
                    1 -> locationA = latLng
                    2 -> setLocations(latLng).also { locationB = latLng }
                }
                mMap.addMarker(MarkerOptions().position(latLng).title(title))
                mMap.setOnMapClickListener { }
                button.visibility = GONE
            }
        }
    }


    override fun onCameraMove() {
        setScreenBounds()
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(locationA))
        mMap.addMarker(MarkerOptions().position(locationB))
        setLocations(locationB)
    }
}