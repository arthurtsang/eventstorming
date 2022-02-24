package com.youramaryllis.eventstorming.plantuml

import com.youramaryllis.eventstorming.data.*
import net.sourceforge.plantuml.SourceStringReader
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.file.Path
import java.util.stream.IntStream

class PlantUML (domainObjects : List<DomainObject>){
    val stringBuilder: StringBuilder = StringBuilder()
    init {
        stringBuilder.appendln("@startuml")
                .appendln("skinparam component {" )
                .appendln( "\tbackgroundColor<<event>> Orange" )
                .appendln( "\tbackgroundColor<<command>> LightBlue" )
                .appendln( "\tbackgroundColor<<external>> Pink" )
                .appendln( "\tbackgroundColor<<document>> Green" )
                .appendln( "}" )

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
                    dobj.prevDomainObject = if( lastDomainObject is Comment ) lastDomainObject.prevDomainObject else lastDomainObject
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
            }
            if( !(dobj is Comment && dobj.prevDomainObject == null) ) { //the only time it's null is when the prev dobj is also a comment
                lastDomainObject = dobj
                events.add(dobj)
            }
        }
        // process the last events list
        stringBuilder.append(processDomainObjects(events))
        stringBuilder.appendln( "@enduml" )
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
                sb.appendln("component C${dobj.index} <<command>> [")
                sb.appendln(parseName(dobj.name))
                if( dobj.properties.size > 0 ) {
                    sb.appendln( "---" )
                    for( prop in dobj.properties ) sb.appendln( "- ${prop}")
                }
                sb.appendln("]")
                if (dobj.prevDomainObject != null)
                    sb.appendln("C${dobj.prevDomainObject?.index} -[hidden]right-> C${dobj.index}")
            }
            is External -> {
                sb.appendln("component C${dobj.index} <<external>> [")
                sb.appendln(parseName(dobj.name))
                if( dobj.properties.size > 0 ) {
                    sb.appendln( "---" )
                    for( prop in dobj.properties ) sb.appendln( "- ${prop}")
                }
                sb.appendln("]")
                if (dobj.prevDomainObject != null)
                    sb.appendln("C${dobj.prevDomainObject?.index} -[hidden]right-> C${dobj.index}")
            }
            is Event -> {
                sb.appendln("component C${dobj.index} <<event>> [")
                sb.appendln(dobj.name)
                if( dobj.properties.size > 0 ) {
                    sb.appendln( "---" )
                    for( prop in dobj.properties ) sb.appendln( "- ${prop}")
                }
                sb.appendln("]")
                when( dobj.prevDomainObject ) {
                    is Event -> {
                        sb.appendln("C${dobj.prevDomainObject?.index} -> C${dobj.index}")
                    }
                    is Command -> {
                        sb.appendln("C${dobj.prevDomainObject?.index} --> C${dobj.index}")
                    }
                    is External -> {
                        sb.appendln("C${dobj.prevDomainObject?.index} --> C${dobj.index}")
                    }
                }
            }
            is Comment -> {
                when( dobj.prevDomainObject ) {
                    is Event, is Document -> {
                        sb.appendln("note bottom of C${dobj.prevDomainObject?.index}")
                        sb.appendln(dobj.text)
                        sb.appendln("end note")
                    }
                    is Command -> {
                        sb.appendln("note top of C${dobj.prevDomainObject?.index}")
                        sb.appendln(dobj.text)
                        sb.appendln("end note")

                    }
                }
            }
            is Document -> {
                sb.appendln("component C${dobj.index} <<document>> [")
                sb.appendln(dobj.name)
                if( dobj.properties.size > 0 ) {
                    sb.appendln( "---" )
                    for( prop in dobj.properties ) sb.appendln( "- ${prop}")
                }
                sb.appendln("]")
                when( dobj.prevDomainObject ) {
                    is Comment -> {
                        sb.appendln("C${dobj.prevDomainObject?.prevDomainObject?.index} -[hidden]-> C${dobj.index}")
                    }
                    else -> sb.appendln("C${dobj.prevDomainObject?.index} -[hidden]-> C${dobj.index}")
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
