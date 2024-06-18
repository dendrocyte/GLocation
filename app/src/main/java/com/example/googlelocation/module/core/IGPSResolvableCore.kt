package com.example.googlelocation.module.core

import android.app.Activity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.common.api.ResolvableApiException

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
abstract class IGPSResolvableCore(val listener: OnGpsListener){
    abstract val request : ActivityResultLauncher<IntentSenderRequest>
    var _listener : OnGpsListener? = null

    fun ActivityResultCaller.getRequest() : ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            _listener?.gpsStatus(result.resultCode == Activity.RESULT_OK)
        }

    fun resolve(e: ResolvableApiException){
        request.launch(IntentSenderRequest.Builder(e.resolution).build())
    }

    fun attach() { _listener = listener }
    fun release() { _listener = null }
}

fun interface OnGpsListener {
    fun gpsStatus(isGPSEnable: Boolean)
}