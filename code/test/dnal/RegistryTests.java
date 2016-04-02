package dnal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mef.dnal.core.DType;
import org.mef.dnal.core.DTypeEntry;
import org.mef.dnal.core.DValue;
import org.mef.dnal.parser.ParseErrorTracker;
import org.mef.dnal.validation.ValidationError;
import org.thingworld.sfx.SfxTextReader;

import testhelper.BaseTest;
import dnal.TypeGeneratorTests.ITypeGenerator;
import dnal.TypeGeneratorTests.TypeGenerator;
import dnal.dio.PositionDIO;
import dnal.myformat.TypeTests;
import dnal.myformat.DNALLoaderTests.DNALLoader;
import dnal.myformat.DNALParserTests.FileScanner;
import dnal.myformat.TypeTests.ITypeValidator;
import dnal.myformat.TypeTests.MockEnumValidator;
import dnal.myformat.TypeTests.MockIntValidator;
import dnal.myformat.TypeTests.MockListStringValidator;
import dnal.myformat.TypeTests.MockStructValidator;
import dnal.myformat.TypeTests.ValidationResult;
import dnal.myformat.TypeTests.ValidatorBase;

public class RegistryTests extends BaseTest {
	
	public static class TypeDesc {
		public ITypeValidator validator;
		public String baseType; //!!later support packages (i.e. two packages can have type of same name)
		public DType customDType;
	}

	public static class TypeRegistry {
		private Map<String,TypeDesc> map = new HashMap<>();
		public ITypeGenerator generator;
		
		public void add(String type, String baseType, ITypeValidator validator, DType customDType) {
			TypeDesc desc = new TypeDesc();
			desc.validator = validator;
			desc.baseType = baseType;
			desc.customDType = customDType;
			map.put(type, desc);
		}

		public ITypeValidator find(String type) {
			TypeDesc desc = map.get(type);
			if (desc == null) {
				return null;
			}
			return desc.validator;
		}
		public String findBaseType(String type) {
			TypeDesc desc = map.get(type);
			if (desc == null) {
				return null;
			}
			return desc.baseType;
		}
		public DType findCustomDType(String type) {
			TypeDesc desc = map.get(type);
			if (desc == null) {
				return null;
			}
			return desc.customDType;
		}
	}

	public static class RegistryBuilder {

		public TypeRegistry buildRegistry() {
			TypeRegistry registry = new TypeRegistry();
			registry.add("int", "SIMPLEPRIMITIVE", new MockIntValidator(), null);
			registry.add("string", "SIMPLEPRIMITIVE", new TypeTests.MockStringValidator(), null);
			registry.add("boolean", "SIMPLEPRIMITIVE", new TypeTests.MockBooleanValidator(), null);

			//!!later support lists of other types
			registry.add("list<string>", "PRIMITIVE", new TypeTests.MockListStringValidator(), null);
			
			//toml
			MockStructValidator structVal = new MockStructValidator();
			structVal.registry = registry;
			registry.add("struct", "PRIMITIVE", structVal, null);

			MockEnumValidator enumVal = new MockEnumValidator();
			enumVal.registry = registry;
			registry.add("enum", "PRIMITIVE", enumVal, null);
			return registry;
		}
	}
	
	public static class MockCustomTypeValidator extends ValidatorBase {
		public TypeRegistry registry;
		private DType customDType;
		
		@Override
		protected void doValue(ValidationResult result, DValue dval, Object inputObj) {
			try {
				result.isValid = true;
				result.validObj = dval.finalValue;
//				result.validObj = inputObj.toString();
//				dval.finalValue = result.validObj;
			} catch(NumberFormatException e) {
				addError(result, dval, "not an string");
			}
		}
		
		@Override
		protected void prepareForSubObj(DValue dval)
		{
			customDType = registry.findCustomDType(dval.type);
		}
		
		@Override
		protected void buildSubObj(DValue dval)
		{
//			dval.finalValue = "should be a position obj"; //!!
			dval.finalValue = registry.generator.createImmutableObject(dval);
		}
		
		@Override
		protected void doSubValue(ValidationResult result, DValue dval)
		{
			String subType = null;
			for(DTypeEntry entry: customDType.entries) {
				if (entry.name.equals(dval.name)) {
					subType = entry.type;
					break;
				}
			}
			
			if (subType == null) {
				addError(result, dval, "can't find type of: " + dval.name);
				return;
			} else {
				dval.type = subType;
			}
			
			ITypeValidator subval = registry.find(subType);
			if (subval == null) {
				this.addError(result, dval, "missing val for sub: " + dval.name);
			} else {
				
				ValidationResult tmp = subval.validate(dval, dval.rawValue);
				result.isValid = tmp.isValid;
//				dval.finalValue = tmp.validObj;
			}
		}
	}


	public static class TypeValidator {
		public TypeRegistry registry;
		public List<ValidationError> errors = new ArrayList<>();
		public int addedCount;
		private ParseErrorTracker errorTracker = new ParseErrorTracker();

		public TypeValidator(ParseErrorTracker errorTracker, TypeRegistry registry) {
			this.errorTracker = errorTracker;
			this.registry = registry;
		}

		public boolean validate(List<DType> typeL) {
			for(DType dtype: typeL) {
				boolean ok = false;
				
				ITypeValidator validator = registry.find(dtype.name);
				if (validator == null) {
					ok = true;
				} else {
					addError(dtype, "already existing type: " + dtype.name);
					return false;
				}
				
				boolean alreadyAdded = false;
				validator = registry.find(dtype.baseType); //later need to walk back till find primitive!!
				if (validator == null) {
					ok = isStruct(dtype) || isEnum(dtype);
					if (! ok) {
						addError(dtype, "unknown base type: " + dtype.baseType);
						return false;
					}
				}
				
				if (dtype.entries != null && dtype.entries.size() > 0) {
					if (isStruct(dtype)) {
						for(DTypeEntry sub: dtype.entries) {
							if (! ensureExists(dtype, sub.type)) {
								ok = false;
							}
						}
					} else if (isEnum(dtype)) {
						registry.add(dtype.name, dtype.baseType, validator, dtype);
						alreadyAdded = true;
						addedCount++;
					} else {
						addError(dtype, String.format("'%s' is not a struct", dtype.name));
						ok = false;
					} 
				} else {
					if (isSimplePrimitive(dtype)) {
						registry.add(dtype.name, dtype.baseType, validator, dtype);
						alreadyAdded = true;
						addedCount++;
					}
				}				
				
				if (ok) {
					if (isList(dtype)) {
						MockListStringValidator custom = new MockListStringValidator();
						registry.add(dtype.name, dtype.baseType, custom, dtype);
						addedCount++;
					} else if (! alreadyAdded){
						MockCustomTypeValidator custom = new MockCustomTypeValidator();
						custom.registry = registry;
						registry.add(dtype.name, dtype.baseType, custom, dtype);
						addedCount++;
					}
				}
			}
			return (errors.size() == 0);
		}
		
		private boolean isSimplePrimitive(DType dtype) {
			return isSomeBaseType(dtype, "SIMPLEPRIMITIVE");
		}

		private boolean isEnum(DType dtype) {
			return isSomeBaseType(dtype, "enum");
		}

		private boolean isList(DType dtype) {
			String currentBaseType = dtype.baseType;

			for(int i = 0; i < 100; i++) {
				if (currentBaseType.startsWith("list<")) {
					return true;
				}
					
				String baseType = registry.findBaseType(currentBaseType);
				if (baseType == null) {
					return false;
				}
				currentBaseType = baseType;
			}
			//!!runaway
			System.out.println("RUNAWAY!!");
			return false;
		}

		private boolean isStruct(DType dtype) {
			return isSomeBaseType(dtype, "struct");
		}
		private boolean isSomeBaseType(DType dtype, String targetBaseType) {
			String currentBaseType = dtype.baseType;

			for(int i = 0; i < 100; i++) {
				if (currentBaseType.startsWith(targetBaseType)) {
					return true;
				}
					
				String baseType = registry.findBaseType(currentBaseType);
				if (baseType == null) {
					return false;
				}
				currentBaseType = baseType;
			}
			//!!runaway
			System.out.println("RUNAWAY!!");
			return false;
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
			
			errorTracker.addError(String.format("validation error: %s: %s", err.fieldName, err.error));
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
		TypeValidator validator = createValidator();
		boolean b = validator.validate(typeL);
		checkValidator(validator, b, true, 1);
	}
	
	private TypeValidator createValidator() {
		ParseErrorTracker errorTracker = new ParseErrorTracker();
		RegistryBuilder builder = new RegistryBuilder();
		TypeValidator validator = new TypeValidator(errorTracker, builder.buildRegistry());
		return validator;
	}
	
	@Test
	public void testTypeValidatorSubBad() {
		List<DType> typeL = buildList("Customer", "struct", true, "zzzz");
		
		TypeValidator validator = createValidator();
		boolean b = validator.validate(typeL);
		checkValidator(validator, b, false, 0);
	}
	@Test
	public void testTypeValidatorSubNotStruct() {
		List<DType> typeL = buildList("Customer", "int", true, "int");
		TypeValidator validator = createValidator();
		boolean b = validator.validate(typeL);
		checkValidator(validator, b, false, 0);
	}
	
	//---
	private void checkValidator(TypeValidator validator, boolean b, boolean bExpected, int expectedAdded) {
		dumpErrors(validator);
		assertEquals(bExpected, b);
		assertEquals(expectedAdded, validator.addedCount);
	}
	
	private void goodOne(String name, String baseType) {
		List<DType> typeL = buildList(name, baseType);
		TypeValidator validator = createValidator();
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
		TypeValidator validator = createValidator();
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
