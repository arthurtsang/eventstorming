package com.youramaryllis.eventstorming.data

data class Event (val text: String) : DomainObject() {
    val name: String
    val properties: List<String>

    init {
        val index = text.indexOf("//")
        name = if(index>0) text.substring(0,index) else text
        properties = extractProperties(text)
    }
}