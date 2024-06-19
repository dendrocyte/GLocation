package com.example.googlelocation.module.pin

import android.app.Service
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.ResultReceiver
import android.text.TextUtils
import android.util.Log
import com.example.googlelocation.Constants
import cus.yiling.location.R
import java.io.IOException
import java.util.Locale

/**
 * Created by luyiling on 2024/2/26
 * Modified by
 * 使否要改成reverse geocoding 比較reliable
 * 因為google 包沒有下載，要重開機才有用能用
 * https://developers.google.com/maps/documentation/geocoding/requests-reverse-geocoding?hl=zh-tw#reverse-requests
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
/**
 * Created by luyiling on 2019/4/6
 *
 *
 * TODO:
 * The getFromLocation() method provided by the Geocoder class accepts a latitude and longitude
 * and returns a list of addresses.
 * The method is synchronous and may take a long time to do its work,
 * so you should not call it from the main, user interface (UI) thread of your app.
 * <IMPORTANT></IMPORTANT>
 */
class ParseLocationService : Service() {
    private var errorMessage = ""

    private val TAG = ParseLocationService::class.java.simpleName
    private lateinit var handlerThread : HandlerThread
    private lateinit var handler : Handler
    /*reverse geocoding*/
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Get the location passed to this service through an extra.
        val location: Location = intent.getParcelableExtra(
            Constants.LOCATION_DATA_EXTRA
        ) ?: throw Exception("Miss param: Location")
        val receiver : ResultReceiver = intent.getParcelableExtra(
            Constants.RECEIVER
        ) ?: throw Exception("Miss param: ResultReceiver")

        var addresses: List<Address>? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val geocoder = Geocoder(baseContext, Locale.getDefault())
                geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1// In this sample, get just a single address.
                ) { result -> addresses = result }
            }else {
                handlerThread = HandlerThread("Get address")
                handlerThread.start()
                handler = Handler(handlerThread.looper)
                handler.post{
                    val geocoder = Geocoder(baseContext)
                    addresses = geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    ) // In this sample, get just a single address.
                }
                while(addresses == null){
                    Thread.sleep(1000)
                }
            }
            handler.removeCallbacksAndMessages(null)
            handlerThread.quit()

        } catch (nullPointerException: NullPointerException) { // location is null.
            errorMessage = getString(R.string.no_location_data_provided)
            Log.e(TAG, errorMessage, nullPointerException)
        } catch (ioException: IOException) { // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_not_available)
            Log.e(TAG, errorMessage, ioException)
        } catch (illegalArgumentException: IllegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used)
            Log.e(
                TAG, errorMessage + ". " +
                        "Latitude = " + location!!.latitude +
                        ", Longitude = " +
                        location.longitude, illegalArgumentException
            )
        }



        // Handle case where no address was found.
        var code : Int
        var msg : String
        var addr : Address?
        if (addresses.isNullOrEmpty()) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found)
                Log.e(TAG, errorMessage)
            }
            code = Constants.FAILURE_RESULT
            msg = errorMessage
            addr = null
        } else {
            val address = addresses!![0]
            val addressFragments = ArrayList<String?>()

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for (i in 0..address.maxAddressLineIndex) {
                Log.e(TAG, "whole address: " + address.getAddressLine(i))
                Log.e(TAG, "dist: " + address.subAdminArea)
                Log.e(TAG, "city: " + address.adminArea)
                Log.e(TAG, "country: " + address.countryName)
                Log.e(TAG, "unknown: " + address.premises)
                Log.e(TAG, "unknown: " + address.subLocality)
                addressFragments.add(address.adminArea) //只存city
            }
            Log.i(TAG, getString(R.string.address_found))
            Log.i(TAG, "$addressFragments")
            code = Constants.SUCCESS_RESULT
            msg = TextUtils.join(
                    System.getProperty("line.separator"),
                    addressFragments
                )
            Log.i(TAG, "$msg")
            addr = address
        }
        // Deliver to receiver
        val bundle = Bundle()
        bundle.putString(Constants.RESULT_DATA_MSG, msg)
        bundle.putParcelable(Constants.RESULT_DATA_OBJ, addr)
        receiver.send(code, bundle)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun stopService(name: Intent?): Boolean {
        if (::handlerThread.isInitialized)
            handlerThread.quit()
        if (::handler.isInitialized)
            handler.removeCallbacksAndMessages(null)
        return super.stopService(name)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

