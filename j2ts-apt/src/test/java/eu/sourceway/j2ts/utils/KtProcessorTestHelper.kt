package eu.sourceway.j2ts.utils

import eu.sourceway.j2ts.J2TsProcessor
import java.io.File

open class KtProcessorTestHelper : ProcessorTestHelper() {
    private val annotations by lazy { J2TsProcessor().supportedAnnotationTypes }


    val javaTemp = File("target/test-java-tmp/")

    init {
        javaTemp.mkdir()
    }

    fun compileSource(name: String, source: String): SourceCompilationResult {
        val javaSourceFile = javaTemp.resolve("$name.java")

        javaSourceFile.writeText("")
        annotations.forEach { annotation ->
            javaSourceFile.appendText("import $annotation;\n")
        }
        javaSourceFile.appendText(source)

        return SourceCompilationResult(compileFiles(javaSourceFile), javaSourceFile)
    }

    data class SourceCompilationResult(
            val compileResult: CompileResult,
            val file: File
    )
}
