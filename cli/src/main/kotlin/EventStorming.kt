package com.youramaryllis.eventstorming

import com.youramaryllis.eventstorming.data.DomainObject
import com.youramaryllis.eventstorming.plantuml.PlantUML
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import java.io.*
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.stream.Collectors.toList

fun main(args: Array<String>) {
    val parser = ArgParser(args)
    val emlFileName by parser.storing( "-f", help = "Input Event Markdown Language file name")
    val pngFileName by parser.storing( "-o", help = "Output PNG file name")
    val plantFileName by parser.storing( "-p", help = "Output PlantUML file name").default<String?>(null)
    var emlPath = FileSystems.getDefault().getPath(emlFileName).toAbsolutePath()
    var pngPath = FileSystems.getDefault().getPath(pngFileName).toAbsolutePath()
    var plantPath : Path? = if(plantFileName != null ) FileSystems.getDefault().getPath(plantFileName).toAbsolutePath() else null
    EventStorming(emlPath).toPlantUML(plantPath).toPNG(pngPath)
}

class EventStorming (emlReader: Reader){
    private val domainObject : List<DomainObject> = emlReader.buffered().use { reader ->
        reader.lines()
                .filter { !it.isBlank() }
                .map { DomainObject.parse(it) }
                .collect(toList())
    }

    fun getDomainObject(): List<DomainObject> {
        return domainObject
    }

    constructor( emlPath: Path ) : this(FileReader(emlPath.toFile()))
    constructor( emlFile: File ) : this(FileReader(emlFile))
    constructor( text: String ) : this( StringReader(text) )

    fun toPlantUML(plantPath: Path? = null): PlantUML {
        val plantuml = PlantUML(domainObject)
        if( plantPath != null ) {
            FileWriter(plantPath.toFile()).buffered().use { it.write(plantuml.stringBuilder.toString()) }
        }
        return plantuml
    }
}