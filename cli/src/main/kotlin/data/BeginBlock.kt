package com.youramaryllis.eventstorming.data

data class BeginBlock (val sourceText: String) : Block() {
    val comment: String = sourceText.removePrefix("[").trim()
    var endBlock: EndBlock? = null
        get() { return field }
        set(value) { field = value }
    var elseBlocks: MutableList<ElseBlock> = ArrayList()
        get() { return  field }
        set(value) { field = value }
}
