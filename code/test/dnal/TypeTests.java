package dnal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mef.dnal.core.DValue;
import org.mef.dnal.validation.ValidationError;

public class TypeTests {

	public static class ValidationResult {
		public boolean isValid;
		public Object validObj;
		public List<ValidationError> errors = new ArrayList<>();
	}

	public interface ITypeValidator {
		ValidationResult validate(DValue dval, Object inputObj);
	}

	public static class MockIntValidator implements ITypeValidator {

		@Override
		public ValidationResult validate(DValue dval, Object inputObj) {
			ValidationResult result = new ValidationResult();

			if (inputObj == null) {
				addError(result, dval, "value is null");
			} else {
				try {
					Integer n = Integer.parseInt(inputObj.toString());
					result.isValid = true;
					result.validObj = n;
				} catch(NumberFormatException e) {
					addError(result, dval, "not an integer");
				}
			}

			return result;
		}

		private void addError(ValidationResult result, DValue dval, String err) {
			ValidationError valerr = new ValidationError();
			valerr.fieldName = dval.packageName + "." + dval.name;
			valerr.error = err;
			result.errors.add(valerr);
		}
	}

	@Test
	public void test() {
		shouldFail(null);
		shouldFail(" ");
		shouldFail(" 5 ");
		shouldFail("ab5");
		shouldFail("5.1");

		shouldPass("0", 0);
		shouldPass("10", 10);
		shouldPass("-450", -450);
	}

	private void shouldFail(String input) {
		MockIntValidator validator = new MockIntValidator();
		DValue dval = createDValue(input);
		ValidationResult result = validator.validate(dval, dval.value);
		checkFail(result);
	}

	private void shouldPass(String input, Object expected) {
		MockIntValidator validator = new MockIntValidator();
		DValue dval = createDValue(input);
		ValidationResult result = validator.validate(dval, dval.value);
		checkPass(result, expected);
	}

	private void checkPass(ValidationResult result, Object object) {
		assertEquals(true, result.isValid);
		assertEquals(object, result.validObj);
		assertTrue(result.errors.size() == 0);
	}
	private void checkFail(ValidationResult result) {
		assertEquals(false, result.isValid);
		assertTrue(result.errors.size() > 0);
	}

	private DValue createDValue(String object) {
		DValue dval = new DValue();
		dval.name = "item1";
		dval.packageName = "a.b.c";
		dval.type = "int";
		dval.value = object;
		return dval;
	}

}
