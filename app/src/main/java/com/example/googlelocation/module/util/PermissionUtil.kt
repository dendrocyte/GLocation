package com.example.googlelocation.module.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

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
 */

interface Callback {
    fun onGranted()
    fun onDenied(denyPermission: Map<String, Boolean>)
}

class PermissionLifecycleObserver(
    private var callback: Callback
) : DefaultLifecycleObserver {
    private lateinit var permissionRequest : ActivityResultLauncher<Array<String>>
    private var _callback : Callback? = null
    private val TAG = PermissionLifecycleObserver::class.java.simpleName

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
            permissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    /*隨著呼叫的lifecycle: onCreate*/
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        Log.d(TAG, "onCreate: ")
        /*確認是ComponentActivity or Fragment*/
        if (owner !is ActivityResultCaller) return
        /* 要在activity, fragment宣告之前*/
        permissionRequest = owner.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.values.contains(false)) {
                return@registerForActivityResult callback.onDenied(permissions)
            }
            callback.onGranted()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d(TAG, "onStart: ")
        _callback = callback
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d(TAG, "onStop: ")
        _callback = null
    }

}

