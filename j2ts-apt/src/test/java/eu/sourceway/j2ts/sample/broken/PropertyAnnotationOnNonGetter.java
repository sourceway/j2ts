package eu.sourceway.j2ts.sample.broken;

import eu.sourceway.j2ts.annotations.J2TsProperty;
import eu.sourceway.j2ts.annotations.J2TsType;

@J2TsType
public class PropertyAnnotationOnNonGetter {

	@J2TsProperty(type = "number")
	public int notAGetter() {
		return 5;
	}
}
