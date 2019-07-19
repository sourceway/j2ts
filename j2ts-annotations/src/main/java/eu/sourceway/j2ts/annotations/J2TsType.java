package eu.sourceway.j2ts.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Target({TYPE})
@Retention(RUNTIME)
public @interface J2TsType {

	/**
	 * @return custom typescript interface name
	 */
	String name() default "";
}
