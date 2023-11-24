package com.xbaimiao.easylib.database.util

import com.xbaimiao.easylib.util.parseJson

/**
 * DatabaseSet
 *
 * @author xbaimiao
 * @since 2023/10/27 14:36
 */
class DatabaseSet(private var content: String, val onchange: (String) -> Unit) : HashSet<String>() {

    constructor(onchange: (String) -> Unit) : this("[]", onchange)

    init {
        super.addAll(content.parseJson().asJsonArray.map { it.asString })
    }

    override fun addAll(elements: Collection<String>): Boolean {
        val json = content.parseJson().asJsonArray
        elements.forEach { s -> json.add(s) }
        content = json.toString()
        onchange(content)
        return super.addAll(elements)
    }

    override fun add(element: String): Boolean {
        val json = content.parseJson().asJsonArray
        json.add(element)
        content = json.toString()
        onchange(content)
        return super.add(element)
    }

    override fun clear() {
        content = "[]"
        onchange(content)
        super.clear()
    }

    override fun remove(element: String): Boolean {
        val json = content.parseJson().asJsonArray
        json.removeAll {
            it.asString == element
        }
        content = json.toString()
        onchange(content)
        return super.remove(element)
    }

}
