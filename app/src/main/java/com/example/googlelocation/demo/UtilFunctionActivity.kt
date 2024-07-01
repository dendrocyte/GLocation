package com.example.googlelocation.demo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.googlelocation.module.man.UtilDelegator
import com.example.googlelocation.module.pin.util.LocationUpdateUtil
import cus.yiling.location.databinding.ActivityUtilFunctionBinding

/**
 * Created by luyiling on 2024/2/26
 * Modified by
 *
 * TODO:
 * Description: if you would not like to use util function via view, you can use UtilDelegator
 *
 * @params
 * @params
 */
/**
 */
class UtilFunctionActivity : AppCompatActivity() {
    /**
     * declare delegator before onCreate()
     * let ActivityResultLauncher register first
     */
    private val delegator = UtilDelegator(this)
    private val TAG = UtilFunctionActivity::class.java.simpleName
    private lateinit var binding : ActivityUtilFunctionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUtilFunctionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.iBBack.setOnClickListener { finish() }
    }

    override fun onStart() {
        super.onStart()
        with(delegator){
            method = LocationUpdateUtil.LocationMethod.GetLastLocation
            locationSuccessObserver = { location ->
                binding.tVresult.text = "(${location.longitude}, ${location.latitude})"
            }
            locationErrObserver = { e ->
                Log.e(TAG, "LocationError: $e")
                binding.tVresult.text = "Loading Failed"
            }
            attach(baseContext).configure().start()
        }

    }

    override fun onStop() {
        super.onStop()
        delegator.release()
    }

}

