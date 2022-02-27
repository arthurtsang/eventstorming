package com.youramaryllis.eventstorming.data

open class Block : DomainObject() {
    var prevCommand: MutableList<DomainObject> = ArrayList()
        get() = field
        set(value) { field = value }
    var nextCommand: MutableList<DomainObject> = ArrayList()
        get() = field
        set(value) { field = value }
}