package eu.sourceway.j2ts.handler

import eu.sourceway.j2ts.annotations.J2TsType
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

abstract class AbstractTypeHandler(private val typeEl: TypeElement, private val processingEnv: ProcessingEnvironment) {
    protected inline val TypeElement.packageName get() = this.qualifiedName.toString().substringBeforeLast(".")
    protected fun TypeMirror.asTypeElement() = processingEnv.typeUtils.asElement(this) as? TypeElement

    protected val targetName by lazy {
        val name = typeEl.getAnnotation(J2TsType::class.java)?.name ?: ""
        return@lazy if (name.isNotBlank()) name else typeEl.simpleName.toString()
    }

    val fileName = "${typeEl.packageName}.$targetName.ts"

    abstract fun generateCode(): String
}
