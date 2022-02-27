package com.youramaryllis.eventstorming.data

data class EndBlock(val sourceText: String) : Block() {
    val comment: String = sourceText.removePrefix("]").trim()
    var beginBlock: BeginBlock? = null
        get() { return field }
        set(value) { field = value }
    fun associateBeginEndBlock(stack: ArrayDeque<Block>) {
        beginBlock = stack.findLast { it is BeginBlock && it.endBlock == null } as BeginBlock?
        beginBlock?.endBlock = this
    }
}
