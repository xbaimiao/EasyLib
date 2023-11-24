package com.xbaimiao.easylib.nms

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RuntimeResources(vararg val value: RuntimeResource)
