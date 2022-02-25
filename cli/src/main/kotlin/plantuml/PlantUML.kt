package com.youramaryllis.eventstorming.plantuml

import com.youramaryllis.eventstorming.data.*
import net.sourceforge.plantuml.SourceStringReader
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.RuntimeException
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
        for( dobj in domainObjects ) {
            when ( dobj ) {
                is Command, is External -> {
                    // remember the previous command for hidden arrow so it stay on the same line
                    dobj.prevDomainObject= lastCommand
                    lastCommand = dobj
                    // process the current events list
                    stringBuilder.append(
                            processDomainObjects(events)
                    )
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
            }
            if( !(dobj is Comment && dobj.prevDomainObject == null) ) { //the only time it's null is when the prev dobj is also a comment
                lastDomainObject = dobj
                events.add(dobj)
            }
        }
        // process the last events list
        stringBuilder.append(processDomainObjects(events))
        stringBuilder.appendLine("@enduml")
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
                    for( prop in dobj.properties ) sb.appendln( "- ${prop}")
                }
                sb.appendLine("]")
                if (dobj.prevDomainObject != null)
                    sb.appendLine("C${dobj.prevDomainObject?.index} -[hidden]right-> C${dobj.index}")
            }
            is External -> {
                sb.appendLine("component C${dobj.index} <<external>> [")
                sb.appendLine(parseName(dobj.name))
                if( dobj.properties.size > 0 ) {
                    sb.appendLine("---")
                    for( prop in dobj.properties ) sb.appendln( "- ${prop}")
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
                        sb.appendLine("C${dobj.prevDomainObject?.index} -> C${dobj.index}")
                    }
                    is Command -> {
                        sb.appendLine("C${dobj.prevDomainObject?.index} --> C${dobj.index}")
                    }
                    is External -> {
                        sb.appendLine("C${dobj.prevDomainObject?.index} --> C${dobj.index}")
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
                        sb.appendLine("C${dobj.prevDomainObject?.prevDomainObject?.index} -[hidden]-> C${dobj.index}")
                    }
                    else -> sb.appendLine("C${dobj.prevDomainObject?.index} -[hidden]-> C${dobj.index}")
                }
            }
            is Invariant -> {
                sb.appendLine( "component C${dobj.index} <<invariant>> [ ")
                sb.appendLine(dobj.text)
                sb.appendLine("]")
                when( dobj.prevDomainObject ) {
                    is Event -> {
                        sb.appendLine("C${dobj.prevDomainObject?.index} -[hidden]-> C${dobj.index}")
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
