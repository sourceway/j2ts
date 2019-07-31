package eu.sourceway.j2ts

import eu.sourceway.j2ts.annotations.J2TsProperty
import eu.sourceway.j2ts.annotations.J2TsType
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind

class J2TsTypeHandler(private val typeEl: TypeElement, private val processingEnv: ProcessingEnvironment) {
    private inline val TypeElement.packageName get() = this.qualifiedName.toString().substringBeforeLast(".")

    private val targetName by lazy {
        val name = typeEl.getAnnotation(J2TsType::class.java)?.name ?: ""
        return@lazy if (name.isNotBlank()) name else typeEl.simpleName
    }

    val fileName = "${typeEl.packageName}.$targetName.ts"

    private fun parseFields(): List<FieldMeta> {
        return processingEnv.elementUtils
                .getAllMembers(typeEl)
                .asSequence()
                .filter(this::shouldGenerateMethod)
                .map(ExecutableElement::class.java::cast)
                .filter {
                    it.simpleName.toString().startsWith("get")
                            || it.simpleName.toString().startsWith("is")
                }
                .filterNot { TypeKind.VOID == it.returnType.kind }
                .map(this::buildFieldMeta)
                .filterNot { it.ignore }
                .toList()
    }

    private class FieldMeta(val name: String, val type: String, val optional: Boolean, val ignore: Boolean, val import: String)

    private fun shouldGenerateMethod(element: Element): Boolean {
        if (element.kind != ElementKind.METHOD) {
            return false
        }

        val typeElement = element.enclosingElement as TypeElement
        val qualifiedName = typeElement.qualifiedName.toString()
        return qualifiedName != Any::class.java.canonicalName
    }

    fun generateCode(): String {
        val fields = parseFields()

        val imports = fields.map { it.import }.distinct().joinToString("\n")
        val fieldsSrc = fields.joinToString("\n") { "    ${it.name}${if (it.optional) "?" else ""}: ${it.type};" }

        return """
$imports
/**
 * Generated from ${typeEl.qualifiedName}
 */
export interface $targetName {
$fieldsSrc
}

""".trimIndent()
    }

    private fun buildFieldMeta(el: ExecutableElement): FieldMeta {
        val methodAnnotation: J2TsProperty? = el.getAnnotation(J2TsProperty::class.java)

        var propertyType = methodAnnotation?.type
        var import = ""

        if (propertyType.isNullOrBlank()) {
            val returnElement = processingEnv.typeUtils.asElement(el.returnType) as? TypeElement
            val annotation = returnElement?.getAnnotation(J2TsType::class.java)

            propertyType = annotation?.name

            if (annotation != null && typeEl.packageName != returnElement.packageName) {
                val substringAfterLast = if (propertyType.isNullOrBlank()) returnElement.simpleName else propertyType
                import = "import {$substringAfterLast} from './${returnElement.packageName}';"
            }
        }

        if (propertyType.isNullOrBlank()) {
            propertyType = TypeMappings[el.returnType]
        }

        if (propertyType.isNullOrBlank()) {
            propertyType = el.returnType.toString().substringAfterLast(".")
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

        return FieldMeta(propertyName, propertyType, optional, ignore, import)
    }
}