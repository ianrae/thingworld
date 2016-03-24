package org.mef.dnal.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationException extends Exception {
	public List<ValidationError> errors = new ArrayList<>();

	public ValidationException(List<ValidationError> errors) {
		this.errors = errors;
	}

	public void dump() {
		for(ValidationError err: errors) {
			String s = String.format("field %s - %s", err.fieldName, err.error);
			System.out.println(s);
		}
	}
}