package com.ckj.fraktonjobapplicationexample.base.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ckj.fraktonjobapplicationexample.base.BaseInitializations

abstract class BaseActivity : AppCompatActivity(), BaseInitializations {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBaseFunctions()
    }
}