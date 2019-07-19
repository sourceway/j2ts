package eu.sourceway.j2ts

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.StandardLocation

class J2TsProperties(private val processingEnv: ProcessingEnvironment) {

    private val options = processingEnv.options
    private val properties = Properties()
    private val paths by lazy { findProjectPaths() }

    init {
        val message = options["j2ts.config"]
        if (message != null) {
            File(message).inputStream().use {
                properties.load(it)
            }
        }
    }

    fun outputTarget(): String {
        return opt("output-target", paths.projectTarget.resolve("generated-typescript").absolutePath)
    }

    fun outputFile(): String {
        return opt("output-file", "models.ts")
    }

    fun generationTarget(): String {
        return opt("generation-target", paths.projectTarget.resolve("generated-typescript").absolutePath)
    }

    private fun opt(key: String, defaultVal: String): String {
        return options["j2ts.${key.replace("-", ".")}"]
                ?: properties[key]?.toString()
                ?: defaultVal
    }

    private fun findProjectPaths(): Paths {
        val dummySourceFile = try {
            processingEnv.filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "dummy" + System.currentTimeMillis())
        } catch (ignored: IOException) {
            throw FileNotFoundException()
        }

        var dummySourceFilePath = dummySourceFile.toUri().toString()

        if (dummySourceFilePath.startsWith("file:")) {
            if (!dummySourceFilePath.startsWith("file://")) {
                dummySourceFilePath = "file://${dummySourceFilePath.substring("file:".length)}"
            }
        } else {
            dummySourceFilePath = "file://$dummySourceFilePath"
        }

        val cleanURI = try {
            URI(dummySourceFilePath)
        } catch (e: URISyntaxException) {
            throw FileNotFoundException()
        }

        val dummyFile = File(cleanURI)
        val sourcesGenerationFolder = dummyFile.parentFile
        val projectTarget = sourcesGenerationFolder?.parentFile
        val projectRoot = projectTarget?.parentFile

        return Paths(
                projectTarget ?: throw FileNotFoundException(),
                projectRoot ?: throw FileNotFoundException()
        )
    }

    private data class Paths(val projectTarget: File, val projectRoot: File)
}
