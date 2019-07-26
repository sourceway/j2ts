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

    private val properties = Properties()
    private val generatedSourcesTarget by lazy { findGeneratedSourcesTarget() }
    private val projectRoot by lazy { findProjectRoot(generatedSourcesTarget) }

    init {
        if (projectRoot.hasConfig) {
            projectRoot.configFile.inputStream().use { stream -> properties.load(stream) }
        }
    }

    fun outputTarget(): File {
        return targetDirectory("output-target", "typescript-out")
    }

    fun generationTarget(): File {
        return targetDirectory("generation-target", "typescript-gen")
    }

    private fun targetDirectory(propertyKey: String, defaultTarget: String): File {
        val outputTarget = properties[propertyKey]?.toString()
                ?: return generatedSourcesTarget.resolve(defaultTarget).absoluteFile.apply { mkdirs() }

        val file = File(outputTarget)
        if (file.isAbsolute) {
            return file.apply { mkdirs() }
        }

        return projectRoot.directory.resolve(outputTarget).absoluteFile.normalize().apply { mkdirs() }
    }

    private data class ProjectRoot(val directory: File, val hasConfig: Boolean) {
        val configFile: File
            get() = directory.resolve(".j2ts")
    }

    private tailrec fun findProjectRoot(where: File, level: Int = 0): ProjectRoot {
        val pomXml = where.resolve("pom.xml")
        val j2tsConfig = where.resolve(".j2ts")
        val buildGradle = where.resolve("build.gradle")
        return when {
            j2tsConfig.isFile -> ProjectRoot(where, true)
            pomXml.isFile || buildGradle.isFile -> ProjectRoot(where, false)
            level > MAX_CONFIG_DEPTH || where.parentFile == null -> throw IllegalStateException("unable to find project root")
            else -> findProjectRoot(where.parentFile, level + 1)
        }
    }

    private fun findGeneratedSourcesTarget(): File {
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

        return sourcesGenerationFolder?.parentFile?.absoluteFile ?: throw FileNotFoundException()
    }
}
