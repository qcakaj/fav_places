package com.ckj.fraktonjobapplicationexample.base

interface BaseInitializations {
    fun initView()
    fun observeViewModel()


    fun initBaseFunctions() {
        initView()
        observeViewModel()
    }
}