package com.example.googlelocation.module.man

import android.content.Context
import android.location.Address
import android.location.Location
import androidx.activity.result.ActivityResultCaller
import com.example.googlelocation.module.pin.GPSUnchangedException
import com.example.googlelocation.module.pin.PermissionDeniedException
import com.example.googlelocation.module.pin.core.Callback
import com.example.googlelocation.module.pin.util.GpsUtil
import com.example.googlelocation.module.pin.util.GPSResovableCore
import com.example.googlelocation.module.pin.util.LocationUpdateUtil
import com.example.googlelocation.module.pin.util.PermissionCore
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import java.lang.Exception


/**
 * Created by luyiling on 2024/6/17
 * Modified by
 *
 * TODO:
 * Description: 特色是Util 功能，但不綁定在view裡，
 * 由使用者本身處理callbacks的存活，可參考 {@link UtilFunctionActivity}
 *
 * @param owner Activity/Fragment
 * @params
 */
class UtilDelegator(val owner: ActivityResultCaller) {
    private val TAG = UtilDelegator::class.java.simpleName
    private var cxt : Context? = null

    //permission
    private val permissionCore = PermissionCore(
        owner, object : Callback {
            override fun onGranted() {
                gpsUtil.exeGPSOn()
            }

            override fun onDenied(denyPermission: Map<String, Boolean>) {
                when(method){
                    LocationUpdateUtil.LocationMethod.GetLastLocation,
                    LocationUpdateUtil.LocationMethod.GetLocationUpdates
                    -> locationErrObserver
                    else -> addressErrObserver
                }.invoke(PermissionDeniedException(denyPermission))
            }
        }
    )



    //gps
    private val gpsOnCore = GPSResovableCore(owner) { on ->
        if(on) requestLocation(method)
        when(method){
            LocationUpdateUtil.LocationMethod.GetLastLocation,
            LocationUpdateUtil.LocationMethod.GetLocationUpdates
            -> locationErrObserver
            else -> addressErrObserver
        }.invoke(GPSUnchangedException())
    }

    private lateinit var gpsUtil : GpsUtil
    private lateinit var locationUtil: LocationUpdateUtil






    /*****************************/
    /*** customize by developer **/
    /*****************************/

    var locationReq : LocationRequest = GpsUtil.MY_DEFAULT_LOCATION_REQ
    var settingReq : LocationSettingsRequest = LocationSettingsRequest.Builder()
        .addLocationRequest(locationReq)
        .build()
    lateinit var locationSuccessObserver : (Location) -> Unit
    lateinit var addressSuccessObserver : (Address) -> Unit
    lateinit var locationErrObserver : (Exception) -> Unit
    lateinit var addressErrObserver : (Exception) -> Unit
    var method: LocationUpdateUtil.LocationMethod =
        LocationUpdateUtil.LocationMethod.GetLastLocation




    /**
     * @param context Activity: Activity.this; Fragment: getContext()
     */
    fun configure() : UtilDelegator{
        //gen GPSUtil
        gpsUtil = GpsUtil.Builder(cxt!!)
            .addObserver(gpsOnCore)
            .addSettingRequest(settingReq)
            .addLocationRequest(locationReq)
            .build()
        //gen LocationUtil
        locationUtil =
            LocationUpdateUtil(cxt!!.applicationContext)
        return this
    }

    fun start(){
        //ask Location, Fire!
        permissionCore.askLocation(cxt!!)
    }

    fun attach(context: Context) : UtilDelegator{
        permissionCore.attach()
        gpsOnCore.attach()
        cxt = context
        return this
    }

    fun release(){
        permissionCore.release()
        gpsOnCore.release()
        if(::locationSuccessObserver.isInitialized)
            locationSuccessObserver = {}
        if(::addressSuccessObserver.isInitialized)
            addressSuccessObserver = {}
        if(::locationErrObserver.isInitialized)
            locationErrObserver = {}
        if(::addressErrObserver.isInitialized)
            addressErrObserver = {}
        if(::locationUtil.isInitialized)
            locationUtil.detach()
        cxt = null
    }

    /**
     * `internal` modifier does not only use in the same module,
     * this is not for end user to modify
     *
     * @purpose get setting from xml to know use which func of locationUtil
     * note: key point
     */
    internal fun requestLocation(method: LocationUpdateUtil.LocationMethod){
        when(method){
            LocationUpdateUtil.LocationMethod.GetLocationUpdates -> {
                locationUtil.getLocationUpdates(
                    gpsUtil.locateRequest!!,
                    locationSuccessObserver,
                    locationErrObserver,
                    false
                )
            }
            LocationUpdateUtil.LocationMethod.GetAddressUpdates -> {
                locationUtil.getAddressUpdates(
                    gpsUtil.locateRequest!!,
                    addressSuccessObserver,
                    addressErrObserver,
                    false
                )
            }
            LocationUpdateUtil.LocationMethod.GetLastAddress -> {
                locationUtil.getLastAddress(
                    true, //因為GPSOn 才做doNext()
                    addressSuccessObserver,
                    addressErrObserver
                )
            }
            //LocationUpdateUtil.LocationMethod.GetLastLocation
            else -> {
                locationUtil.getLastLocation(
                    true, //因為GPSOn 才做doNext()
                    locationSuccessObserver,
                    locationErrObserver
                )
            }
        }
    }


}



