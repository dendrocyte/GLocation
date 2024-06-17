package com.example.googlelocation.demo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cus.yiling.location.R
import cus.yiling.location.databinding.ActivityMainBinding
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
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNaviUtilBtn.setOnClickListener {
            startActivity(Intent(baseContext, UtilBtnActivity::class.java))
        }
        binding.btnNonViewUtil.setOnClickListener {

        }

    }


}

