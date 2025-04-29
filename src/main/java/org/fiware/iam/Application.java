package org.fiware.iam;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.runtime.Micronaut;

@Introspected(classes = {String.class})
public class Application {

	public static void main(String[] args) {
		Micronaut.run(Application.class, args);
	}

}
