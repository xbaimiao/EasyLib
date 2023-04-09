package com.xbaimiao.easylib.module.config

import com.xbaimiao.easylib.module.chat.BuiltInConfiguration
import java.io.File

/**
 * @author 小白
 * @date 2023/4/9 22:39
 **/
abstract class BuiltInAutoFileConfiguration(
    val builtInConfiguration: BuiltInConfiguration
) : AutoFileConfiguration {

    override val file: File by lazy { builtInConfiguration.file }

}