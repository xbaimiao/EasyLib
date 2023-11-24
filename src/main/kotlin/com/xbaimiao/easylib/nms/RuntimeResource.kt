package com.xbaimiao.easylib.nms

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
//@JvmRepeatable(
//    RuntimeResources::class
//)
annotation class RuntimeResource(
    val value: String,
    val hash: String,
    val name: String = "",
    val tag: String = "",
    val zip: Boolean = false
) 