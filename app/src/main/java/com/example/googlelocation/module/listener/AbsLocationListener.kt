package com.example.googlelocation.module.listener

import com.google.android.gms.location.LocationResult

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
fun interface AbsLocationListener {
    fun onAbsResult(result: LocationResult?)
}