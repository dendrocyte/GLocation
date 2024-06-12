//package com.example.googlelocation.module.customView
//
//import com.example.googlelocation.module.listener.AbsLocationListener
//import com.example.googlelocation.module.util.LocationUpdateUtil
//import com.google.android.gms.location.LocationCallback
//import com.google.android.gms.location.LocationResult
// FIXME: do this btn?
///**
// * Created by luyiling on 2024/2/23
// * Modified by
// *
// * TODO:
// * Description:
// *
// * @params
// * @params
// */
///**
// * Created by luyiling on 2019-10-04
// * Modified by
// *
// * <title> </title>
// * TODO:
// * Description: wanna get the lon & lat of location
// *
// * <IMPORTANT>
// * this is the tutorial for get abs location btn
// * do these necessary
// * @params setLocationCallback(AbsLocationListener callback)
// * @params AbsLocationBtn setStopEnv(int spec)
// * </IMPORTANT>
// */
//class AbsLocationBtn  : LocationBtn() {
//    private val TAG = AbsLocationBtn::class.java.simpleName
//    private var listener: AbsLocationListener? = null
//
//
//    /**
//     * do it necessary at your UI layer
//     * @param callback
//     */
//    fun setLocationCallback(callback: AbsLocationListener?) {
//        listener = callback
//    }
//
//    val STOP_AFTER_GOT = 2
//    val NOT_STOP = 3
//    private var stop_spec = NOT_STOP //default not_stop
//
//
//    /**
//     * do it necessary at your UI layer
//     * 2 choice: wanna non_stop or stop after got
//     * @param spec STOP_AFTER_GOT | NOT_STOP
//     * @return AbsLocationBtn
//     */
//    fun setStopEnv(spec: Int): AbsLocationBtn? {
//        stop_spec = spec
//        return this
//    }
//
//
//    override fun startLocationUpdate() {
//        LocationUpdateUtil
//            .getInstance()
//            .startLocationUpdates(gpsUtil.locateRequest, locationCallback)
//    }
//
//    private val locationCallback: LocationCallback = object : LocationCallback() {
//        override fun onLocationResult(locationResult: LocationResult) {
//            if (stop_spec == STOP_AFTER_GOT && locationResult.lastLocation != null) {
//                LocationUpdateUtil.getInstance().stopLocationUpdates()
//            }
//            listener!!.onAbsResult(locationResult)
//        }
//    }
//
//    override fun setOnAddressListener() {
//        /**
//         * not to do this
//         * clear the super.setOnAddressListener
//         */
//    }
//}