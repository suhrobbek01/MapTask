package com.example.myapplication.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Camera
import android.location.Geocoder
import android.location.Location
import android.location.LocationRequest
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentMapBinding
import com.example.myapplication.repository.LocationService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentMapBinding
    private lateinit var map: GoogleMap
    private lateinit var geocoder: Geocoder
    private var ACCESS_LOCATION_REQUEST_CODE = 10001
    private lateinit var locationRequest: com.google.android.gms.location.LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mapFragment: SupportMapFragment
    private var marker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater)

        binding.apply {
            zoomIn.setOnClickListener {
                map.animateCamera(CameraUpdateFactory.zoomIn())
            }
            zoomOn.setOnClickListener {
                map.animateCamera(CameraUpdateFactory.zoomOut())
            }
            myLocation.setOnClickListener {
                getCurrentLocation()
            }
        }
        locationRequest = com.google.android.gms.location.LocationRequest.create()
        locationRequest.interval = 500
        locationRequest.fastestInterval = 500
        locationRequest.priority =
            com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY


        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

//        getLocationAccess()
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) { /* ... */
//                    getCurrentLocation()
                    startLocationUpdates()
                    Intent(requireContext(), LocationService::class.java).apply {
                        action = LocationService.ACTION_START
                        requireActivity().startService(this)
                    }
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) { /* ... */
                    val alertDialog = AlertDialog.Builder(requireContext())
                    alertDialog.setTitle("GPS Settings")
                    alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?")
                    alertDialog.setPositiveButton(
                        "Yes"
                    ) { p0, p1 ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", requireActivity().packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }

                    alertDialog.setNegativeButton(
                        "No"
                    ) { dialog, p1 ->
                        dialog?.cancel()
                        getLocationAccess()
                    }
                    alertDialog.show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest, token: PermissionToken
                ) {
                    token.continuePermissionRequest()/* ... */
                }
            }).check()
    }

    var locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            if (map != null) {
                p0.lastLocation?.let { setUserLocationMarker(it) }
            }
        }
    }

    private fun setUserLocationMarker(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)

        if (marker == null) {
            var markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car2))
            markerOptions.rotation(location.bearing)
            markerOptions.anchor(0.5f, 0.5f)
            marker = map.addMarker(markerOptions)!!
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, map.cameraPosition.zoom))
        } else {
            marker?.position = (latLng)
            marker?.rotation = location.bearing
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, map.cameraPosition.zoom))
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    fun getLocationAccess() {

    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location ->
            mapFragment.getMapAsync { googleMap ->
                googleMap.clear()

                val latlng = location?.let { LatLng(it.latitude, location.longitude) }
                var markerOptions = MarkerOptions()
                markerOptions.position(latlng!!)
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car2))
                markerOptions.rotation(location.bearing)
                markerOptions.anchor(0.5f, 0.5f)
                marker = map.addMarker(markerOptions)!!
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, map.cameraPosition.zoom))

            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isCompassEnabled = true
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
    }
}