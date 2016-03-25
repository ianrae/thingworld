package dnal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mef.dnal.core.DValue;
import org.mef.dnal.validation.ValidationError;
import org.thingworld.sfx.SfxTextReader;

import dnal.DNALParserTests.FileScanner;
import dnal.TypeParserTests.DType;
import dnal.TypeTests.ITypeValidator;
import dnal.TypeTests.MockIntValidator;
import dnal.TypeTests.ValidationResult;

public class RegistryTests {
	
	public static class TypeRegistry {
		private Map<String,ITypeValidator> map = new HashMap<>();
		
		public void add(String type, ITypeValidator validator) {
			map.put(type, validator);
		}

		public ITypeValidator find(String type) {
			return map.get(type);
		}
	}
	
	public static class RegistryBuilder {
		
		public TypeRegistry buildRegistry() {
			TypeRegistry registry = new TypeRegistry();
			registry.add("int", new MockIntValidator());
			registry.add("string", new TypeTests.MockStringValidator());
			registry.add("boolean", new TypeTests.MockBooleanValidator());
			return registry;
		}
	}

	public static class TypeValidator {
		public TypeRegistry registry;
		public List<ValidationError> errors = new ArrayList<>();
//		private List<DType> typeL;
		
		public TypeValidator() {
			RegistryBuilder builder = new RegistryBuilder();
			registry = builder.buildRegistry();
		}
		
		public boolean validate(List<DType> typeL) {
			int failCount = 0;
			for(DType tval: typeL) {
				ITypeValidator validator = registry.find(tval.name);
				if (validator != null) {
//					ValidationResult result = validator.validate(dval, dval.rawValue);
//					if (! result.isValid) {
//						failCount++;
//						errors.addAll(result.errors);
//					} else if (dval.finalValue == null) {
//						ValidationError err = new ValidationError();
//						err.fieldName = dval.name;
//						err.error = "null finalValue";
//						errors.add(err);
//						failCount++;
//					}
				} else {
					ValidationError err = new ValidationError();
					err.fieldName = tval.name;
					err.error = "unknown type: " + tval.name;
					errors.add(err);
					failCount++;
				}
			}
			return (failCount == 0);
		}

	}
	
	
	@Test
	public void testTypeValidator() {
		goodOne("Timeout", "int");
		goodOne("Timeout", "string");
		goodOne("Timeout", "boolean");

		badOne("Timeout", "nosuchtype");
	}
	
	private void goodOne(String name, String baseType) {
		List<DType> typeL = buildList(name, baseType);
		TypeValidator validator = new TypeValidator();
		boolean b = validator.validate(typeL);
		assertEquals(true, b);
	}
	private void badOne(String name, String baseType) {
		List<DType> typeL = buildList(name, baseType);
		TypeValidator validator = new TypeValidator();
		boolean b = validator.validate(typeL);
		assertEquals(false, b);
	}


	List<DType> buildList(String name, String baseType) {
		DType dtype = new DType();
		dtype.baseType = baseType;
		dtype.name = name;
		dtype.packageName = null;
		
		List<DType> typeL = new ArrayList<>();
		typeL.add(dtype);
		return typeL;
	}
}
