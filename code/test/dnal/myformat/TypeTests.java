package dnal.myformat;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mef.dnal.core.DValue;
import org.mef.dnal.validation.ValidationError;

import dnal.RegistryTests.TypeRegistry;

public class TypeTests {

	public static class ValidationResult {
		public boolean isValid;
		public Object validObj;
		public List<ValidationError> errors = new ArrayList<>();
	}

	public interface ITypeValidator {
		ValidationResult validate(DValue dval, Object inputObj);
	}

	public static abstract class ValidatorBase implements ITypeValidator {

		@Override
		public ValidationResult validate(DValue dval, Object inputObj) {
			ValidationResult result = new ValidationResult();

			if (inputObj == null) {
				if (dval.valueList != null) {
					prepareForSubObj(dval);
					boolean sav = result.isValid;
					int failCount = 0;
					for(DValue sub: dval.valueList) {
						//!!fix
						result.isValid = sav;
						doSubValue(result, sub);
						if (! result.isValid) {
							failCount++;
						}
					}
					buildSubObj(dval);
					
					result.isValid = (failCount == 0);
				} else if (dval.tmplist != null) {
					doValue(result, dval, inputObj);
				} else {
					addError(result, dval, "value is null");
				}
			} else {
				doValue(result, dval, inputObj);
			}

			return result;
		}

		protected abstract void prepareForSubObj(DValue dval);
		protected abstract void buildSubObj(DValue dval);
		protected abstract void doValue(ValidationResult result, DValue dval, Object inputObj);
		protected abstract void doSubValue(ValidationResult result, DValue dval);

		protected void addError(ValidationResult result, DValue dval, String err) {
			ValidationError valerr = new ValidationError();
			valerr.fieldName = dval.packageName + "." + dval.name;
			valerr.error = err;
			result.errors.add(valerr);
		}
	}
	public static abstract class SimpleValidatorBase extends ValidatorBase {

		@Override
		protected void doSubValue(ValidationResult result, DValue dval)
		{}
		@Override
		protected void prepareForSubObj(DValue dval)
		{}
		@Override
		protected void buildSubObj(DValue dval)
		{}
	}
	public static class MockIntValidator extends SimpleValidatorBase {

		@Override
		protected void doValue(ValidationResult result, DValue dval, Object inputObj) {
			try {
				Integer n = Integer.parseInt(inputObj.toString());
				result.isValid = true;
				result.validObj = n;
				dval.finalValue = result.validObj;
			} catch(NumberFormatException e) {
				addError(result, dval, "not an integer");
			}
		}
	}
	public static class MockStringValidator extends SimpleValidatorBase {

		@Override
		protected void doValue(ValidationResult result, DValue dval, Object inputObj) {
			try {
				result.isValid = true;
				result.validObj = inputObj.toString();
				dval.finalValue = result.validObj;
			} catch(NumberFormatException e) {
				addError(result, dval, "not an string");
			}
		}
	}
	public static class MockBooleanValidator extends SimpleValidatorBase {

		@Override
		protected void doValue(ValidationResult result, DValue dval, Object inputObj) {
			try {
				String s = inputObj.toString();
				if (s.equalsIgnoreCase("true")) {
					result.isValid = true;
					result.validObj = Boolean.TRUE;
					dval.finalValue = result.validObj;
				} else if (s.equalsIgnoreCase("false")) {
					result.isValid = true;
					result.validObj = Boolean.FALSE;
					dval.finalValue = result.validObj;
				} else {
					addError(result, dval, String.format("%s is neither 'true' nor 'false'"));
				}
			} catch(Exception e) {
				addError(result, dval, "not an boolean");
			}
		}
	}
	
	public static class MockListStringValidator extends SimpleValidatorBase {

		@Override
		protected void doValue(ValidationResult result, DValue dval, Object inputObj) {
			MockStringValidator inner = new MockStringValidator();
			result.isValid = true;
			for(String s: dval.tmplist) {
				DValue innerdval = new DValue();
				innerdval.type = "string";
				innerdval.rawValue = s;
				ValidationResult result2 =inner.validate(innerdval, s);
				if (!result2.isValid) {
					result.isValid = false;
				}
			}
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
		ValidationResult result = validator.validate(dval, dval.rawValue);
		checkFail(result);
	}

	private void shouldPass(String input, Object expected) {
		MockIntValidator validator = new MockIntValidator();
		DValue dval = createDValue(input);
		ValidationResult result = validator.validate(dval, dval.rawValue);
		checkPass(result, expected);
		assertEquals(expected, dval.finalValue);
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
		dval.rawValue = object;
		return dval;
	}

}
