import com.youramaryllis.eventstorming.EventStorming
import com.youramaryllis.eventstorming.plantuml.PlantUML
import org.junit.Test

import org.junit.Assert.*
import java.io.File

class EventStormingTest {

    @Test
    fun testParseName() {
        val plantUML = PlantUML(emptyList())
        assertEquals( "ABC\n---\n<&person> User", plantUML.parseName("(User) ABC") )
        assertEquals( "ABC\n---\n<&person> User", plantUML.parseName("ABC (User)") )
        assertEquals( "ABC\n---\n<&person> User\n---\n<&task> 1234", plantUML.parseName("(User) ABC [1234]") )
        assertEquals( "ABC\n---\n<&person> User\n---\n<&task> 1234", plantUML.parseName("ABC (User) [1234]") )
        assertEquals( "ABC\n---\n<&person> User\n---\n<&task> 1234", plantUML.parseName("(User) [1234] ABC") )
    }

    @Test
    fun testSimpleCommandEvent() {
        testBase("simple")
    }

    @Test
    fun testComment() {
        testBase("comment")
    }

    @Test
    fun testMultilineCommentCommand() {
        testBase("multiline-comment")
    }

    @Test
    fun testCommandName() {
        testBase("command-name")
    }

    @Test
    fun testDocument() {
        testBase( "document" )
    }

    @Test
    fun testDocumentComment() {
        testBase( "document-comment" )
    }

    @Test
    fun testLastEmptyLine() {
        testBase( "last-empty-line" )
    }

    @Test
    fun testCombined() {
        testBase( "combined" )
    }

    @Test
    fun testExternal() {
        testBase( "external" )
    }

    @Test
    fun testParallel() {
        testBase( "parallel" )
    }

    @Test
    fun testInvariant() {
        testBase( "invariant" )
    }

    fun testBase(name: String) {
        val eml = File(this::class.java.getResource("${name}.eml").toURI())
        val dobj = EventStorming(eml).getDomainObject()
        val puml = eml.resolve("../../../src/test/resources/${name}.puml").normalize()
        val pumlText = PlantUML(dobj).stringBuilder.toString()
        if( puml.exists() ) {
            val stringBuilder = StringBuilder()
            puml.bufferedReader().useLines{ lines -> lines.forEach {  stringBuilder.appendln(it) } }
            val refPumlText = stringBuilder.toString()
            assertEquals( refPumlText, pumlText )
        } else {
            puml.bufferedWriter().use { it.write(pumlText) }
        }
    }
}
