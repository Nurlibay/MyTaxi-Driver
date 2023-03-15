package uz.nurlibaydev.mytaxitesttask.utils

import androidx.lifecycle.MutableLiveData

object GlobalObserver {
    var isServiceRunning = MutableLiveData(false)
}