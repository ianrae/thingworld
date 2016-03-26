package dnal.toml;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mef.dnal.core.DValue;
import org.mef.dnal.core.IDNALLoader;
import org.mef.dnal.parser.ParseErrorTracker;
import org.thingworld.sfx.SfxTextReader;

import testhelper.BaseTest;
import dnal.DNALLoadValidatorTests.DNALLoadValidator;
import dnal.RegistryTests;
import dnal.RegistryTests.RegistryBuilder;
import dnal.RegistryTests.TypeRegistry;

public class TomlDNALLoaderTests extends BaseTest {
	
	public static class TomlDNALLoader implements IDNALLoader {
		private List<DValue> dataL;
		private boolean success;
		private ParseErrorTracker errorTracker = new ParseErrorTracker();

		public TomlDNALLoader(ParseErrorTracker errorTracker) {
			this.errorTracker = errorTracker;
		}
		@Override
		public boolean load(String path) {
			SfxTextReader reader = new SfxTextReader();
			String content = reader.readUTF8File(path);
			List<String> list = Collections.singletonList(content);
			success = load(list);
			return success;
		}
		@Override
		public boolean isValid() {
			return success;
		}
		@Override
		public boolean load(List<String> lines) {

			boolean b = false;
			if (b) {
//				dataL = scanner.valueL;
			}
			
			return b;
		}

		@Override
		public List<DValue> getDataL() {
			return dataL;
		}
	}

	@Test
	public void test() {
		List<String> lines = buildFile(0);
		ParseErrorTracker errorTracker = new ParseErrorTracker();
		IDNALLoader loader = new TomlDNALLoader(errorTracker);
		boolean b = loader.load(lines);
		b = doValidation(b, loader, errorTracker);
		assertEquals(true, b);
		assertEquals(1, loader.getDataL().size());
	}
	
	private DNALLoadValidator loadValidator;
	private boolean doValidation(boolean b, IDNALLoader loader, ParseErrorTracker errorTracker) {
		if (!b) {
			return false;
		}
		loadValidator = new DNALLoadValidator(errorTracker);
		loadValidator.registry = buildRegistry();
		return loadValidator.validate(loader.getDataL());
	}
//	@Test
//	public void test1() {
//		List<String> lines = buildFile(1);
//		ParseErrorTracker errorTracker = new ParseErrorTracker();
//		IDNALLoader loader = new TomlDNALLoader(errorTracker);
//		boolean b = loader.load(lines);
//		b = doValidation(b, loader, errorTracker);
//		assertEquals(false, b);
//		for(ValidationError err: loadValidator.errors) {
//			log(String.format("%s: %s", err.fieldName, err.error));
//		}
//	}
//	@Test
//	public void testFile() {
//		String path = "./test/testfiles/file1.dnal";
//		ParseErrorTracker errorTracker = new ParseErrorTracker();
//		IDNALLoader loader = new TomlDNALLoader(errorTracker);
//		boolean b = loader.load(path);
//		b = doValidation(b, loader, errorTracker);
//		assertEquals(true, b);
//		assertEquals(1, loader.getDataL().size());
//		assertEquals("size", loader.getDataL().get(0).name);
//	}
//	@Test
//	public void testFile2() {
//		String path = "./test/testfiles/file2.dnal";
//		ParseErrorTracker errorTracker = new ParseErrorTracker();
//		IDNALLoader loader = new TomlDNALLoader(errorTracker);
//		boolean b = loader.load(path);
//		b = doValidation(b, loader, errorTracker);
//		assertEquals(true, b);
//		assertEquals(3, loader.getDataL().size());
//		assertEquals("size", loader.getDataL().get(0).name);
//		assertEquals("firstName", loader.getDataL().get(1).name);
//		assertEquals("flag", loader.getDataL().get(2).name);
//
//		checkInt(100, loader.getDataL().get(0));
//		assertEquals("sue mary", loader.getDataL().get(1).finalValue);
//		checkBool(true, loader.getDataL().get(2));
//	}
	
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
	
	
	//----
	private StringBuilder sb;
	private List<String> buildFile(int scenario) {
		sb = new StringBuilder();
		switch(scenario) {
		case 0:
			add("");
			break;
		case 1:
			add("[Foo]");
			add("a = 1");
			add("'int size' = 45");
			break;
//		case 2:
////			add("[TYPE]");
//			add("[TYPE.Position]");
//			add("a = 1");
//			add("BASE = 'struct'");
//			add("MEMBERS = [");
//			add("'int x',");
//			add("'int y'");
//			add("]");
//			add("");
//			add("[TYPE.Person]");
////			add("BASE = 'struct'");
//			add("MEMBERS = [");
//			add("'string firstName',");
//			add("'string lastName'");
//			add("]");
//			break;
//		case 3:
//			add("[TYPE.Timeout]");
//			add("BASE = 'int'");
//			break;
		default:
			break;
		}
		
		String s = sb.toString();
		return Collections.singletonList(s);
	}
	private void add(String string) {
		sb.append(string);
		sb.append("\n");
	}

	protected static String fix(String s)
	{
		s = s.replace('\'', '"');
		return s;
	}
	
	private List<String> xxxbuildFile(int scenario) {
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
