package com.xbaimiao.easylib.module.utils

import com.xbaimiao.easylib.EasyPlugin

interface Module<T : EasyPlugin> {

    fun load(plugin: T) {

    }

    fun enable(plugin: T){

    }

    fun disable(plugin: T){

    }

}