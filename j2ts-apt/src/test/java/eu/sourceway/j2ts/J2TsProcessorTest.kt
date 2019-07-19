package eu.sourceway.j2ts

import eu.sourceway.j2ts.sample.AnotherPojo
import eu.sourceway.j2ts.sample.Pojo
import eu.sourceway.j2ts.sample.broken.PojoWithoutTypeAnnotation
import eu.sourceway.j2ts.sample.broken.PropertyAnnotationOnNonGetter
import eu.sourceway.j2ts.utils.ProcessorTestHelper
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import java.io.File


class J2TsProcessorTest : ProcessorTestHelper() {

    @Rule
    @JvmField
    val name = TestName()

    private val javaTemp = File("target/test-java-tmp/")

    @Before
    fun setUp() {
        addProcessor(J2TsProcessor::class.java)
        addProcessorParameter("j2ts.generation.target", "target/test-ts-gen/${name.methodName}")
        addProcessorParameter("j2ts.output.target", "target/test-ts-out")
        addProcessorParameter("j2ts.output.file", "${name.methodName}.ts")
        javaTemp.mkdir()
    }

    @Test
    fun `valid types compiling successful`() {
        val result = compileFiles(Pojo::class.java, AnotherPojo::class.java)
        assertCompilationSuccessful(result)
    }

    @Test
    fun `error on getter with property annotation in non annotated type`() {
        val result = compileFiles(PojoWithoutTypeAnnotation::class.java)
        assertCompilationError(result)
        assertCompilationErrorCount(1, result)
        assertCompilationErrorOn(PojoWithoutTypeAnnotation::class.java, "public int getFive()", result)
    }

    @Test
    fun `error on non-getter with property annotation`() {
        val result = compileFiles(PropertyAnnotationOnNonGetter::class.java)
        assertCompilationError(result)
        assertCompilationErrorCount(1, result)
        assertCompilationErrorOn(PropertyAnnotationOnNonGetter::class.java, "public int notAGetter()", result)
    }
}
