package com.sap.codelab.view.create

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityCreateMemoBinding
import com.sap.codelab.utils.extensions.empty
import com.sap.codelab.location.GeofenceHelper
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlin.toString

/**
 * Activity that allows a user to create a new Memo.
 */

private const val LOCATION_PERMISSION_REQUEST_CODE = 1001

internal class CreateMemo : AppCompatActivity(), OnMapReadyCallback {
    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    private fun hasLocationPermissions(): Boolean {
        return locationPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private lateinit var binding: ActivityCreateMemoBinding
    private lateinit var model: CreateMemoViewModel
    private var selectedLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        model = ViewModelProvider(this)[CreateMemoViewModel::class.java]
        binding.contentCreateMemo.mapView.onCreate(savedInstanceState)
        binding.contentCreateMemo.mapView.getMapAsync(this)
    }

    private fun showLocationError(show: Boolean) {
        val label = binding.contentCreateMemo.labelSelectLocation
        if (show) {
            label.setTextColor(ContextCompat.getColor(this, R.color.design_default_color_error))
            label.text = getString(R.string.location_required_error)
        } else {
            label.setTextColor(ContextCompat.getColor(this, R.color.primary_text_color))
            label.text = getString(R.string.select_location)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng))
            selectedLatLng = latLng
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            val title = binding.contentCreateMemo.memoTitle.text.toString()
            val description = binding.contentCreateMemo.memoDescription.text.toString()
            model.updateMemo(title, description, latLng.latitude, latLng.longitude)
            showLocationError(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_memo, menu)
        return true
    }

    /**
     * Handles actionbar interactions.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveMemo()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Saves the memo if the input is valid; otherwise shows the corresponding error messages.
     */

    private fun saveMemo() {
        if (!hasLocationPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                locationPermissions,
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        binding.contentCreateMemo.run {
            val lat = selectedLatLng?.latitude ?: 0.0
            val lng = selectedLatLng?.longitude ?: 0.0
            model.updateMemo(memoTitle.text.toString(), memoDescription.text.toString(), lat, lng)
            if (model.hasLocationError()) {
                showLocationError(true)
                return
            } else {
                showLocationError(false)
            }
            if (model.isMemoValid()) {
                lifecycleScope.launch {
                    val savedMemo = model.saveMemoAndReturn()
                    GeofenceHelper.addGeofence(
                        this@CreateMemo,
                        savedMemo.reminderLatitude,
                        savedMemo.reminderLongitude,
                        savedMemo.id.toString(),
                        savedMemo.title,
                        description = savedMemo.description
                    )
                }
                setResult(RESULT_OK)
                finish()
            } else {
                memoTitleContainer.error =
                    getErrorMessage(model.hasTitleError(), R.string.memo_title_empty_error)
                memoDescription.error =
                    getErrorMessage(model.hasTextError(), R.string.memo_text_empty_error)
            }
        }
    }

    /**
     * Returns the error message if there is an error, or an empty string otherwise.
     *
     * @param hasError          - whether there is an error.
     * @param errorMessageResId - the resource id of the error message to show.
     * @return the error message if there is an error, or an empty string otherwise.
     */
    private fun getErrorMessage(hasError: Boolean, @StringRes errorMessageResId: Int): String {
        return if (hasError) {
            getString(errorMessageResId)
        } else {
            String.empty()
        }
    }

    override fun onResume() {
        super.onResume(); binding.contentCreateMemo.mapView.onResume()
    }

    override fun onPause() {
        super.onPause(); binding.contentCreateMemo.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy(); binding.contentCreateMemo.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory(); binding.contentCreateMemo.mapView.onLowMemory()
    }
}
