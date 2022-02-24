package com.youramaryllis.eventstorming.data

data class Comment (val sourcetext: String) : DomainObject() {
    var text: String = sourcetext.removePrefix("#").trim()
}