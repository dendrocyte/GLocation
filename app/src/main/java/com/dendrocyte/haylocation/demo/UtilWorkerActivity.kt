package com.dendrocyte.haylocation.demo

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.WorkManager
import com.dendrocyte.haylocation.module.pin.core.Callback
import com.dendrocyte.haylocation.module.pin.util.GPSResovableCore
import com.dendrocyte.haylocation.module.pin.util.GpsUtil
import com.dendrocyte.haylocation.module.pin.util.PermissionCore
import cus.yiling.location.databinding.ActivityUtilWorkerBinding

/**
 * Created by luyiling on 2024/2/26
 * Modified by
 *
 * TODO:
 * Description: worker mode, do not need to care the result at foreground
 *
 * @params
 * @params
 */
/**
 */
class UtilWorkerActivity : AppCompatActivity() {

    private val TAG = UtilWorkerActivity::class.java.simpleName
    private lateinit var binding : ActivityUtilWorkerBinding


    private val permissionCore = PermissionCore(this, object: Callback {
        override fun onGranted() {
            GpsUtil.Builder(baseContext)
                .addObserver(gpsCore)
                .build()
                .exeGPSOn()
        }
        override fun onDenied(denyPermission: Map<String, Boolean>) {
            binding.tVresult.text = "Permissions are denied"
        }
    })
    private val gpsCore = GPSResovableCore(this) { on ->
        exeLocationWorker(on)
        if(!on) binding.tVresult.text = "GPS is not changed to on"
    }

    @SuppressLint("MissingPermission")
    private fun exeLocationWorker(isGpsOn: Boolean){
        if(!isGpsOn) return
        WorkManager.getInstance(baseContext)
            .beginWith(gpsRequest)
            .enqueue()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUtilWorkerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.iBBack.setOnClickListener { finish() }

        /**
         * this is for demo check.
         * Use worker mode, you should not care the result at foreground
         */
        WorkManager.getInstance(baseContext)
            .getWorkInfoByIdLiveData(gpsRequest.id)
            .observe(this){
                val result =
                    if(it.state.isFinished) "Finished"
                    else "Not yet"
                binding.tVresult.text = "$result"
            }

    }

    override fun onStart() {
        super.onStart()
        permissionCore.attach()
        gpsCore.attach()
        //Fire!
        permissionCore.askLocation(baseContext)
    }

    override fun onStop() {
        super.onStop()
        permissionCore.release()
        gpsCore.release()
    }

}

