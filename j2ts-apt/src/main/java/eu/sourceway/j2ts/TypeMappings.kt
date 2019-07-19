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

        register(Long::class.java, "number")
        register(Int::class.java, "number")
        register(Integer::class.java, "number")
        register(Float::class.java, "number")
        register(Double::class.java, "number")
        register(BigDecimal::class.java, "number")
        register(BigInteger::class.java, "number")

        register("long", "number")
        register("int", "number")
        register("float", "number")
        register("double", "number")
    }

    override operator fun get(type: TypeMirror): String? = types[type.toString()]

    private fun register(type: String, target: String) {
        types[type] = target
    }

    private fun register(type: Class<*>, target: String) {
        register(type.canonicalName, target)
    }
}
