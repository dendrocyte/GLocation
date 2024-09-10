package com.dendrocyte.haylocation.module.pin.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Created by luyiling on 2024/6/18
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
interface Callback {
    fun onGranted()
    fun onDenied(denyPermission: Map<String, Boolean>)
}

abstract class IPermissionCore(val callback: Callback){
    abstract val request : ActivityResultLauncher<Array<String>>

    internal fun ActivityResultCaller.getRequest() : ActivityResultLauncher<Array<String>> {
        return registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.values.contains(false))
                _callback?.onDenied(permissions)
            else
                _callback?.onGranted()
        }
    }
    private var _callback : Callback? = null

    /**
     * @param context Activity: Activity.this; Fragment: getContext()
     */
    fun askLocation(context: Context) {
        if(ContextCompat.checkSelfPermission(
                context,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ).toString()
            ) == PackageManager.PERMISSION_GRANTED){
            _callback?.onGranted()
        }else {
            request.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    fun attach() { _callback = callback }
    fun release() { _callback = null }
}
