package eu.sourceway.j2ts

import eu.sourceway.j2ts.sample.AnotherPojo
import eu.sourceway.j2ts.sample.Pojo
import eu.sourceway.j2ts.utils.KtProcessorTestHelper
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName


class J2TsProcessorTest : KtProcessorTestHelper() {

    @Rule
    @JvmField
    val name = TestName()


    @Before
    fun setUp() {
        addProcessor(J2TsProcessor::class.java)
        addProcessorParameter("j2ts.generation.target", "target/test-ts-gen/${name.methodName}")
        addProcessorParameter("j2ts.output.target", "target/test-ts-out")
        addProcessorParameter("j2ts.output.file", "${name.methodName}.ts")
    }

    @Test
    fun `valid types compiling successful`() {
        val result = compileFiles(Pojo::class.java, AnotherPojo::class.java)
        assertCompilationSuccessful(result)
    }

    @Test
    fun `error on getter with property annotation in non annotated type`() {
        val (result, file) = compileSource(name.methodName, """
public class PojoWithoutTypeAnnotation {

    @J2TsProperty(type = "number")
    public int getFive() {
        return 5;
    }
}
""".trimIndent())

        assertCompilationError(result)
        assertCompilationErrorCount(1, result)
        assertCompilationErrorOn(file, "public int getFive()", result)
    }

    @Test
    fun `error on non-getter with property annotation`() {
        val (result, file) = compileSource(name.methodName, """
@J2TsType
public class PropertyAnnotationOnNonGetter {

	@J2TsProperty(type = "number")
	public int notAGetter() {
		return 5;
	}
}
""".trimIndent())

        assertCompilationError(result)
        assertCompilationErrorCount(1, result)
        assertCompilationErrorOn(file, "public int notAGetter()", result)
    }
}
