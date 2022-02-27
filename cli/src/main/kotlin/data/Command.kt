package com.youramaryllis.eventstorming.data

data class Command (val text: String): DomainObject(){
    val name: String
    val properties: List<String>
    var belowCommand: DomainObject? = null

    init {
        val index = text.indexOf("->" )
        name = text.substring(0,index).trim()
        properties = extractProperties(text)
    }

}