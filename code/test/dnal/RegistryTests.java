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

import testhelper.BaseTest;
import dnal.DNALParserTests.FileScanner;
import dnal.TypeParserTests.DType;
import dnal.TypeParserTests.DTypeEntry;
import dnal.TypeTests.ITypeValidator;
import dnal.TypeTests.MockIntValidator;
import dnal.TypeTests.ValidationResult;
import dnal.TypeTests.ValidatorBase;

public class RegistryTests extends BaseTest {

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
	
	public static class MockCustomTypeValidator extends ValidatorBase {
		public TypeRegistry registry;
		
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


	public static class TypeValidator {
		public TypeRegistry registry;
		public List<ValidationError> errors = new ArrayList<>();
		public int addedCount;

		public TypeValidator() {
			RegistryBuilder builder = new RegistryBuilder();
			registry = builder.buildRegistry();
		}

		public boolean validate(List<DType> typeL) {
			for(DType dtype: typeL) {
				boolean ok = false;
				
				ITypeValidator validator = registry.find(dtype.name);
				if (validator == null) {
					ok = true;
				} else {
					addError(dtype, "already existing type: " + dtype.name);
				}
				
				validator = registry.find(dtype.baseType);
				if (validator == null) {
					ok = dtype.baseType.equals("struct");
					if (! ok) {
						addError(dtype, "unknown base type: " + dtype.baseType);
					}
				} else {
				}
				
				if (dtype.entries != null && dtype.entries.size() > 0) {
					if (! isStruct(dtype)) {
						addError(dtype, String.format("'%s' is not a struct", dtype.name));
					}
					
					for(DTypeEntry sub: dtype.entries) {
						if (! ensureExists(dtype, sub.type)) {
							ok = false;
						}
					}
				}
				
				
				if (ok) {
					MockCustomTypeValidator custom = new MockCustomTypeValidator();
					custom.registry = registry;
					registry.add(dtype.name, custom);
					addedCount++;
				}
			}
			return (errors.size() == 0);
		}
		
		private boolean isStruct(DType dtype) {
			DType original = dtype;
			
			while(true) {
				if (dtype.baseType.equals("struct")) {
					return true;
				}
					
				//ITypeValidator validator = registry.find(dtype.baseType);
				throw new IllegalArgumentException("not supported");
			}
		}

		private boolean ensureExists(DType dtype, String typeName) {
			boolean ok = false;
			ITypeValidator validator = registry.find(typeName);
			if (validator == null) {
				addError(dtype, "unknown type: " + typeName);
			} else {
				ok = true;
			}
			return ok;
		}

		private void addError(DType dtype, String errMsg) {
			ValidationError err = new ValidationError();
			err.fieldName = dtype.name;
			err.error = errMsg;
			errors.add(err);
		}
	}


	@Test
	public void testTypeValidator() {
		goodOne("Timeout", "int");
		goodOne("Timeout", "string");
		goodOne("Timeout", "boolean");

		badOne("Timeout", "nosuchtype");
		badOne("string", "int");
	}
	@Test
	public void testTypeValidatorSub() {
		List<DType> typeL = buildList("Customer", "struct", true, "int");
		TypeValidator validator = new TypeValidator();
		boolean b = validator.validate(typeL);
		dumpErrors(validator);
		assertEquals(true, b);
		assertEquals(1, validator.addedCount);
	}
	@Test
	public void testTypeValidatorSubBad() {
		List<DType> typeL = buildList("Customer", "struct", true, "zzzz");
		TypeValidator validator = new TypeValidator();
		boolean b = validator.validate(typeL);
		dumpErrors(validator);
		assertEquals(false, b);
		assertEquals(0, validator.addedCount);
	}
	
	//---
	
	private void goodOne(String name, String baseType) {
		List<DType> typeL = buildList(name, baseType);
		TypeValidator validator = new TypeValidator();
		boolean b = validator.validate(typeL);
		dumpErrors(validator);
		assertEquals(true, b);
		assertEquals(1, validator.addedCount);
	}
	private void dumpErrors(TypeValidator validator) {
		for(ValidationError err: validator.errors) {
			log(String.format("%s: %s", err.fieldName, err.error));
		}
		
	}
	private void badOne(String name, String baseType) {
		List<DType> typeL = buildList(name, baseType);
		TypeValidator validator = new TypeValidator();
		boolean b = validator.validate(typeL);
		dumpErrors(validator);
		assertEquals(false, b);
		assertEquals(0, validator.addedCount);
	}


	List<DType> buildList(String name, String baseType) {
		return buildList(name, baseType, false, null);
	}
	List<DType> buildList(String name, String baseType, boolean subTypes, String sub1Type) {
		DType dtype = new DType();
		dtype.baseType = baseType;
		dtype.name = name;
		dtype.packageName = null;
		
		if (subTypes) {
			DTypeEntry entry = new DTypeEntry();
			entry.name = "item1";
			entry.type = sub1Type;
			dtype.entries.add(entry);
			entry = new DTypeEntry();
			entry.name = "item2";
			entry.type = "int";
			dtype.entries.add(entry);
		}

		List<DType> typeL = new ArrayList<>();
		typeL.add(dtype);
		return typeL;
	}
}
