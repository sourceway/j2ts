package eu.sourceway.j2ts.handler

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

class J2TsEnumHandler(private val typeEl: TypeElement, private val processingEnv: ProcessingEnvironment) : AbstractTypeHandler(typeEl, processingEnv) {
    override fun generateCode(): String {
        val fieldsSrc = typeEl.enclosedElements.asSequence()
                .filter { it.kind == ElementKind.ENUM_CONSTANT }
                .joinToString("\n") { "    $it = '$it'," }

        return """
/**
 * Generated from ${typeEl.qualifiedName}
 */
export enum $targetName {
$fieldsSrc
}

""".trimIndent()
    }
}
