package com.youramaryllis.eventstorming.plantuml

import com.youramaryllis.eventstorming.data.*
import h.block
import net.sourceforge.plantuml.SourceStringReader
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.nio.file.Path
import java.util.stream.IntStream

class PlantUML (domainObjects : List<DomainObject>){
    val stringBuilder: StringBuilder = StringBuilder()
    init {
        stringBuilder.appendLine("@startuml")
            .appendLine( "skinparam componentStyle rectangle")
            .appendLine("skinparam rectangle {")
            .appendLine("\tbackgroundColor<<event>> Orange")
            .appendLine("\tbackgroundColor<<command>> LightBlue")
            .appendLine("\tbackgroundColor<<external>> Pink")
            .appendLine("\tbackgroundColor<<document>> Green")
            .appendLine("\tbackgroundColor<<invariant>> Yellow")
            .appendLine("}")

        IntStream.range(0,domainObjects.size).forEach{ domainObjects.get(it).index = it  }
        var lastCommand: DomainObject? = null
        var lastDomainObject: DomainObject? = null
        var events = ArrayList<DomainObject>()
        var blockStack = ArrayDeque<Block>()
        for( dobj in domainObjects ) {
            when ( dobj ) {
                is Command, is External -> {
                    when( lastDomainObject ) {
                        is BeginBlock -> {
                            lastDomainObject.nextCommand.add(dobj)
                            dobj.prevDomainObject = lastCommand
                            lastCommand = dobj
                        }
                        is ElseBlock -> {
                            val beginBlock = lastDomainObject.beginBlock
                            beginBlock!!.nextCommand?.add(dobj)
                            lastDomainObject.nextCommand.add(dobj)
                            dobj.prevDomainObject = null
                            (dobj as Command).belowCommand = beginBlock!!.nextCommand?.first()
                            lastCommand = dobj
                        }
                        is EndBlock -> {
                            val elseBlock = lastDomainObject.beginBlock!!.elseBlocks[0]
                            //val elseBlock = blockStack.findLast { it is ElseBlock }
                            dobj.prevDomainObject = elseBlock!!.prevCommand.first()
                            lastDomainObject.nextCommand.add(dobj)
                            blockStack.findLast { it is EndBlock && it.nextCommand.isEmpty()  }?.nextCommand?.add(dobj) // if a block is closed and another block is opened right away, we'll need to set the next command to this command
                            lastCommand = dobj
                        }
                        else -> {
                            dobj.prevDomainObject = lastCommand
                            lastCommand = dobj
                        }
                    }
                    // process the current events list
                    stringBuilder.append( processDomainObjects(events) )
                    events.clear()
                }
                is Event -> {
                    dobj.prevDomainObject = if( lastDomainObject is Comment || lastDomainObject is Invariant ) lastDomainObject.prevDomainObject else lastDomainObject
                }
                is Comment -> {
                    if (lastDomainObject is Comment)
                        lastDomainObject.text += "\n" + dobj.text
                    else {
                        dobj.prevDomainObject = lastDomainObject
                    }
                }
                is Document -> {
                    dobj.prevDomainObject = lastDomainObject
                }
                is Invariant -> {
                    dobj.prevDomainObject = lastDomainObject
                }
                is BeginBlock -> {
                    (dobj as Block).prevCommand.add(lastCommand!!)
                    dobj.prevDomainObject = lastDomainObject
                    blockStack.addLast(dobj)
                }
                is ElseBlock -> {
                    (dobj as Block).prevCommand.add(lastCommand!!)
                    dobj.prevDomainObject = lastDomainObject
                    blockStack.addLast(dobj)
                    dobj.associateBeginElseBlock(blockStack)
                }
                is EndBlock -> {
                    dobj.associateBeginEndBlock(blockStack)
                    var beginBlock = dobj.beginBlock
                    beginBlock!!.elseBlocks.forEach { dobj.prevCommand.addAll( it.prevCommand ) }
                    dobj.prevCommand.add(lastCommand!!)
                    blockStack.addLast(dobj)
                }
            }
            when( dobj ) {
                is Block -> {
                    lastDomainObject = dobj // blocks are printed when the outermost block are completed and in the next command domain object.  so that the endblock has info on where the next command is
                    dobj.prevDomainObject?.nextDomainObject = dobj
                }
                is Comment -> {
                    if( dobj.prevDomainObject != null ) {// this only happens with prevDomainObject and current dobj is both a comment
                        lastDomainObject = dobj
                        dobj.prevDomainObject?.nextDomainObject = dobj
                        events.add(dobj)

                    }
                }
                else -> {
                    lastDomainObject = dobj
                    dobj.prevDomainObject?.nextDomainObject = dobj
                    events.add(dobj)
                }
            }
        }
        // process the last events list
        stringBuilder.append(processDomainObjects(events))
        stringBuilder.append(processBlocks(blockStack))
        stringBuilder.appendLine("@enduml")
    }

    fun getPlantUMLComment( comment: String ): String {
        return when (comment.trim()) {
            null, "" -> { "" }
            else -> { ": ${comment}" }
        }
    }

    fun processBlocks( blockStack: ArrayDeque<Block> ): StringBuilder {
        val sb = StringBuilder()
        blockStack.forEach {
            when( it ) {
                is BeginBlock -> {
                    val prevCommand = it.prevCommand.first()
                    val comment = when (it.comment.trim()) {
                        null, "" -> { "" }
                        else -> { ": ${it.comment}" }
                    }
                    sb.appendLine( "C${prevCommand.index} -[#Blue]> C${it.nextCommand[0].index}${getPlantUMLComment(it.comment)}")
                    it.elseBlocks.forEach {
                        sb.appendLine( "C${prevCommand.index} -[#Blue]> C${it.nextCommand[0].index}${getPlantUMLComment(it.comment)}")
                    }
                }
                is EndBlock -> {
                    val nextCommand = it.nextCommand.first()
                    val comment = getPlantUMLComment(it.comment)
                    sb.appendLine( "C${it.prevCommand[0].index} -[#Blue]> C${nextCommand.index}${comment}" )
                    it.prevCommand.removeAt(0)
                    it.prevCommand.forEach {
                        sb.appendLine( "C${it.index} -[#Blue]up-> C${nextCommand.index}${comment}")
                    }
                }
            }
        }
        return sb
    }

    fun processDomainObjects(domainObjects: List<DomainObject> ): StringBuilder {
        val sb = StringBuilder()
        for( dobj in domainObjects ) {
            sb.append( processComponent(dobj) )
        }
        return sb
    }

    fun processComponent( dobj: DomainObject ): StringBuilder {
        val sb = StringBuilder()
        when( dobj ) {
            is Command -> {
                sb.appendLine("component C${dobj.index} <<command>> [")
                sb.appendLine(parseName(dobj.name))
                if( dobj.properties.size > 0 ) {
                    sb.appendLine("---")
                    for( prop in dobj.properties ) sb.appendLine("- ${prop}")
                }
                sb.appendLine("]")
                if (dobj.prevDomainObject != null)
                    sb.appendLine("C${dobj.prevDomainObject?.index} -[hidden]right-> C${dobj.index}")
                if (dobj.belowCommand != null ) {
                    var lowestDomainObject: DomainObject = dobj.belowCommand!!
                    while ( lowestDomainObject != null ) {
                        lowestDomainObject = lowestDomainObject.nextDomainObject!!
                        if(  lowestDomainObject.nextDomainObject is Block  || lowestDomainObject.nextDomainObject is Command ||  lowestDomainObject.nextDomainObject is External) break;
                    }
                    //sb.appendLine( "C${dobj.belowCommand?.index} -----[hidden]-> C${dobj.index}" )
                    sb.appendLine( "C${lowestDomainObject?.index} -----[hidden]-> C${dobj.index}" )
                }
            }
            is External -> {
                sb.appendLine("component C${dobj.index} <<external>> [")
                sb.appendLine(parseName(dobj.name))
                if( dobj.properties.size > 0 ) {
                    sb.appendLine("---")
                    for( prop in dobj.properties ) sb.appendLine("- ${prop}")
                }
                sb.appendLine("]")
                if (dobj.prevDomainObject != null)
                    sb.appendLine("C${dobj.prevDomainObject?.index} -[hidden]right-> C${dobj.index}")
            }
            is Event -> {
                sb.appendLine("component C${dobj.index} <<event>> [")
                sb.appendLine(dobj.name)
                if( dobj.properties.size > 0 ) {
                    sb.appendLine("---")
                    for( prop in dobj.properties ) sb.appendln( "- ${prop}")
                }
                sb.appendLine("]")
                when( dobj.prevDomainObject ) {
                    is Event -> {
                        sb.appendLine("C${dobj.prevDomainObject?.index}  .[#green,thickness=3]> C${dobj.index}")
                    }
                    is Command -> {
                        sb.appendLine("C${dobj.prevDomainObject?.index} .[#green,thickness=3].> C${dobj.index}")
                    }
                    is External -> {
                        sb.appendLine("C${dobj.prevDomainObject?.index} .[#green,thickness=3].> C${dobj.index}")
                    }
                }
            }
            is Comment -> {
                when( dobj.prevDomainObject ) {
                    is Event, is Document -> {
                        sb.appendLine("note bottom of C${dobj.prevDomainObject?.index}")
                        sb.appendLine(dobj.text)
                        sb.appendLine("end note")
                    }
                    is Command -> {
                        sb.appendLine("note top of C${dobj.prevDomainObject?.index}")
                        sb.appendLine(dobj.text)
                        sb.appendLine("end note")

                    }
                }
            }
            is Document -> {
                sb.appendLine("component C${dobj.index} <<document>> [")
                sb.appendLine(dobj.name)
                if( dobj.properties.size > 0 ) {
                    sb.appendLine("---")
                    for( prop in dobj.properties ) sb.appendln( "- ${prop}")
                }
                sb.appendLine("]")
                when( dobj.prevDomainObject ) {
                    is Comment -> {
                        sb.appendLine("C${dobj.prevDomainObject?.prevDomainObject?.index} .[#green,thickness=3].> C${dobj.index}")
                    }
                    else -> sb.appendLine("C${dobj.prevDomainObject?.index} .[#green,thickness=3].> C${dobj.index}")
                }
            }
            is Invariant -> {
                sb.appendLine( "component C${dobj.index} <<invariant>> [ ")
                sb.appendLine(dobj.text)
                sb.appendLine("]")
                when( dobj.prevDomainObject ) {
                    is Event -> {
                        sb.appendLine("C${dobj.prevDomainObject?.index} .[#green,thickness=3].> C${dobj.index}")
                    }
                    else -> throw RuntimeException("Invariant must belongs to an Event")
                }
            }
        }
        return  sb
    }

    fun toPNG(pngPath: Path? = null) : OutputStream {
        val output = if( pngPath != null ) FileOutputStream(pngPath.toFile()).buffered() else ByteArrayOutputStream(2*1024*1024 )
        output.use {
                SourceStringReader(stringBuilder.toString()).generateImage(it)
        }
        return output
    }

    fun parseName(name: String): String {
        val actorRange = IntRange(name.indexOf('('), name.indexOf(')'))
        val actor = if( actorRange.contains(-1)) "" else "\n---\n<&person> ${name.substring(actorRange).trim('(', ')', ' ')}"
        val nameNoActor = if( actorRange.contains(-1)) name else name.removeRange(actorRange)
        val ticketRange = IntRange(nameNoActor.indexOf('['), nameNoActor.indexOf(']'))
        val ticket = if(ticketRange.contains(-1)) "" else "\n---\n<&task> ${nameNoActor.substring(ticketRange).trim('[',']', ' ')}"
        val nameCleaned = if( ticketRange.contains(-1) ) nameNoActor else nameNoActor.removeRange(ticketRange)
        return "${nameCleaned.trim()}$actor$ticket"
    }
}
