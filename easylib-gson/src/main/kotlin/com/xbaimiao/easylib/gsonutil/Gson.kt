package com.xbaimiao.easylib.gsonutil

import com.google.gson.JsonElement
import com.google.gson.JsonParser

/**
 * @author 小白
 * @date 2023/5/28 11:46
 **/
fun String.parseGsonObject(): JsonElement {
    runCatching {
        return JsonParser.parseString(this)
    }
    runCatching {
        return JsonParser.parseReader(this.reader())
    }
    runCatching {
        return JsonParser().parse(this)
    }
    error("无法解析json字符串 $this")
}