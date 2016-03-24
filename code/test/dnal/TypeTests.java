package dnal;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mef.dnal.core.DValue;

public class TypeTests {
	
	public static class ValidationResult {
		public boolean isValid;
		public Object validObj;
	}
	
	public interface ITypeValidator {
		ValidationResult validate(DValue dval, Object inputObj);
	}
	
	public static class MockIntValidator implements ITypeValidator {

		@Override
		public ValidationResult validate(DValue dval, Object inputObj) {
			ValidationResult result = new ValidationResult();
			
			try {
				if (inputObj != null) {
					Integer n = Integer.parseInt(inputObj.toString());
					result.isValid = true;
					result.validObj = n;
				}
			} catch(NumberFormatException e) {
				
			}
			return result;
		}
		
	}

	@Test
	public void test() {
		MockIntValidator validator = new MockIntValidator();
		DValue dval = createDValue(null);
		ValidationResult result = validator.validate(dval, null);
		checkFail(result);
	}

	private void checkPass(ValidationResult result, Object object) {
		assertEquals(true, result.isValid);
		assertEquals(object, result.validObj);
	}
	private void checkFail(ValidationResult result) {
		assertEquals(false, result.isValid);
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
