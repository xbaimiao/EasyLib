package com.xbaimiao.easylib.database.util

import com.xbaimiao.easylib.util.parseJson

/**
 * DatabaseMap
 *
 * @author xbaimiao
 * @since 2023/10/27 14:38
 */
class DatabaseMap(private var content: String, val onchange: (String) -> Unit) : HashMap<String, String>() {

    constructor(onchange: (String) -> Unit) : this("{}", onchange)

    init {
        val json = content.parseJson().asJsonObject
        super.putAll(json.keySet().associateWith { json[it].asString })
    }

    override fun put(key: String, value: String): String? {
        val json = content.parseJson().asJsonObject
        json.addProperty(key, value)
        content = json.toString()
        onchange(content)
        return super.put(key, value)
    }

    override fun putAll(from: Map<out String, String>) {
        val json = content.parseJson().asJsonObject
        from.forEach { (key, value) ->
            json.addProperty(key, value)
        }
        content = json.toString()
        onchange(content)
        super.putAll(from)
    }

    override fun remove(key: String): String? {
        val json = content.parseJson().asJsonObject
        json.remove(key)
        content = json.toString()
        onchange(content)
        return super.remove(key)
    }

    override fun clear() {
        content = "{}"
        onchange(content)
        super.clear()
    }

}
