package eu.sourceway.j2ts

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.util.Properties
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.StandardLocation

private const val MAX_CONFIG_DEPTH = 5

class J2TsProperties(private val processingEnv: ProcessingEnvironment) {

    private val options = processingEnv.options
    private val properties = Properties()
    private val projectTarget by lazy { findProjectTarget() }

    init {
        val configFile = options["j2ts.config"] ?: findConfig(projectTarget)?.absolutePath
        if (configFile != null) {
            File(configFile).inputStream().use {
                properties.load(it)
            }
        }
    }

    fun outputTarget(): String {
        return opt("output-target", projectTarget.resolve("generated-typescript").absolutePath)
    }

    fun outputFile(): String {
        return opt("output-file", "models.ts")
    }

    fun generationTarget(): String {
        return opt("generation-target", projectTarget.resolve("generated-typescript").absolutePath)
    }

    private fun opt(key: String, defaultVal: String): String {
        return options["j2ts.${key.replace("-", ".")}"]
                ?: properties[key]?.toString()
                ?: defaultVal
    }

    private tailrec fun findConfig(where: File, level: Int = 0): File? {
        val defaultFile = where.resolve(".j2ts")
        return when {
            defaultFile.isFile -> defaultFile
            level > MAX_CONFIG_DEPTH || where.parentFile == null -> null
            else -> findConfig(where.parentFile, level + 1)
        }
    }

    private fun findProjectTarget(): File {
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

        return sourcesGenerationFolder?.parentFile ?: throw FileNotFoundException()
    }
}
