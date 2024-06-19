package com.example.googlelocation.module.pin.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.util.Log
import com.example.googlelocation.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import cus.yiling.location.BuildConfig
import java.lang.IllegalArgumentException

/**
 * Created by luyiling on 2024/2/23
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
/**
 * Created by luyiling on 2019/4/5
 *
 * note: PermissionUtil -> GpsUtil -> LocationUpdateUtil
 */
class LocationUpdateUtil(application: Context) {
    private lateinit var fusedClient: FusedLocationProviderClient
    private val TAG = LocationUpdateUtil::class.java.simpleName
    private var mCurrentLocation: Location? = null
    //LocationUpdate 持續性時才會填入的參數
    private var callback: LocationCallback? = null

    //parseLocation 需要的參數
    private var resultReceiver: ResultReceiver? = null
    private var handler: Handler? = null
    private var context: Context? = null




    /**
     * 取得最新的位置
     * this method is for get last location only, not consistently to update location
     * special case: location be null
     * * 1.Location is turned off in the device settings
     * * 2.The device never recorded its location,
     * * 3.the device that has been restored to factory settings.
     * * 4.Google Play services on the device has restarted
     * * 5. GPS is close
     * *
     * @return Location format
     */
    @SuppressLint("MissingPermission")
    fun getLastLocation(
        alreadyGPSOn: Boolean,
        onSuccess: (Location) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        check(::fusedClient.isInitialized) { Log.e(TAG, "Do start() first") }
        fusedClient.lastLocation.addOnSuccessListener { location ->
            val passResult: (Location) -> Unit = { locate ->
                Log.w(TAG, "(" + locate.latitude + "," + locate.longitude+")")
                mCurrentLocation = locate
                onSuccess.invoke(locate)
            }
            when{
                location != null -> passResult(location)
                //if the permission or gps is off, then get granted permission and gps is on now
                //last location will be null
                !alreadyGPSOn -> {
                    onFailure.invoke(Exception("Location is null"))
                    return@addOnSuccessListener
                }
                //actively request new location now
                else -> {
                    getLocationUpdates(
                        LocationRequest.Builder(
                            LocationRequest.PRIORITY_HIGH_ACCURACY, //city level:PRIORITY_LOW_POWER
                            60 * 1000//1 min
                        )
                            .setMaxUpdates(1) //只更新一次
                            .build(),
                        { location -> passResult(location) },
                        { e-> onFailure.invoke(e) },
                        true
                    )
                }
            }
        }
        fusedClient.lastLocation.addOnFailureListener { e ->
            onFailure.invoke(e)
        }
    }

    /**
     * 取得最新的位置
     *
     * this method is for get last location only, not consistently to update location
     * special case: location be null
     * * 1.Location is turned off in the device settings
     * * 2.The device never recorded its location,
     * * 3.the device that has been restored to factory settings.
     * * 4.Google Play services on the device has restarted
     * * 5. GPS is close
     * *
     * @return Address Format
     */
    @SuppressLint("MissingPermission")
    fun getLastAddress(
        alreadyGPSOn: Boolean,
        onSuccess: (Address) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        check(::fusedClient.isInitialized) { Log.e(TAG, "Do start() first") }
        fusedClient.lastLocation.addOnSuccessListener { location ->
            val passResult: (Location) -> Unit = { locate ->
                Log.w(TAG, "(" + locate.latitude + "," + locate.longitude+")")
                mCurrentLocation = locate
                parseLocation(onSuccess,onFailure)
            }
            when{
                location != null -> passResult(location)
                //if the permission or gps is off, then get granted permission and gps is on now
                //last location will be null
                !alreadyGPSOn -> {
                    onFailure.invoke(Exception("Location is null"))
                    return@addOnSuccessListener
                }
                //actively request new location now
                else -> {
                    getLocationUpdates(
                        LocationRequest.Builder(
                            LocationRequest.PRIORITY_HIGH_ACCURACY, //city level:PRIORITY_LOW_POWER
                            5 * 60 * 1000//5 min
                        )
                            .setMaxUpdates(1) //只更新一次
                            .build(),
                        { location -> passResult(location) },
                        { e-> onFailure.invoke(e) },
                        true
                    )
                }
            }
        }
        fusedClient.lastLocation.addOnFailureListener { e ->
            onFailure.invoke(e)
        }
    }



    /**
     * 主動索取位置更新
     * @param locationRequest 來自GPSUtil的設定，設定更新頻率
     * @param once (T)一次 (F)持續收到
     * @return Location format
     */
    @SuppressLint("MissingPermission")
     fun getLocationUpdates(
         locationRequest: LocationRequest,
         onSuccess: (Location) -> Unit,
         onFail: (Exception) -> Unit,
         once: Boolean
    ) {
        check(::fusedClient.isInitialized) { Log.e(TAG, "Do start() first") }
        callback = object : LocationCallback() {
             override fun onLocationResult(locationResult: LocationResult) {
                 mCurrentLocation = locationResult.lastLocation
                 Log.w(TAG, "Last Location = ("+mCurrentLocation?.latitude.toString() + "," + mCurrentLocation?.longitude +")")
                 if (mCurrentLocation == null) onFail.invoke(Exception("Last location is null"))
                 else onSuccess.invoke(mCurrentLocation!!)
                 if (once) fusedClient.removeLocationUpdates(this)
             }
         }
        fusedClient.requestLocationUpdates(
            locationRequest,
            callback!!,
            null /* Looper */ //null -> current looper
        )
    }

    /**
     * 主動索取位置更新
     * @param locationRequest 來自GPSUtil的設定，設定更新頻率
     * @param once (T)一次 (F)持續收到
     * @return Address format
     */
    @SuppressLint("MissingPermission")
    fun getAddressUpdates(
        locationRequest: LocationRequest,
        onSuccess: (Address) -> Unit,
        onFail: (Exception) -> Unit,
        once: Boolean
    ) {
        check(::fusedClient.isInitialized) { Log.e(TAG, "Do start() first") }
        callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                mCurrentLocation = locationResult.lastLocation
                Log.w(TAG, "Last Location = ("+mCurrentLocation?.latitude.toString() + "," + mCurrentLocation?.longitude +")")
                if (mCurrentLocation == null) onFail.invoke(Exception("Last location is null"))
                parseLocation(onSuccess, onFail)
                if (once) fusedClient.removeLocationUpdates(this)
            }
        }
        fusedClient.requestLocationUpdates(
            locationRequest,
            callback!!,
            null /* Looper */ //null -> current looper
        )
    }


    init {
        this.context = application
        fusedClient = LocationServices.getFusedLocationProviderClient(application)
    }

    //remove it when view is detached
    fun detach() {
        if (resultReceiver != null) {
            //關掉Service
            val intent = Intent(BuildConfig.ACTION_LOCATION)
            intent.addCategory(context!!.packageName)
            intent.setPackage(context!!.packageName)
            context!!.stopService(intent)
            resultReceiver = null
        }
        if (callback != null) fusedClient.removeLocationUpdates(callback!!)
        callback = null
        handler?.removeCallbacksAndMessages(null)
        handler = null
        context = null
    }



    /**
     * note: key point
     */
    private fun parseLocation(onSuccess: (Address) -> Unit, onFail: (Exception) -> Unit) {
        handler = Handler(Looper.myLooper()!!)
        resultReceiver = object : ResultReceiver(handler) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                if (resultCode == Constants.SUCCESS_RESULT) {
                    val address = resultData.getParcelable<Address>(Constants.RESULT_DATA_OBJ)
                    if(address == null) onFail(Exception("Address is null"))
                    else onSuccess(address!!)
                }else {
                    onFail(Exception(resultData.getString(Constants.RESULT_DATA_MSG)))
                }
            }
        }
        if (context != null) {
            Log.e(TAG, "CHECK pkg name = ${context!!.packageName}")
            val intent = Intent(BuildConfig.ACTION_LOCATION)
            intent.addCategory(context!!.packageName)
            intent.setPackage(context!!.packageName)
            intent.putExtra(Constants.RECEIVER, resultReceiver)
            intent.putExtra(Constants.LOCATION_DATA_EXTRA, mCurrentLocation)
            context!!.startService(intent)
        }
    }
    enum class LocationMethod(val id: Int){
        /**
         * those are LocationUpdateUtil's methods for usage
         */
        GetLastLocation(0),GetLastAddress(1),
        GetLocationUpdates(2),GetAddressUpdates(3);

        companion object{
            fun getId(id: Int) : LocationMethod {
                for (method in enumValues<LocationMethod>()){
                    if (method.id == id) return method
                }
                throw IllegalArgumentException("id,$id, is not within enum LocationMethod's values" )
            }
        }

    }
}






