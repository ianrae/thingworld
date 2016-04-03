package dnal;

import java.util.ArrayList;
import java.util.List;

import org.mef.dnal.core.DValue;
import org.mef.dnal.parser.ParseErrorTracker;
import org.mef.dnal.validation.ValidationError;

import testhelper.BaseTest;
import dnal.RegistryTests.TypeRegistry;
import dnal.myformat.TypeTests.ITypeValidator;
import dnal.myformat.TypeTests.ValidationResult;

public class DNALLoadValidatorTests extends BaseTest {
	
	
	public static class DNALLoadValidator {
		public TypeRegistry registry;
		public List<ValidationError> errors = new ArrayList<>();
		private ParseErrorTracker errorTracker = new ParseErrorTracker();

		public DNALLoadValidator(ParseErrorTracker errorTracker) {
			this.errorTracker = errorTracker;
		}

		public boolean validate(List<DValue> valueL) {
			int failCount = 0;
			for(DValue dval: valueL) {
				ITypeValidator validator = registry.find(dval.type);
				if (validator != null) {
					ValidationResult result = validator.validate(dval);
					if (! result.isValid) {
						failCount++;
						for(ValidationError err: result.errors) {
							addError(err);
						}
					} else if (dval.finalValue == null) {
						if (dval.type.equals("struct")) {
							//in toml even basic values are in a struct.
						} else {
							ValidationError err = new ValidationError();
							err.fieldName = dval.name;
							err.error = "null finalValue";
							addError(err);
							failCount++;
						}
					}
				} else {
					ValidationError err = new ValidationError();
					err.fieldName = dval.name;
					err.error = "missing validator for type: " + dval.type;
					addError(err);
					failCount++;
				}
			}
			return (failCount == 0);
		}
		
		private void addError(ValidationError err) {
			errorTracker.addError(String.format("validation error: %s: %s", err.fieldName, err.error));
			errors.add(err);
		}

	}


}
