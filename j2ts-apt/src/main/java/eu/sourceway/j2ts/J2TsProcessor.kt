package eu.sourceway.j2ts

import eu.sourceway.j2ts.annotations.J2TsProperty
import eu.sourceway.j2ts.annotations.J2TsType
import eu.sourceway.j2ts.handler.J2TsClassHandler
import eu.sourceway.j2ts.handler.J2TsEnumHandler
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.tools.Diagnostic.Kind

class J2TsProcessor : AbstractProcessor() {

    private val properties by lazy { J2TsProperties(processingEnv) }

    override fun process(annotations: Set<TypeElement>,
                         roundEnv: RoundEnvironment): Boolean {
        if (annotations.isEmpty()) {
            return false
        }

        roundEnv.getElementsAnnotatedWith(J2TsProperty::class.java)
                .asSequence()
                .map { it as ExecutableElement }
                .forEach { e ->
                    val annotation = e.enclosingElement.getAnnotation(J2TsType::class.java)
                    if (annotation == null) {
                        processingEnv.messager.printMessage(Kind.ERROR,
                                "enclosing element must be annotated with ${J2TsType::class.java.canonicalName}", e)
                    }
                    if (!(e.simpleName.startsWith("get") || e.simpleName.startsWith("is"))
                            || TypeKind.VOID == e.returnType.kind || e.parameters.isNotEmpty()) {
                        processingEnv.messager.printMessage(Kind.ERROR,
                                "${J2TsProperty::class.java.canonicalName} is only allowed on getter methods", e)
                    }
                }

        val generationTarget = properties.generationTarget()
        roundEnv.getElementsAnnotatedWith(J2TsType::class.java)
                .asSequence()
                .map { it as TypeElement }
                .map {
                    when (it.kind) {
                        ElementKind.ENUM -> J2TsEnumHandler(it, processingEnv)
                        ElementKind.CLASS -> J2TsClassHandler(it, processingEnv)
                        else -> throw IllegalStateException("ElementKind ${it.kind} of type ${it.qualifiedName} not supported.")
                    }
                }
                .forEach { tsTypeHandler ->
                    generationTarget.resolve(tsTypeHandler.fileName).apply {
                        writeText(tsTypeHandler.generateCode())
                    }
                }

        generationTarget.listFiles { f -> f.name.endsWith(".ts") }
                .orEmpty().asSequence()
                .filterNotNull()
                .groupBy { it.name.substringBeforeLast(".").substringBeforeLast(".") }
                .forEach { (pkg, files) ->
                    properties.outputTarget().resolve("$pkg.ts").apply {
                        writeText("/* tslint:disable */\n")
                        appendText("/* eslint-disable */\n")
                        appendText("\n")

                        val allImports = mutableListOf<String>()
                        val allCodeBlocks = mutableListOf<String>()

                        files.forEach { file ->
                            val readLines = file.readLines()
                            val (imports, code) = readLines.partition { it.startsWith("import ") }
                            val trimmedCode = code.toMutableList().apply { removeIf { it.isBlank() } }

                            allImports.addAll(imports)
                            allCodeBlocks.add(trimmedCode.joinToString("\n"))
                        }

                        allImports.asSequence()
                                .distinct()
                                .joinToString("\n") { it }
                                .apply {
                                    if (this.isNotBlank()) {
                                        appendText(this)
                                        appendText("\n\n")
                                    }
                                }

                        allCodeBlocks.asSequence()
                                .joinToString("\n\n") { it }
                                .apply { appendText(this) }

                        appendText("\n")
                    }
                }

        return true
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf<String>(
                J2TsType::class.java.canonicalName,
                J2TsProperty::class.java.canonicalName
        )
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }
}
