package com.example.googlelocation.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cus.yiling.location.R
import cus.yiling.location.databinding.ActivityUtilBinding

/**
 * Created by luyiling on 2024/2/26
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
/**
 * permission module test and location module test
 * function check done
 *
 * assume location btn will exist all the time in this app:
 * make a customize view
 * <IMPORTANT>
 * 3 method:
 * make permission, gps, and location check
 * 1. in activity
 * 2. in fragment: use fragment module to transform modules (ex: permission, gps, and location)
 * 3. in customized btn: use 3rd lib, listen lifecycle and activityResult method
</IMPORTANT> *
 */
class UtilBtnActivity : AppCompatActivity() {
    var location: TextView? = null

    private lateinit var binding : ActivityUtilBinding
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUtilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.iBBack.setOnClickListener {
            finish()
        }

        /* method : permission invoked in fragment*/
        supportFragmentManager.beginTransaction()
            .add(R.id.container, FragBlank())
            .addToBackStack(null).commit()
    }


}

