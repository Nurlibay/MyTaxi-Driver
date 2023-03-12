package uz.nurlibaydev.mytaxitesttask.presetation.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mapbox.android.core.location.*
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uz.nurlibaydev.mytaxitesttask.BuildConfig
import uz.nurlibaydev.mytaxitesttask.R
import uz.nurlibaydev.mytaxitesttask.databinding.ScreenMainBinding
import uz.nurlibaydev.mytaxitesttask.service.LocationService
import uz.nurlibaydev.mytaxitesttask.utils.*

/**
 *  Created by Nurlibay Koshkinbaev on 08/03/2023 17:07
 */

@AndroidEntryPoint
class MainScreen : Fragment(R.layout.screen_main), OnMapReadyCallback {

    private val binding by viewBinding<ScreenMainBinding>()
    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private val viewModel: MainViewModel by viewModels<MainViewModelImpl>()

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) permissionApprovedSnackBar() else permissionDeniedSnackBar()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token))
        val view = inflater.inflate(R.layout.screen_main, container, false)
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        if (!isLocationEnabled()) {
            showAlert()
        }
        startService()
    }

    private fun startService(){
        if(isLocationEnabled() && hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            val intent = Intent(requireContext(), LocationService::class.java)
            ContextCompat.startForegroundService(requireContext(), intent)
            viewModel.getAllLocations()
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        observe()
        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            enableLocationComponent(it)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            val customLocationComponentOptions = LocationComponentOptions.builder(requireContext())
                .pulseEnabled(true)
                .pulseColor(Color.GREEN)
                .pulseAlpha(.4f)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(requireContext(), R.color.mapboxGreen))
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(requireContext(), loadedMapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .build()

            mapboxMap.locationComponent.apply {
                activateLocationComponent(locationComponentActivationOptions)
                isLocationComponentEnabled = true
                cameraMode = CameraMode.TRACKING
                renderMode = RenderMode.COMPASS
            }
        }
    }

    private fun observe() {
        viewModel.allUserLocations.onEach {
            val lastItem = it.last()
            val lastLocation = LatLng(lastItem.lat, lastItem.lng)
            val markerOptions = MarkerOptions().apply {
                position = lastLocation
                icon = IconFactory.getInstance(requireContext())
                    .fromBitmap(bitmapFromDrawableRes(R.drawable.ic_car)!!)
                snippet("Nukus")
            }

            val position = CameraPosition.Builder()
                .target(lastLocation)
                .zoom(12.0)
                .tilt(45.0)
                .bearing(180.0)
                .build()

            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3000)
            mapboxMap.addMarker(markerOptions)

        }.launchIn(lifecycleScope)
    }

    private fun permissionApprovedSnackBar() {
        Snackbar.make(binding.root, R.string.permission_approved_explanation, BaseTransientBottomBar.LENGTH_LONG).show()
    }

    private fun permissionDeniedSnackBar() {
        Snackbar.make(
            binding.root,
            R.string.fine_permission_denied_explanation,
            BaseTransientBottomBar.LENGTH_LONG
        )
            .setAction(R.string.settings) { launchSettings() }
            .setActionTextColor(getColorRes(R.color.white))
            .show()
    }

    private fun launchSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts(
            "package",
            BuildConfig.APPLICATION_ID, null
        )
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle(getString(R.string.enable_location))
            .setMessage(getString(R.string.enable_location_message))
            .setPositiveButton(getString(R.string.location_settings)) { _, _ ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                myIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(myIntent)
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().stopService(Intent(requireContext(), LocationService::class.java))
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}