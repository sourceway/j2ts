package eu.sourceway.j2ts

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import eu.sourceway.j2ts.utils.ProcessorTestHelper
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import kotlin.test.assertEquals


@RunWith(Parameterized::class)
class CompilationTests(
        private val name: String,
        private val java: String,
        private val typescript: String
) : ProcessorTestHelper() {

    @JsonRootName("test")
    private data class Xml(
            val java: String,
            val typescript: String
    )

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun createTestData(): List<Array<String>> {
            val xmlPath = File("src/test/resources/compilation-tests")

            val xmlMapper = XmlMapper(JacksonXmlModule().apply {
                setDefaultUseWrapper(false)
            }).registerKotlinModule()
                    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    ?: throw IllegalStateException("unable to instantiate XmlMapper")

            return xmlPath.listFiles { f -> f.name.endsWith(".xml") }
                    ?.asSequence()
                    ?.map {
                        val xml: Xml = xmlMapper.readValue(it)
                        arrayOf(
                                it.name.substringBeforeLast("."),
                                xml.java.trimIndent(),
                                xml.typescript.trimIndent()
                        )
                    }?.toList()
                    ?: throw IllegalStateException("unable to find test cases")

        }
    }

    private val annotations by lazy { J2TsProcessor().supportedAnnotationTypes }
    private val javaTemp = File("target/test-java-tmp/")

    @Before
    fun setUp() {
        addProcessor(J2TsProcessor::class.java)
        addProcessorParameter("j2ts.generation.target", "target/test-ts-gen/$name")
        addProcessorParameter("j2ts.output.target", "target/test-ts-out")
        addProcessorParameter("j2ts.output.file", "$name.ts")
        javaTemp.mkdir()
    }

    @Test
    fun `compile and validate output`() {
        val javaSourceFile = javaTemp.resolve("$name.java")

        javaSourceFile.writeText("")
        annotations.forEach { annotation ->
            javaSourceFile.appendText("import $annotation;\n")
        }
        javaSourceFile.appendText(java)

        val result = compileFiles(javaSourceFile)
        assertCompilationSuccessful(result)

        val readText = File("target/test-ts-out/$name.ts").readText()

        assertEquals("/* tslint:disable */\n/* eslint-disable */\n\n$typescript\n", readText)
    }
}

