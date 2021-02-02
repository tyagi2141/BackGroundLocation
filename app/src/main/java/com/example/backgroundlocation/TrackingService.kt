package com.example.backgroundlocation

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
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices


/**
 * Created by Rahul on 02/02/21.
 */
class TrackingService : Service() {
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if (locationResult != null && locationResult.lastLocation != null) {
                val latitude = locationResult.lastLocation.latitude
                val longitude = locationResult.lastLocation.longitude
                Log.e("UPDATE_LOCATION", "$latitude = $longitude")
            }
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    @SuppressLint("MissingPermission")
    private fun startLocationService() {
        val channelId = "location_notification_channel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val resultIntent = Intent()
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(
            applicationContext, channelId
        )
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle("Location Service")
        builder.setContentText("running")
        builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(false)
        builder.priority = NotificationCompat.PRIORITY_MAX
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                val notificationChannel = NotificationChannel(
                    channelId, "Location Service",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationChannel.description = "This channel is used by this Location service"
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
        val locationRequest = LocationRequest()
        locationRequest.interval =  1000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
        startForeground(
            Constant.LOCATION_SERVICE_ID,
            builder.build()
        )
    }

    private fun stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this)
            .removeLocationUpdates(locationCallback)
        stopForeground(true)
        stopSelf()
    }

    @SuppressLint("InvalidWakeLockTag")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (action != null) {
                if (action == Constant.ACTION_START_LOCATION_SERVICE) {
                    startLocationService()
                } else if (action == Constant.ACTION_STOP_LOCATION_SERVICE) {
                    stopLocationService()
                }
            }
        }

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag")
        wl.acquire()

        return START_STICKY;

        //  return super.onStartCommand(intent, flags, startId)
    }
}