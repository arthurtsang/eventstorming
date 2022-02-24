package com.youramaryllis.eventstorming.web

import com.youramaryllis.eventstorming.EventStorming
import com.youramaryllis.eventstorming.plantuml.PlantUML
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView
import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.zip.Deflater
import java.util.zip.Inflater

@CrossOrigin(origins = ["http://localhost:4200"])
@RestController
@RequestMapping( "/webeventstorming")
class WebEventStormingController( val webEventStormingService: WebEventStormingService ) {

    @Autowired lateinit var context: ApplicationContext

    private fun findAndLoadPage( filename: String ) : String? {
        if( filename.isEmpty() ) return ""
        val resources = context.getResources("classpath*:**/static/webeventstorming/eml/$filename")
        return if(resources.isNotEmpty()) resources[0].inputStream.bufferedReader().lines().reduce { x, y->"$x\n$y" }.get() else null
    }

    @PostMapping( "/eml" )
    fun encodeAndForward( @RequestBody eml: String, redirectAttributes: RedirectAttributes ): RedirectView {
        val encoded = encode(eml)
        return RedirectView("/webeventstorming/eml/$encoded")
    }

    @RequestMapping( "/encode", method = [(RequestMethod.POST)])
    fun encode(@RequestBody eml: String ): String {
        val deflater = Deflater()
        deflater.setInput(eml.toByteArray())
        deflater.finish()
        val compressedEml = ByteArray(8000)
        val length = deflater.deflate(compressedEml)
        deflater.end()
        val base64 = Base64.getEncoder().encodeToString(compressedEml.slice(IntRange(0,length)).toByteArray())
        return URLEncoder.encode(base64,StandardCharsets.UTF_8.toString()).replace("%","_")
    }

    @GetMapping( "/text/{encoded}", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun decode(@PathVariable("encoded") encoded: String): String {
        val base64 = URLDecoder.decode(encoded.replace("_","%"), StandardCharsets.UTF_8.toString())
        val decodedText = Base64.getDecoder().decode(base64)
        val inflater = Inflater()
        inflater.setInput(decodedText)
        val emlByteArray = ByteArray(10000 )
        val length = inflater.inflate(emlByteArray)
        inflater.end()
        return String(emlByteArray.slice(IntRange(0,length)).toByteArray())
    }

    @GetMapping("/png/{encoded}", produces = [MediaType.IMAGE_PNG_VALUE])
    @ResponseBody
    fun convertEML2PNG(@PathVariable("encoded") encoded: String): ByteArray {
        val eml = decode(encoded)
        val png = if( eml.startsWith("@startuml") ) {
                val plantUml = PlantUML(emptyList())
   		val sb = plantUml.stringBuilder
		sb.setLength(0)
		sb.trimToSize()
		sb.append(eml)
		plantUml.toPNG() as ByteArrayOutputStream
        } else {
        	EventStorming(eml).toPlantUML().toPNG() as ByteArrayOutputStream
	}
        return png.toByteArray()
    }

    @GetMapping( "/{path}" )
    fun forward(@PathVariable path: String ): Any? {
        return when( path ) {
            "text" -> {
                throw RuntimeException("fail calling /text without encoded text")
            }
            "eml" -> {
               RedirectView("/webeventstorming/eml/eJxzzs_2FNTcxLUdC140otS80rAQAuNAV3AA_3D_3D")
            }
            else -> {
                main(path)
            }
        }
    }

    @GetMapping( "/eml/{path}" )
    fun main(@PathVariable("path") path:String ): String? {
        var page = findAndLoadPage(path)
        if( page == null ) page = findAndLoadPage("index.html")
        return page
    }
}
