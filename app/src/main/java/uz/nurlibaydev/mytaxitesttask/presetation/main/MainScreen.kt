package uz.nurlibaydev.mytaxitesttask.presetation.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
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
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.utils.BitmapUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uz.nurlibaydev.mytaxitesttask.BuildConfig
import uz.nurlibaydev.mytaxitesttask.R
import uz.nurlibaydev.mytaxitesttask.databinding.ScreenMainBinding
import uz.nurlibaydev.mytaxitesttask.service.LocationService
import uz.nurlibaydev.mytaxitesttask.utils.getColorRes
import uz.nurlibaydev.mytaxitesttask.utils.hasPermission
import uz.nurlibaydev.mytaxitesttask.utils.isLocationEnabled
import uz.nurlibaydev.mytaxitesttask.utils.showMessage

/**
 *  Created by Nurlibay Koshkinbaev on 08/03/2023 17:07
 */

@AndroidEntryPoint
class MainScreen : Fragment(R.layout.screen_main), OnMapReadyCallback {

    private val binding by viewBinding<ScreenMainBinding>()
    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private val viewModel: MainViewModel by viewModels<MainViewModelImpl>()
    private lateinit var symbolManager: SymbolManager
    private lateinit var currentLocation: LatLng
    private lateinit var icon: Symbol

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) permissionApprovedSnackBar() else permissionDeniedSnackBar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        zoomInAction()
        zoomOutAction()
        navigateMyLocationAction()
    }

    private fun startService() {
        val intent by lazy { Intent(requireContext(), LocationService::class.java) }
        ContextCompat.startForegroundService(requireContext(), intent)
    }

    private fun zoomInAction() {
        binding.btnZoomIn.setOnClickListener {
            val cameraPosition = mapboxMap.cameraPosition
            val newCameraPosition = CameraPosition.Builder(cameraPosition)
                .zoom(cameraPosition.zoom + 1.0)
                .build()
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 1000)
        }
    }

    private fun zoomOutAction() {
        binding.btnZoomOut.setOnClickListener {
            val cameraPosition = mapboxMap.cameraPosition
            val newCameraPosition = CameraPosition.Builder(cameraPosition)
                .zoom(cameraPosition.zoom - 1.0)
                .build()
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 1000)
        }
    }

    private fun navigateMyLocationAction() {
        binding.btnMyLocation.setOnClickListener {
            val locationComponent = mapboxMap.locationComponent
            if (locationComponent.isLocationComponentActivated) {
                val lastLocation: Location? = locationComponent.lastKnownLocation
                lastLocation.let {
                    val cameraPosition = CameraPosition.Builder()
                        .target(LatLng(lastLocation))
                        .zoom(15.0)
                        .build()
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000)
                }
            }
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        observe()
        val styleBuilderLight = Style.Builder().fromUri(Style.MAPBOX_STREETS)
        val styleBuilderNight = Style.Builder().fromUri(MAP_BOX_DARK_MODE)
        val nightModeFlags = requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        mapboxMap.setStyle(
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) styleBuilderNight else styleBuilderLight
        ) { style ->
            mapboxMap.uiSettings.isCompassEnabled = false
            mapboxMap.uiSettings.isLogoEnabled = false
            mapboxMap.uiSettings.isAttributionEnabled = false
            mapboxMap.locationComponent.isLocationComponentActivated
            enableLocationComponent(style)
            val selectedMarkerIconDrawable = ResourcesCompat.getDrawable(this.resources, R.drawable.ic_car, null)
            style.addImage(MARKER_ICON, BitmapUtils.getBitmapFromDrawable(selectedMarkerIconDrawable)!!)
            symbolManager = SymbolManager(mapView, mapboxMap, style)
            symbolManager.iconAllowOverlap = true
            symbolManager.iconIgnorePlacement = true
            icon = symbolManager.create(
                SymbolOptions()
                    .withLatLng(TASHKENT)
                    .withIconImage(MARKER_ICON)
                    .withDraggable(false)
            )
        }
    }

    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            val customLocationComponentOptions =
                LocationComponentOptions.builder(requireContext())
                    .elevation(5f)
                    .pulseEnabled(true)
                    .pulseColor(Color.GREEN)
                    .pulseAlpha(.4f)
                    .accuracyAnimationEnabled(true)
                    .trackingGesturesManagement(true)
                    .accuracyAlpha(.6f)
                    .accuracyColor(ContextCompat.getColor(requireContext(), R.color.mapboxGreen))
                    .build()

            val locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(requireContext(), loadedMapStyle).locationComponentOptions(customLocationComponentOptions)
                    .build()

            mapboxMap.locationComponent.apply {
                activateLocationComponent(locationComponentActivationOptions)
                if (ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    showMessage("isLocationComponentEnabled = true")
                    isLocationComponentEnabled = true
                    cameraMode = CameraMode.TRACKING
                    renderMode = RenderMode.COMPASS
                }
            }
        }
    }

    private fun observe() {
        viewModel.allUserLocations.onEach { locations ->
            if (locations.isEmpty()) return@onEach
            val lastItem = locations.last()
            val lastLocation = LatLng(lastItem.lat, lastItem.lng)
            currentLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
            if (::icon.isInitialized) {
                icon.apply {
                    this.latLng = currentLocation
                    symbolManager.update(this)
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun locationRequest() {
        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            startService()
            viewModel.getAllLocations()
        } else requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun permissionApprovedSnackBar() {
        Snackbar.make(binding.root, R.string.permission_approved_explanation, BaseTransientBottomBar.LENGTH_LONG).show()
    }

    private fun permissionDeniedSnackBar() {
        Snackbar.make(
            binding.root, R.string.fine_permission_denied_explanation, BaseTransientBottomBar.LENGTH_LONG
        ).setAction(R.string.settings) { launchSettings() }.setActionTextColor(getColorRes(R.color.white)).show()
    }

    private fun launchSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts(
            "package", BuildConfig.APPLICATION_ID, null
        )
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle(getString(R.string.enable_location)).setMessage(getString(R.string.enable_location_message))
            .setPositiveButton(getString(R.string.location_settings)) { _, _ ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                myIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(myIntent)
            }.setNegativeButton(getString(R.string.cancel)) { _, _ -> }
        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        if (isLocationEnabled()) locationRequest()
        else showAlert()
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
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private val TASHKENT = LatLng(41.2995, 69.2401)
        private const val MAP_BOX_DARK_MODE = "mapbox://styles/mapbox/dark-v11"
        private const val MARKER_ICON = "MARKER_ICON"
    }
}