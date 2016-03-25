package dnal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mef.dnal.core.DValue;
import org.mef.dnal.validation.ValidationError;
import org.thingworld.sfx.SfxTextReader;

import testhelper.BaseTest;
import dnal.DNALParserTests.FileScanner;
import dnal.RegistryTests.RegistryBuilder;
import dnal.RegistryTests.TypeRegistry;
import dnal.TypeTests.ITypeValidator;
import dnal.TypeTests.ValidationResult;

public class DNALLoaderTests extends BaseTest {
	
	
	public static class DNALLoader {
		public TypeRegistry registry;
		public List<ValidationError> errors = new ArrayList<>();
		private List<DValue> dataL;
		private boolean success;
		
		public boolean load(String path) {
			SfxTextReader reader = new SfxTextReader();
			List<String> lines = reader.readFile(path);
			success = load(lines);
			return success;
		}
		public boolean isValid() {
			return success;
		}
		public boolean load(List<String> lines) {

			FileScanner scanner = new FileScanner();
			boolean b = scanner.scan(lines);
			if (b) {
				b = validate(scanner.valueL);
			}
			
			if (b) {
				dataL = scanner.valueL;
			}
			
			return b;
		}

		private boolean validate(List<DValue> valueL) {
			int failCount = 0;
			for(DValue dval: valueL) {
				ITypeValidator validator = registry.find(dval.type);
				if (validator != null) {
					ValidationResult result = validator.validate(dval, dval.rawValue);
					if (! result.isValid) {
						failCount++;
						errors.addAll(result.errors);
					} else if (dval.finalValue == null) {
						ValidationError err = new ValidationError();
						err.fieldName = dval.name;
						err.error = "null finalValue";
						errors.add(err);
						failCount++;
					}
				} else {
					ValidationError err = new ValidationError();
					err.fieldName = dval.name;
					err.error = "missing validator for type: " + dval.type;
					errors.add(err);
					failCount++;
				}
			}
			return (failCount == 0);
		}

		public List<DValue> getDataL() {
			return dataL;
		}
	}

	@Test
	public void test() {
		List<String> lines = buildFile(0);
		DNALLoader loader = new DNALLoader();
		loader.registry = buildRegistry();
		boolean b = loader.load(lines);
		assertEquals(true, b);
		assertEquals(1, loader.getDataL().size());
	}
	@Test
	public void test1() {
		List<String> lines = buildFile(1);
		DNALLoader loader = new DNALLoader();
		loader.registry = buildRegistry();
		boolean b = loader.load(lines);
		assertEquals(false, b);
		for(ValidationError err: loader.errors) {
			log(String.format("%s: %s", err.fieldName, err.error));
		}
	}
	@Test
	public void testFile() {
		String path = "./test/testfiles/file1.dnal";
		DNALLoader loader = new DNALLoader();
		loader.registry = buildRegistry();
		boolean b = loader.load(path);
		assertEquals(true, b);
		assertEquals(1, loader.getDataL().size());
		assertEquals("size", loader.getDataL().get(0).name);
	}
	@Test
	public void testFile2() {
		String path = "./test/testfiles/file2.dnal";
		DNALLoader loader = new DNALLoader();
		loader.registry = buildRegistry();
		boolean b = loader.load(path);
		assertEquals(true, b);
		assertEquals(3, loader.getDataL().size());
		assertEquals("size", loader.getDataL().get(0).name);
		assertEquals("firstName", loader.getDataL().get(1).name);
		assertEquals("flag", loader.getDataL().get(2).name);

		checkInt(100, loader.getDataL().get(0));
		assertEquals("sue", loader.getDataL().get(1).finalValue);
		checkBool(true, loader.getDataL().get(2));
	}
	
	private void checkBool(boolean b, DValue dval) {
		Boolean bb = (Boolean) dval.finalValue;
		assertEquals(b, bb.booleanValue());
	}
	private void checkInt(int i, DValue dval) {
		Integer n = (Integer) dval.finalValue;
		assertEquals(i, n.intValue());
	}
	private TypeRegistry buildRegistry() {
		RegistryTests.RegistryBuilder builder = new RegistryBuilder();
		return builder.buildRegistry();
	}
	
	private List<String> buildFile(int scenario) {
		List<String> L = new ArrayList<>();
		switch(scenario) {
		case 0:
			L.add("");
			L.add("package a.b.c");
			L.add(" int size: 45");
			L.add("end");
			L.add("");
			break;
		case 1:
			L.add("");
			L.add("package a.b.c");
			L.add(" int size: zoo");
			L.add("end");
			L.add("");
			break;
		}
		return L;
	}
	

}
