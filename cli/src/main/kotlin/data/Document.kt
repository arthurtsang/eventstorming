package com.youramaryllis.eventstorming.data

data class Document (val text: String) : DomainObject() {
    val name: String
    val properties: List<String>
    init {
        val index = text.indexOf("*")
        name = text.substring(0,index).trim()
        properties = extractProperties(text)
    }
}