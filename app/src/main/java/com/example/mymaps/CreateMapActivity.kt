package com.example.mymaps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mymaps.databinding.ActivityCreateMapBinding
import com.example.mymaps.model.Place
import com.example.mymaps.model.UserMap
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar

class CreateMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityCreateMapBinding
    private var markers: MutableList<Marker> = mutableListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.title = intent.getStringExtra(EXTRA_MAP_TITLE)

        binding = ActivityCreateMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mapFragment.view?.let {
            Snackbar.make(it, "Long press to add a marker", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", {})
                .setActionTextColor(resources.getColor(android.R.color.white))
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_create_map, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.miSave){
            Log.i("CreateMapActivity", "Tapped on Save")
            if(markers.isEmpty()){
                Toast.makeText(this, "There must be at least one marker on the map", Toast.LENGTH_LONG).show()
                return true
            }
            val places = markers.map{marker -> marker.title?.let { marker.snippet?.let { it1 ->
                Place(it,
                    it1, marker.position.latitude, marker.position.longitude)
            } } }
            val userMap = intent.getStringExtra(EXTRA_MAP_TITLE)?.let { UserMap(it, places) }
            val data = Intent()
            data.putExtra(EXTRA_USER_MAP, userMap)
            setResult(RESULT_OK, data)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnInfoWindowClickListener {
            markers.remove(it)
            it.remove()
        }

        mMap.setOnMapLongClickListener { latlog ->
            showAlertDialog(latlog)
        }
    }

    private fun showAlertDialog(latlog: LatLng) {
        val placeFormView = LayoutInflater.from(this).inflate(R.layout.dialog_create_place, null)
        var dialog = AlertDialog.Builder(this)
            .setTitle("Create a marker")
            .setView(placeFormView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val title = placeFormView.findViewById<EditText>(R.id.etTitle).text.toString()
            val description = placeFormView.findViewById<EditText>(R.id.etDescription).text.toString()

            if(title.trim().isEmpty() || description.trim().isEmpty()){
                Toast.makeText(this, "Title and description cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val marker = mMap.addMarker(MarkerOptions().position(latlog).title(title).snippet(description))
            if (marker != null) {
                markers.add(marker)
            }
            dialog.dismiss()
        }

    }
}