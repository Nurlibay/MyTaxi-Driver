package uz.nurlibaydev.mytaxitesttask.presetation.ui

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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
    private var mapView: MapView? = null
    private lateinit var mapboxMap: MapboxMap
    private val viewModel: MainViewModel by viewModels()
    private lateinit var symbolManager: SymbolManager
    private var currentLocation: LatLng? = null
    private lateinit var icon: Symbol

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) permissionApprovedSnackBar() else permissionDeniedSnackBar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = binding.mapView
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
        binding.apply {
            binding.btnZoomIn.onClick { zoomInAction() }
            binding.btnZoomOut.onClick { zoomOutAction() }
            binding.btnMyLocation.onClick { navigateMyLocationAction() }
        }
    }

    private fun zoomInAction() {
        val cameraPosition = mapboxMap.cameraPosition
        val newCameraPosition = CameraPosition.Builder(cameraPosition).zoom(cameraPosition.zoom + 1.0).build()
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 1000)
    }

    private fun zoomOutAction() {
        val cameraPosition = mapboxMap.cameraPosition
        val newCameraPosition = CameraPosition.Builder(cameraPosition).zoom(cameraPosition.zoom - 1.0).build()
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 1000)
    }

    private fun navigateMyLocationAction() {
        val cameraPosition = CameraPosition.Builder().target(currentLocation).zoom(15.0).build()
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        viewModel.getLastLocation()
        setupObserver()
        val styleBuilderLight = Style.Builder().fromUri(Style.MAPBOX_STREETS)
        val styleBuilderNight = Style.Builder().fromUri(MAP_BOX_DARK_MODE)
        val nightModeFlags = requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        mapboxMap.setStyle(
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) styleBuilderNight else styleBuilderLight,
        ) { style ->
            mapboxMap.uiSettings.apply {
                isCompassEnabled = false
                isLogoEnabled = false
                isAttributionEnabled = false
            }
            enableLocationComponent(style)
            bitmapFromDrawableRes(R.drawable.ic_car)?.let { style.addImage(MARKER_ICON, it) }
            symbolManager = SymbolManager(mapView!!, mapboxMap, style)
            symbolManager.iconAllowOverlap = true
            symbolManager.iconIgnorePlacement = true
            icon = symbolManager.create(
                SymbolOptions().withLatLng(TASHKENT).withIconImage(MARKER_ICON).withDraggable(false),
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            val customLocationComponentOptions = LocationComponentOptions.builder(requireContext()).build()
            val locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(requireContext(), loadedMapStyle).locationComponentOptions(customLocationComponentOptions)
                    .build()
            mapboxMap.locationComponent.apply {
                activateLocationComponent(locationComponentActivationOptions)
                isLocationComponentEnabled = true
                cameraMode = CameraMode.TRACKING
                renderMode = RenderMode.COMPASS
            }
        }
    }

    private fun setupObserver() {
        viewModel.lastLocation.onEach { location ->
            val lat = location.lat
            val lng = location.lng
            val bearing = location.bearing.toDouble()
            currentLocation = LatLng(lat, lng)
            val camera = CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder().target(currentLocation).bearing(bearing).build(),
            )
            if (::icon.isInitialized) {
                mapboxMap.animateCamera(camera)
                val objectAnimator = ObjectAnimator.ofObject(latLngEvaluator, icon.latLng, currentLocation).setDuration(2000L)
                objectAnimator.addUpdateListener {
                    icon.latLng = it.animatedValue as LatLng
                    symbolManager.update(icon)
                }
                objectAnimator.start()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun locationRequest() {
        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            startService()
        } else {
            requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun startService() {
        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            GlobalObserver.isServiceRunning.observe(viewLifecycleOwner) { serviceActive ->
                if (!serviceActive) {
                    val intent = Intent(requireContext(), LocationService::class.java)
                    ContextCompat.startForegroundService(requireContext(), intent)
                }
            }
        }
    }

    private fun permissionApprovedSnackBar() {
        Snackbar.make(binding.root, R.string.permission_approved_explanation, BaseTransientBottomBar.LENGTH_LONG).show()
    }

    private fun permissionDeniedSnackBar() {
        Snackbar.make(
            binding.root,
            R.string.fine_permission_denied_explanation,
            BaseTransientBottomBar.LENGTH_LONG,
        ).setAction(R.string.settings) { launchSettings() }.setActionTextColor(getColorRes(R.color.white)).show()
    }

    private fun launchSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts(
            "package",
            BuildConfig.APPLICATION_ID,
            null,
        )
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun showDialog() {
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
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        checkNetworkConnection()
        if (isLocationEnabled()) {
            locationRequest()
        } else {
            showDialog()
        }
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    private fun checkNetworkConnection() {
        val connectivityLiveData = ConnectivityLiveData(requireActivity().application)
        connectivityLiveData.observe(viewLifecycleOwner) { isAvailable ->
            when (isAvailable) {
                false -> showMessage("No internet connection")
                else -> {}
            }
        }
    }

    companion object {
        private val TASHKENT = LatLng(41.2937681, 69.2461707)
        private const val MAP_BOX_DARK_MODE = "mapbox://styles/mapbox/dark-v11"
        private const val MARKER_ICON = "MARKER_ICON"
    }
}
