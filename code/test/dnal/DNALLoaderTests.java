package dnal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.junit.Test;
import org.mef.dnal.core.DValue;
import org.mef.dnal.validation.ValidationError;
import org.thingworld.sfx.SfxTextReader;

import testhelper.BaseTest;
import dnal.DNALParserTests.FileScanner;
import dnal.TypeTests.ITypeValidator;
import dnal.TypeTests.MockIntValidator;
import dnal.TypeTests.ValidationResult;

public class DNALLoaderTests extends BaseTest {
	
	public static class TypeRegistry {
		private Map<String,ITypeValidator> map = new HashMap<>();
		
		public void add(String type, ITypeValidator validator) {
			map.put(type, validator);
		}

		public ITypeValidator find(String type) {
			return map.get(type);
		}
	}
	
	public static class DNALLoader {
		public TypeRegistry registry;
		public List<ValidationError> errors = new ArrayList<>();
		private List<DValue> dataL;
		
		public boolean load(String path) {
			SfxTextReader reader = new SfxTextReader();
			List<String> lines = reader.readFile(path);
			return load(lines);
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
					ValidationResult result = validator.validate(dval, dval.value);
					if (! result.isValid) {
						failCount++;
						errors.addAll(result.errors);
					}
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
	
	private TypeRegistry buildRegistry() {
		TypeRegistry registry = new TypeRegistry();
		registry.add("int", new MockIntValidator());
		return registry;
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
