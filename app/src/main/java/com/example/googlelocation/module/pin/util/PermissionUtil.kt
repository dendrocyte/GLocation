package com.example.googlelocation.module.pin.util

import android.util.Log
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.googlelocation.module.pin.core.Callback
import com.example.googlelocation.module.pin.core.IPermissionCore

/**
 * this file is created by luyiling on 2019/3/31
 * migrate to this project and re-construct
 */


/**
 * Created by luyiling on 2024/6/18
 * @utilFunction
 */
class PermissionCore(
    caller : ActivityResultCaller,
    callback: Callback
) : IPermissionCore(callback){

    override val request: ActivityResultLauncher<Array<String>> =
        caller.getRequest()
}



/**
 * Created by luyiling on 2024/2/23
 * @utilView
 */
class PermissionLifecycleObserver(
    callback: Callback
) : IPermissionCore(callback), DefaultLifecycleObserver {
    override lateinit var request : ActivityResultLauncher<Array<String>>
    private val TAG = PermissionLifecycleObserver::class.java.simpleName

    /*隨著呼叫的lifecycle: onCreate*/
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        Log.d(TAG, "onCreate: ")
        /*確認是ComponentActivity or Fragment*/
        if (owner !is ActivityResultCaller) return
        /* 要在activity, fragment宣告之前*/
        request = owner.getRequest()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d(TAG, "onStart: ")
        attach()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d(TAG, "onStop: ")
        release()
    }

}

