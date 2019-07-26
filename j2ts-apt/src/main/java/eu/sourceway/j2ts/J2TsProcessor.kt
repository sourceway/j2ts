package eu.sourceway.j2ts

import eu.sourceway.j2ts.annotations.J2TsProperty
import eu.sourceway.j2ts.annotations.J2TsType
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
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
                .forEach { e ->
                    val name = e.getAnnotation(J2TsType::class.java)?.name ?: ""
                    val targetName = if (name.isNotBlank()) name else e.simpleName

                    generationTarget.resolve("$targetName.ts").apply {
                        writeText("/**\n * Generated from ${e.qualifiedName}\n */\n")
                        appendText("export interface $targetName {\n")

                        processingEnv.elementUtils
                                .getAllMembers(e)
                                .asSequence()
                                .filter(this@J2TsProcessor::shouldGenerateMethod)
                                .map(ExecutableElement::class.java::cast)
                                .filter {
                                    it.simpleName.toString().startsWith("get")
                                            || it.simpleName.toString().startsWith("is")
                                }
                                .filterNot { TypeKind.VOID == it.returnType.kind }
                                .map(this@J2TsProcessor::buildProp)
                                .filterNot { it.ignore }
                                .map { "    ${it.name}${if (it.optional) "?" else ""}: ${it.type};\n" }
                                .forEach { appendText(it) }

                        appendText("}\n")
                    }
                }

        properties.outputFile().apply {
            writeText("/* tslint:disable */\n")
            appendText("/* eslint-disable */\n")
            appendText("\n")

            generationTarget.listFiles { f -> f != this }
                    ?.map { it.readText() }
                    ?.joinToString("\n") { it }
                    ?.apply { appendText(this) }
        }

        return true
    }

    private fun buildProp(el: ExecutableElement): Prop {
        val methodAnnotation: J2TsProperty? = el.getAnnotation(J2TsProperty::class.java)

        var propertyType = methodAnnotation?.type

        if (propertyType.isNullOrBlank()) {
            propertyType = processingEnv.typeUtils.asElement(el.returnType)?.getAnnotation(J2TsType::class.java)?.name
        }

        if (propertyType.isNullOrBlank()) {
            propertyType = TypeMappings[el.returnType]
        }

        if (propertyType.isNullOrBlank()) {
            propertyType = el.returnType.toString().replaceBeforeLast(".", "").trimStart('.')
        }

        val ignore = methodAnnotation?.ignore ?: false
        val optional = methodAnnotation?.optional ?: false

        val propertyName = el.simpleName.toString().let {
            when {
                it.startsWith("is") -> it.substring(2)
                it.startsWith("get") -> it.substring(3)
                else -> throw IllegalStateException("$el is not a valid getter")
            }
        }.decapitalize()

        return Prop(propertyName, propertyType, optional, ignore)
    }

    private class Prop(val name: String, val type: String, val optional: Boolean, val ignore: Boolean)

    private fun shouldGenerateMethod(element: Element): Boolean {
        if (element.kind != ElementKind.METHOD) {
            return false
        }

        val typeElement = element.enclosingElement as TypeElement
        return !typeElementIsClass(typeElement, Any::class.java)
    }

    private fun typeElementIsClass(typeElement: TypeElement,
                                   className: Class<*>, vararg classNames: Class<*>): Boolean {
        val qualifiedName = typeElement.qualifiedName.toString()
        if (qualifiedName == className.canonicalName) {
            return true
        }
        for (otherName in classNames) {
            if (qualifiedName == otherName.canonicalName) {
                return true
            }
        }
        return false
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
