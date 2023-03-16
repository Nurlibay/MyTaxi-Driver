package uz.nurlibaydev.mytaxitesttask.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.mapbox.android.core.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import uz.nurlibaydev.mytaxitesttask.R
import uz.nurlibaydev.mytaxitesttask.data.entity.Location
import uz.nurlibaydev.mytaxitesttask.domain.repository.LocationRepository
import uz.nurlibaydev.mytaxitesttask.presetation.MainActivity
import uz.nurlibaydev.mytaxitesttask.utils.Constants.CHANNEL_ID
import uz.nurlibaydev.mytaxitesttask.utils.Constants.CHANNEL_NAME
import uz.nurlibaydev.mytaxitesttask.utils.Constants.DEFAULT_INTERVAL_IN_MILLISECONDS
import uz.nurlibaydev.mytaxitesttask.utils.Constants.DEFAULT_MAX_WAIT_TIME
import uz.nurlibaydev.mytaxitesttask.utils.Constants.FAILURE_TAG
import uz.nurlibaydev.mytaxitesttask.utils.Constants.NOTIFICATION_CONTENT_TEXT
import uz.nurlibaydev.mytaxitesttask.utils.Constants.NOTIFICATION_ID
import uz.nurlibaydev.mytaxitesttask.utils.Constants.NOTIFICATION_TITLE
import uz.nurlibaydev.mytaxitesttask.utils.GlobalObserver
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class LocationService @Inject constructor() : Service() {

    @Inject
    lateinit var repository: LocationRepository
    private val locationEngine: LocationEngine by lazy { LocationEngineProvider.getBestLocationEngine(this) }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val callback = object : LocationEngineCallback<LocationEngineResult> {
        override fun onSuccess(result: LocationEngineResult?) {
            val lastLocation = result?.lastLocation ?: return
            val lat = lastLocation.latitude
            val lng = lastLocation.longitude
            val bearing = lastLocation.bearing
            scope.launch(Dispatchers.IO) {
                val currentTime: Date = Calendar.getInstance().time
                repository.addLocation(Location(0, lat, lng, currentTime.toString(), bearing))
                val updatedNotification = builder.setContentText("Coordinates: ($lat, $lng)")
                notificationManager.notify(NOTIFICATION_ID, updatedNotification.build())
            }
        }

        override fun onFailure(e: Exception) {
            Timber.tag(FAILURE_TAG).d(e.localizedMessage?.toString())
        }
    }
    private lateinit var notificationManager: NotificationManager
    private lateinit var builder: NotificationCompat.Builder

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val resultPendingIntent: PendingIntent? = createPendingIntent(resultIntent)

        builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(NOTIFICATION_CONTENT_TEXT)
            .setSmallIcon(R.drawable.ic_location)
            .setContentIntent(resultPendingIntent)
            .setOngoing(true)
            .setSilent(true)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        GlobalObserver.isServiceRunning.postValue(true)
        val request = LocationEngineRequest
            .Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
            .build()

        locationEngine.requestLocationUpdates(request, callback, Looper.getMainLooper())
        locationEngine.getLastLocation(callback)
        startForeground(NOTIFICATION_ID, builder.build())
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createPendingIntent(intent: Intent): PendingIntent? {
        return TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
