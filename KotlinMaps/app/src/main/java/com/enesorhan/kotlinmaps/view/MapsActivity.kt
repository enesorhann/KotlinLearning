package com.enesorhan.kotlinmaps.view

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.room.Room
import com.enesorhan.kotlinmaps.R
import com.enesorhan.kotlinmaps.databinding.ActivityMapsBinding
import com.enesorhan.kotlinmaps.model.Place
import com.enesorhan.kotlinmaps.roomdb.PlaceDao
import com.enesorhan.kotlinmaps.roomdb.PlaceDatabase
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var sharedPreferences : SharedPreferences
    private var trackBoolean : Boolean? = null
    private var latitude : Double? = null
    private var longitude : Double? = null
    private lateinit var db : PlaceDatabase
    private lateinit var placeDao : PlaceDao
    val compositeDisposable = CompositeDisposable()
    private var selectedItem : Place? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        register_launcher()
        sharedPreferences = this.getSharedPreferences("com.enesorhan.kotlinmaps", MODE_PRIVATE)
        trackBoolean = false
        latitude = 0.0
        longitude = 0.0

        db = Room.databaseBuilder(
            applicationContext,
            PlaceDatabase::class.java, "Place"
        ).build()
        placeDao = db.placeDao()

        binding.button.isEnabled = false
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)

        val intent = intent
        val info = intent.getStringExtra("info")

        if(info.equals("new")){

            binding.button2.visibility = View.GONE
            binding.button.visibility = View.VISIBLE

            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

            locationListener = object : LocationListener{
                override fun onLocationChanged(location: Location) {

                    trackBoolean = sharedPreferences.getBoolean("trackBoolean",false)

                    if (!trackBoolean!!){
                        val userLocation = LatLng(location.latitude,location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,9f))
                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply()
                    }


                }
            }

            if(ContextCompat.checkSelfPermission(this@MapsActivity,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this@MapsActivity,Manifest.permission.ACCESS_FINE_LOCATION)){
                    Snackbar.make(binding.root,"Permission needed for Location",Snackbar.LENGTH_INDEFINITE).setAction("Permission Needed"){
                        //  request permission
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }.show()
                }else{
                    // request permission
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }

            }else{
                // permission granted
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,1000,0f,locationListener
                )
                val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if(lastKnownLocation != null){
                    val lastUserLocation = LatLng(lastKnownLocation.latitude,lastKnownLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,9f))
                }
                mMap.isMyLocationEnabled = true
            }


        }else{
            mMap.clear()

            selectedItem = intent.getSerializableExtra("selectedItem") as Place

            selectedItem?.let { it // From Place

                val latLng = LatLng(it.latitude,it.longitude)
                mMap.addMarker(MarkerOptions().position(latLng).title(it.name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,9f))

                binding.editTextText.setText(it.name)
                binding.button.visibility = View.GONE
                binding.button2.visibility = View.VISIBLE

            }

        }

    }

    private fun register_launcher(){
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if(result){
                // permission granted
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,0,0f,locationListener
                    )
                    val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if(lastKnownLocation != null){
                        val lastUserLocation = LatLng(lastKnownLocation.latitude,lastKnownLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,9f))
                    }
                    mMap.isMyLocationEnabled= true
                }
            }else{
                // permission denied
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onMapLongClick(p0: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(p0))
        latitude = p0.latitude
        longitude = p0.longitude
        binding.button.isEnabled = true
        binding.button.isVisible = true
    }

    fun save(view: View){
        var place = Place(binding.editTextText.text.toString(),latitude!!,longitude!!)
        compositeDisposable.add(
            placeDao.insert(place)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }

    private fun handleResponse(){
        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    fun delete(view: View){
        selectedItem?.let {

            compositeDisposable.add(
                placeDao.delete(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}