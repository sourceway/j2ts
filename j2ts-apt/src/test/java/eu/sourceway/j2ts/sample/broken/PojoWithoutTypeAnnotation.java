package eu.sourceway.j2ts.sample.broken;

import eu.sourceway.j2ts.annotations.J2TsProperty;

public class PojoWithoutTypeAnnotation {

	@J2TsProperty(type = "number")
	public int getFive() {
		return 5;
	}
}
