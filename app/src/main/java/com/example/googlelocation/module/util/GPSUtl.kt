package com.example.googlelocation.module.util

import android.app.Activity
import android.content.Context
import android.location.LocationManager
import android.util.Log
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task

/**
 * Created by luyiling on 2024/2/23
 * Modified by
 *
 * TODO: Done
 * Description:
 *
 * @params
 * @params
 */
/**
 * Created by luyiling on 2019/3/31
 *
 *
 * method for turn on GPS
 */
class GpsUtil private constructor() {
    private lateinit var locationManager: LocationManager
    private lateinit var client: SettingsClient
    var settingRequest: LocationSettingsRequest? = null
        private set
    var locateRequest: LocationRequest? = null
        private set
    private lateinit var observer : GPSResolvableApiLifecycleObserver
    private lateinit var onGpsListener: OnGpsListener
    private val TAG = GpsUtil::class.java.simpleName

    companion object{
        val MY_DEFAULT_LOCATION_REQ = LocationRequest.Builder(
            LocationRequest.PRIORITY_HIGH_ACCURACY, //city level:PRIORITY_LOW_POWER
            10000//10s
        ).build()

    }
    private fun createClient(application: Context) {
        locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(locateRequest == null)
            locateRequest = MY_DEFAULT_LOCATION_REQ
        if(settingRequest == null){
            settingRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(locateRequest!!)
                .setAlwaysShow(true) //this is the key ingredient
                .build()
        }
        client = LocationServices.getSettingsClient(application)
    }


    /**
     * Android 4.4-7: gps check
     * locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
     *
     * @param onGpsListener
     */
    fun exeGPSOn() {
        if (!::observer.isInitialized) throw Exception("GPSResolvableApiLifecycleObserver shall initiate when Fragment / Activity onCreate(). Do addObserver() !!!")
        if (!::onGpsListener.isInitialized) throw Exception("OnGpsListener shall be initiated. Do addObserver() first")
        if (settingRequest == null) throw Exception("settingRequest shall be not be null. Do createClient() first")
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(settingRequest!!)
        //do success
        task.addOnSuccessListener { locationSettingsResponse -> //GPS is already enable, callback GPS status through listener
            val states = locationSettingsResponse.locationSettingsStates
            /*
                    * gps is on, network is on:
                    *   isGPSUsable:false
                        isGPSPresent:true
                        isNetworkPresent:true
                        isNetworkUsable:true
                        isLocationPresent:true
                        isLocationUsable:true
                    * */
            Log.e(TAG, """
     isGPSUsable:${states!!.isGpsUsable}
     isGPSPresent:${states.isGpsPresent}
     isNetworkPresent:${states.isNetworkLocationPresent}
     isNetworkUsable:${states.isNetworkLocationUsable}
     isLocationPresent:${states.isLocationPresent}
     isLocationUsable:${states.isLocationUsable}
     """.trimIndent()
            )
            onGpsListener.gpsStatus(states.isGpsPresent)
        }

        //do fail
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                observer.resolve(e)
            } else {
                Log.e(TAG, e.toString())
                onGpsListener.gpsStatus(false)
            }
        }
    }

    //只由Builder呼叫
    private fun addObserver(lifecycleObserver: GPSResolvableApiLifecycleObserver) {
        observer = lifecycleObserver
        onGpsListener = lifecycleObserver.listener
    }
    //只由Builder呼叫
    private fun addSettingRequest(request: LocationSettingsRequest){
        settingRequest = request
    }
    //只由Builder呼叫
    private fun addLocationRequest(request: LocationRequest){
        locateRequest = request
    }

    class Builder{
        private var _context : Context
        private var _settingRequest: LocationSettingsRequest? = null
        private var _locateRequest: LocationRequest? = null
        private var _observer : GPSResolvableApiLifecycleObserver? = null

        constructor(context: Context){
            _context = context
        }
        fun addObserver(observer: GPSResolvableApiLifecycleObserver) :Builder{
            _observer = observer
            return this
        }
        fun addSettingRequest(settingRequest: LocationSettingsRequest) :Builder{
            _settingRequest = settingRequest
            return this
        }
        fun addLocationRequest(locateRequest: LocationRequest):Builder{
            _locateRequest = locateRequest
            return this
        }
        fun build() : GpsUtil {
            checkNotNull(_observer)
            return GpsUtil().apply {
                addObserver(_observer!!)
                _settingRequest?.let{ addSettingRequest(it)}
                _locateRequest?.let { addLocationRequest(it) }
                createClient(_context)
            }
        }
    }

}

class GPSResolvableApiLifecycleObserver(
    val listener: OnGpsListener
) : DefaultLifecycleObserver {
    private lateinit var gpsOnRequest: ActivityResultLauncher<IntentSenderRequest>
    private var _listener : OnGpsListener? = null
    private val TAG = GPSResolvableApiLifecycleObserver::class.java.simpleName

    /*隨著呼叫的lifecycle: onCreate*/
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        Log.d(TAG, "onCreate")
        /*確認是ComponentActivity or Fragment*/
        if (owner !is ActivityResultCaller) return
        gpsOnRequest = owner.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            listener.gpsStatus(result.resultCode == Activity.RESULT_OK)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d(TAG, "onStart")
        _listener = listener
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d(TAG, "onStop")
        _listener = null
    }

    fun resolve(e: ResolvableApiException){
        gpsOnRequest.launch(IntentSenderRequest.Builder(e.resolution).build())
    }
}

fun interface OnGpsListener {
    fun gpsStatus(isGPSEnable: Boolean)
}
