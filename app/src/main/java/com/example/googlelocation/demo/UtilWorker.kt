package com.example.googlelocation.demo

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import com.example.googlelocation.module.pin.util.LocationUpdateUtil
import kotlinx.coroutines.delay

/**
 * Created by luyiling on 2024/6/19
 * Modified by
 *
 * TODO:
 * Description: customize by your own! Make an example for getting last location.
 * You can make a customized worker and workRequest by yourself
 *
 * @params
 * @params
 */
val gpsRequest = OneTimeWorkRequest
    .Builder(GPSWorker::class.java)
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    )
    .build()



open class GPSWorker(context: Context, var params: WorkerParameters)
    : CoroutineWorker(context, params){
    private val locationUtil = LocationUpdateUtil(context.applicationContext)
    private val TAG = GPSWorker::class.java.simpleName

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {

        /**
         * Use getLastLocation for example
         */
        var lastLocation : Location? = null
        locationUtil.getLastLocation(
            true, //因為GPSOn 才做doWork()
            { location -> lastLocation = location },
            { e ->  }
        )
        val timestamp = System.currentTimeMillis()
        while(
            lastLocation == null ||
            (System.currentTimeMillis() - timestamp) < 5000 //timeout
        ){
            delay(1000)
        }
        Log.d(TAG, "Last Location: (${lastLocation?.longitude}, ${lastLocation?.latitude})")
        if(lastLocation == null) return Result.retry()
        return Result.success()
    }
}