package com.youramaryllis.eventstorming.data

data class ElseBlock(val sourceText: String) : Block() {
    val comment: String = sourceText.removePrefix("~").trim()
    var beginBlock: BeginBlock? = null
    fun associateBeginElseBlock(stack: ArrayDeque<Block>) {
        beginBlock = stack.findLast { it is BeginBlock && it.endBlock == null } as BeginBlock?
        beginBlock?.elseBlocks?.add(this)
    }
}
