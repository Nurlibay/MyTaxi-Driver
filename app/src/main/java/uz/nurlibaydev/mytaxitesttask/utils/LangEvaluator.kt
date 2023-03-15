package uz.nurlibaydev.mytaxitesttask.utils

import android.animation.TypeEvaluator
import com.mapbox.mapboxsdk.geometry.LatLng

val latLngEvaluator: TypeEvaluator<LatLng> = TypeEvaluator<LatLng> { fraction, startValue, endValue ->
    val lat = startValue.latitude + (endValue.latitude - startValue.latitude) * fraction
    val lng = startValue.longitude + (endValue.longitude - startValue.longitude) * fraction
    LatLng(lat, lng)
}
