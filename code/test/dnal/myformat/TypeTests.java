package dnal.myformat;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mef.dnal.core.DType;
import org.mef.dnal.core.DTypeEntry;
import org.mef.dnal.core.DValue;
import org.mef.dnal.validation.ValidationError;

import dnal.RegistryTests.TypeRegistry;

public class TypeTests {

	public static class ValidationResult {
		public boolean isValid;
//		public Object validObj;
		public List<ValidationError> errors = new ArrayList<>();
	}

	public interface ITypeValidator {
//		ValidationResult validate(DValue dval, Object inputObj);
		ValidationResult validate(DValue dval);
	}

	public static abstract class ValidatorBase implements ITypeValidator {

		@Override
		public ValidationResult validate(DValue dval) {
			ValidationResult result = new ValidationResult();

			if (dval.finalValue != null) {
				doValue(result, dval);
			} else if (dval.valueList != null) {
				prepareForSubObj(result, dval);
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
			} else {
				addError(result, dval, "value is null");
			}

			return result;
		}

		protected abstract void prepareForSubObj(ValidationResult result, DValue dval);
		protected abstract void buildSubObj(DValue dval);
		protected abstract void doValue(ValidationResult result, DValue dval);
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
		protected void prepareForSubObj(ValidationResult result, DValue dval)
		{}
		@Override
		protected void buildSubObj(DValue dval)
		{}
	}
	public static class MockIntValidator extends SimpleValidatorBase {

		@Override
		protected void doValue(ValidationResult result, DValue dval) {
			try {
				Integer n = Integer.parseInt(dval.finalValue.toString());
				result.isValid = true;
				dval.finalValue = n;
			} catch(NumberFormatException e) {
				addError(result, dval, "not an integer");
			}
		}
	}
	public static class MockLongValidator extends SimpleValidatorBase {

		@Override
		protected void doValue(ValidationResult result, DValue dval) {
			try {
				Long n = Long.parseLong(dval.finalValue.toString());
				result.isValid = true;
				dval.finalValue = n;
			} catch(NumberFormatException e) {
				addError(result, dval, "not a long");
			}
		}
	}
	public static class MockStringValidator extends SimpleValidatorBase {

		@Override
		protected void doValue(ValidationResult result, DValue dval) {
			try {
				result.isValid = true;
			} catch(NumberFormatException e) {
				addError(result, dval, "not an string");
			}
		}
	}
	public static class MockBooleanValidator extends SimpleValidatorBase {

		@Override
		protected void doValue(ValidationResult result, DValue dval) {
			try {
				String s = dval.finalValue.toString();
				if (s.equalsIgnoreCase("true")) {
					result.isValid = true;
				} else if (s.equalsIgnoreCase("false")) {
					result.isValid = true;
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
		protected void doSubValue(ValidationResult result, DValue dval)
		{
			result.isValid = true; //fix later!!
		}

		@Override
		protected void doValue(ValidationResult result, DValue dval) {
			// TODO Auto-generated method stub
			System.out.println("NEVER!!!");
		}
	}

	public static class MockStructValidator extends SimpleValidatorBase {
		public TypeRegistry registry;

		@Override
		protected void doSubValue(ValidationResult result, DValue dval) {
			ITypeValidator subval = registry.find(dval.type);
			if (subval == null) {
				this.addError(result, dval, String.format("missing val for sub: '%s' %s", dval.type, dval.name));
			} else {
				ValidationResult tmp = subval.validate(dval);
				result.isValid = tmp.isValid;
				result.errors.addAll(tmp.errors);
//				dval.finalValue = tmp.validObj;
			}
		}

		@Override
		protected void prepareForSubObj(ValidationResult result, DValue dval) {
			if (dval.type.equals("struct")) {
				return;
			}
			//custom types only
			DType customDType = registry.findCustomDType(dval.type);
			for(DValue childdval: dval.valueList) {
				DTypeEntry entry = findInType(customDType, childdval.name);
				if (entry == null) {
					this.addError(result, dval, String.format("Type %s doesn't have field %s", dval.type, childdval.name));
				} else {
					if (childdval.type == null) {
						childdval.type = entry.type;
					}
				}
			}
		}

		@Override
		protected void buildSubObj(DValue dval) {
		}

		@Override
		protected void doValue(ValidationResult result, DValue dval) {
		}

		private DTypeEntry findInType(DType customDType, String fieldName) {
			for(DTypeEntry entry: customDType.entries) {
				if (entry.name.equals(fieldName)) {
					return entry;
				}
			}
			return null;
		}
	}
	
	public static class MockEnumValidator extends SimpleValidatorBase {
		public TypeRegistry registry;

		@Override
		protected void doValue(ValidationResult result, DValue dval) {
			DType enumDType = registry.findCustomDType(dval.type);
			
			boolean found = false;
			for(DTypeEntry entry : enumDType.entries) {
				if (entry.name.equals(dval.finalValue)) {
					found = true;
					break;
				}
			}
			
			result.isValid = found;
			if (! found) {
				addError(result, dval, String.format("%s is not one of the enum values", dval.finalValue));
			}
		}

		@Override
		protected void prepareForSubObj(ValidationResult result, DValue dval) {
		}

		@Override
		protected void buildSubObj(DValue dval) {
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
		ValidationResult result = validator.validate(dval);
		checkFail(result);
	}

	private void shouldPass(String input, Object expected) {
		MockIntValidator validator = new MockIntValidator();
		DValue dval = createDValue(input);
		ValidationResult result = validator.validate(dval);
		checkPass(result, expected);
		assertEquals(expected, dval.finalValue);
	}

	private void checkPass(ValidationResult result, Object object) {
		assertEquals(true, result.isValid);
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
		dval.finalValue = object;
		return dval;
	}

}
