package eu.sourceway.j2ts.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Target({METHOD})
@Retention(RUNTIME)
public @interface J2TsProperty {

	/**
	 * @return custom typescript type to use
	 */
	String type() default "";

	/**
	 * @return whether or not this property should be ignored in typescript
	 */
	boolean ignore() default false;

	/**
	 * @return whether or not this property should be optional in typescript
	 */
	boolean optional() default false;
}
