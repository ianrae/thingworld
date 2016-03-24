package org.mef.dnal.validation;

import java.util.List;

public abstract class ValidationBase {
	protected String fieldName;
	protected String value;
	
	public ValidationBase(String fieldName, String value) {
		this.fieldName = fieldName;
		this.value = value;
	}
	
	public abstract void validate(List<ValidationError> list);

	protected void addError(List<ValidationError> errors, String fieldName, String err) {
		ValidationError error = new ValidationError();
		error.fieldName = fieldName;
		error.error = err;
		errors.add(error);
	}
}