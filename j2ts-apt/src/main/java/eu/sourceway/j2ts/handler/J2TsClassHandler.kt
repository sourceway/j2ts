package eu.sourceway.j2ts.handler

import eu.sourceway.j2ts.TypeMappings
import eu.sourceway.j2ts.annotations.J2TsProperty
import eu.sourceway.j2ts.annotations.J2TsType
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

class J2TsClassHandler(private val typeEl: TypeElement, private val processingEnv: ProcessingEnvironment) : AbstractTypeHandler(typeEl, processingEnv) {

    private val collectionTypes = listOf(
            "java.util.List", "java.util.List<E>",
            "java.util.Set", "java.util.Set<E>",
            "java.util.Map", "java.util.Map<K,V>"
    )

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

    override fun generateCode(): String {
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

        val ignore = methodAnnotation?.ignore ?: false
        val optional = methodAnnotation?.optional ?: false

        val propertyName = el.simpleName.toString().let {
            when {
                it.startsWith("is") -> it.substring(2)
                it.startsWith("get") -> it.substring(3)
                else -> throw IllegalStateException("$el is not a valid getter")
            }
        }.decapitalize()

        val (propertyType, import) = determinePropertyType(el.returnType, methodAnnotation)
        return FieldMeta(propertyName, propertyType, optional, ignore, import)
    }

    private fun determinePropertyType(typeMirror: TypeMirror, methodAnnotation: J2TsProperty? = null): Pair<String, String> {
        var propertyType = methodAnnotation?.type
        var import = ""

        if (propertyType.isNullOrBlank()) {
            val (isCollectionType, typeArguments) = isCollectionType(typeMirror)
            if (isCollectionType) {
                when {
                    typeArguments.size == 1 -> {
                        val (arrayType, arrayImport) = determinePropertyType(typeArguments[0])
                        propertyType = "$arrayType[]"
                        import = arrayImport
                    }
                    typeArguments.size == 2 -> {
                        val (keyType, keyImport) = determinePropertyType(typeArguments[0])
                        val (valueType, valueImport) = determinePropertyType(typeArguments[1])
                        propertyType = "{ [key: $keyType]: $valueType; }"
                        import = keyImport + "\n" + valueImport
                    }
                    else -> throw IllegalStateException("expected to have 1 or 2 typeArguments, got ${typeArguments.size}")
                }
            }
        }

        if (propertyType.isNullOrBlank()) {
            val returnElement = typeMirror.asTypeElement()
            val annotation = returnElement?.getAnnotation(J2TsType::class.java)

            propertyType = annotation?.name

            if (annotation != null && typeEl.packageName != returnElement.packageName) {
                val substringAfterLast = if (propertyType.isNullOrBlank()) returnElement.simpleName else propertyType
                import = "import {$substringAfterLast} from './${returnElement.packageName}';"
            }
        }

        if (propertyType.isNullOrBlank()) {
            propertyType = TypeMappings[typeMirror]
        }

        if (propertyType.isNullOrBlank()) {
            propertyType = typeMirror.toString().substringAfterLast(".")
        }

        return Pair(propertyType, import)
    }

    private fun isCollectionType(returnType: TypeMirror): Pair<Boolean, List<TypeMirror>> {
        val returnElement = returnType.asTypeElement() ?: return Pair(false, emptyList())

        if (("$returnElement" in collectionTypes || returnElement.interfaces.any { "$it" in collectionTypes })
                && returnType is DeclaredType) {
            return Pair(true, returnType.typeArguments)
        }

        return Pair(false, emptyList())
    }
}
