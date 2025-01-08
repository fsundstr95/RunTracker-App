package com.example.runtracker2.other

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import androidx.core.app.ActivityCompat
import com.example.runtracker2.model.service.PolyLine
import com.vmadalin.easypermissions.EasyPermissions
import timber.log.Timber
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object TrackingUtility {

    fun hasLocationPermissions(context: Context) =
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )




    fun hasBackgroundPermissions(context: Context): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            return true
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            Timber.d("Build version is above TIRAMISU:  ${Build.VERSION.SDK_INT}")
            return EasyPermissions.hasPermissions(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )

        }else{
            Timber.d("Build version is below TIRAMISU:  ${Build.VERSION.SDK_INT}")
            return true
        }
    }


    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String{
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        if (!includeMillis){
            return "${if(hours < 10) "0" else ""}$hours:" +
                    "${if(minutes < 10) "0" else ""}$minutes:" +
                    "${if(seconds < 10) "0" else ""}$seconds"
        }
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        milliseconds /= 10
        return "${if(hours < 10) "0" else ""}$hours:" +
                "${if(minutes < 10) "0" else ""}$minutes:" +
                "${if(seconds < 10) "0" else ""}$seconds:" +
                "${if(milliseconds < 10) "0" else ""}$milliseconds"

    }

    fun calculatePolylineLength(polyLine: PolyLine): Float {
        var distance = 0f
        for (i in 0..polyLine.size - 2){
            val pos1 = polyLine[i]
            val pos2 = polyLine[i+1]

            val result = FloatArray(1)
            Location.distanceBetween(
                pos1.latitude, pos1.longitude, pos2.latitude, pos2.longitude, result
            )
            distance += result[0]
        }
        return distance
    }

    fun formatDateTime(timestampDate : Long) : String{
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestampDate
        }

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val dateString = dateFormat.format(calendar.time)
        return dateString

    }
}