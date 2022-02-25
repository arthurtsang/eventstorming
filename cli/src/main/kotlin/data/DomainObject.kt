package com.youramaryllis.eventstorming.data

import java.util.stream.Collectors


open class DomainObject {
    var index: Int = -1
    var prevDomainObject: DomainObject? = null

    companion object {
        fun parse(text: String): DomainObject {
            if (text.indexOf("->") > 0) {
                return Command(text)
            }
            if (text.indexOf("-*") > 0) {
                return External(text)
            }
            if (text.startsWith("#")) {
                return Comment(text)
            }
            if (text.indexOf("*") > 0) {
                return Document(text)
            }
            if (text.startsWith( "!")) {
                return Invariant(text)
            }
            return Event(text)
        }
    }

    fun extractProperties(text: String): List<String> {
        val propertiesIndex = text.indexOf("//")
        return if (propertiesIndex != -1) {
            val propertiesText = text.substring(propertiesIndex + 2)
            propertiesText.split(",").stream().map { it.trim() }.collect(Collectors.toList())
        } else {
            ArrayList()
        }
    }

}