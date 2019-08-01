package eu.sourceway.j2ts

import java.math.BigDecimal
import java.math.BigInteger
import javax.lang.model.type.TypeMirror

interface ITypeMappings {
    operator fun get(type: TypeMirror): String?
}

val TypeMappings = object : ITypeMappings {

    private val types = mutableMapOf<String, String>()

    init {
        register(String::class.java, "string")

        register(BigDecimal::class.java, "number")
        register(BigInteger::class.java, "number")

        register("long", "number")
        register("int", "number")
        register("float", "number")
        register("double", "number")
        register("java.lang.Long", "number")
        register("java.lang.Integer", "number")
        register("java.lang.Float", "number")
        register("java.lang.Double", "number")
        register("java.lang.Boolean", "boolean")
    }

    override operator fun get(type: TypeMirror): String? = types[type.toString()]

    private fun register(type: String, target: String) {
        types[type] = target
    }

    private fun register(type: Class<*>, target: String) {
        register(type.canonicalName, target)
    }
}
