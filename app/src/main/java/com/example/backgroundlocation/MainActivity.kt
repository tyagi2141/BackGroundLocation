package com.example.backgroundlocation

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_LOCATION_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = packageName
            val pm =
                getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }

        findViewById<View>(R.id.start).setOnClickListener { view: View? ->
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_LOCATION_PERMISSION
                )
            } else {

                startLocationService()
            }
        }
        findViewById<View>(R.id.stop).setOnClickListener { stopLocationService() }
    }

    private fun isLocationIsRunning(): Boolean {
        val activityManager =
            getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (activityManager != null) {
            for (serviceInfo in activityManager.getRunningServices(
                Int.MAX_VALUE
            )) {
                if (TrackingService::class.java.name == serviceInfo.service.className) {
                    if (serviceInfo.foreground) {
                        return true
                    }
                }
            }
            return false
        }
        return false
    }

    private fun startLocationService() {

        Log.e("vchdhvchd","yess "+isLocationIsRunning())


        if (!isLocationIsRunning()) {
            val intent = Intent(applicationContext, TrackingService::class.java)
            intent.action = Constant.ACTION_START_LOCATION_SERVICE
            startService(intent)
            Toast.makeText(this, "start", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopLocationService() {
        if (isLocationIsRunning()) {
            val intent = Intent(applicationContext, TrackingService::class.java)
            intent.action = Constant.ACTION_STOP_LOCATION_SERVICE
            startService(intent)
            Toast.makeText(this, "stop", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.size > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationService()
            } else {
                Toast.makeText(applicationContext, "permission not granted", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}