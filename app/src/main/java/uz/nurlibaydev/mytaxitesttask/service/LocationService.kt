package uz.nurlibaydev.mytaxitesttask.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.mapbox.android.core.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import uz.nurlibaydev.mytaxitesttask.R
import uz.nurlibaydev.mytaxitesttask.data.dao.LocationDao
import uz.nurlibaydev.mytaxitesttask.data.entity.Location
import uz.nurlibaydev.mytaxitesttask.presetation.MainActivity
import uz.nurlibaydev.mytaxitesttask.utils.Constants.CHANNEL_ID
import uz.nurlibaydev.mytaxitesttask.utils.Constants.CHANNEL_NAME
import uz.nurlibaydev.mytaxitesttask.utils.Constants.DEFAULT_INTERVAL_IN_MILLISECONDS
import uz.nurlibaydev.mytaxitesttask.utils.Constants.DEFAULT_MAX_WAIT_TIME
import uz.nurlibaydev.mytaxitesttask.utils.Constants.NOTIFICATION_ID
import uz.nurlibaydev.mytaxitesttask.utils.Constants.TAG
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var locationDao: LocationDao
    private lateinit var locationEngine: LocationEngine

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var callback: LocationEngineCallback<LocationEngineResult>
    private lateinit var notificationManager: NotificationManager
    private lateinit var builder: NotificationCompat.Builder

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        locationEngine = LocationEngineProvider.getBestLocationEngine(this)

        val resultIntent = Intent(this, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)

        val pendingIntent = stackBuilder.getPendingIntent(1, PendingIntent.FLAG_MUTABLE)

        // Create the notification
        builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Service")
            .setContentText("Location Value")
            .setSmallIcon(R.drawable.ic_location)
            .setOngoing(true)
            .setSilent(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        callback = object : LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(result: LocationEngineResult?) {
                result?.lastLocation ?: return
                if (result.lastLocation != null) {
                    val lat = result.lastLocation?.latitude!!
                    val lng = result.lastLocation?.longitude!!
                    scope.launch(Dispatchers.IO) {
                        val currentTime: Date = Calendar.getInstance().time
                        locationDao.addLocation(Location(0, lat, lng, currentTime.toString()))
                        val updatedNotification = builder.setContentText(
                            "Location: ($lat, $lng)"
                        )
                        notificationManager.notify(NOTIFICATION_ID, updatedNotification.build())
                    }
                }
            }
            override fun onFailure(e: Exception) {
                Timber.tag(TAG).d(e.localizedMessage?.toString())
            }
        }

        val request = LocationEngineRequest
            .Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
            .build()

        locationEngine.requestLocationUpdates(
            request,
            callback,
            Looper.getMainLooper()
        )
        locationEngine.getLastLocation(callback)
        startForeground(NOTIFICATION_ID, builder.build())
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.tag("oldi_servis").d("exxxxxxxxxxxxxxxxeeeeeeeeeee")
        job.cancel()
    }
}