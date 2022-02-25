package com.youramaryllis.eventstorming.data

data class Invariant(val sourcetext: String) : DomainObject() {
    var text: String = sourcetext.removePrefix("!").trim()
}