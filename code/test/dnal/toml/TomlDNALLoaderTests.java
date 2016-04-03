package dnal.toml;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mef.dnal.core.DValue;
import org.mef.dnal.core.IDNALLoader;
import org.mef.dnal.parser.ParseErrorTracker;
import org.thingworld.sfx.SfxTextReader;

import com.moandjiezana.toml.Toml;

import testhelper.BaseTest;
import dnal.DNALLoadValidatorTests.DNALLoadValidator;
import dnal.RegistryTests;
import dnal.RegistryTests.RegistryBuilder;
import dnal.RegistryTests.TypeRegistry;

public class TomlDNALLoaderTests extends BaseTest {

	public static class TomlDNALLoader implements IDNALLoader {
		private List<DValue> dataL = new ArrayList<>();
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
		private void log(String s) {
			System.out.println(s);
		}
		@Override
		public boolean load(List<String> lines) {

			StringBuilder sb = new StringBuilder();
			for(String tmp: lines) {
				sb.append(tmp);
				sb.append("\n");
			}
			String input = sb.toString();
			Toml toml = null;
			try {
				toml = new Toml().read(input);
			} catch (IllegalStateException e) {
				errorTracker.addError("TOML error:" + e.getMessage());
			}

			if (toml == null) {
				return false;
			}

			for(Entry<String, Object> entry: toml.entrySet()) {
				log(entry.getKey());
				if (entry.getKey().startsWith("TYPE")) {
					continue;
				}
				createDValue(toml, entry);
			}

			boolean b = true;
			return b;
		}

		private void createDValue(Toml rootToml, Entry<String, Object> entry) {
			String keyx = entry.getKey();
			Toml toml = rootToml.getTable(keyx);
			Map<String, Object> map = toml.to(Map.class);		
			DValue dval = createStrutDValueFromMap(keyx, map);
			dataL.add(dval);
		}
		private DValue createStrutDValueFromMap(String keyx, Map<String, Object> map) {
			DValue dval = new DValue();
			dval.type = "struct";
			dval.name = keyx;
			for(String key: map.keySet()) {
				log(key);
				if (dval.valueList == null) {
					dval.valueList = new ArrayList<>();
				}

				DValue childDVal = parseEntry(key);
				String raw = getAsString(map, key);
				childDVal.rawValue = raw;
				childDVal.finalValue = getAsFinalValue(map, key, childDVal);
				dval.valueList.add(childDVal);
			}
			return dval;
		}
		private DValue createDValueFromMap(String keyx, Map<String, Object> map) {
			DValue dval = parseEntry(keyx);

			String raw = getAsString(map, keyx);
			dval.rawValue = raw;
			dval.finalValue = getAsFinalValue(map, keyx, dval);
			return dval;
		}


		private Object getAsFinalValue(Map<String, Object> map, String key, DValue dval) {
			Object obj = map.get(key);
			if (obj == null) {
				return null;
			}

			if (obj instanceof ArrayList) {
				dval.tmplist = new ArrayList<>();
				ArrayList L = (ArrayList) obj;
				for(Object el: L) {
					System.out.println(el);
					dval.tmplist.add(el.toString());
				}
				return null;
			}

			if (obj instanceof Double) {
				Double d = (Double) obj;
				return Long.valueOf(d.longValue());
			}

			if (obj instanceof Map) {
				Map xmap = (Map) obj;
				dval.valueList = new ArrayList<>();
				for(Object keyObj : xmap.keySet()) {
					String innerKey = keyObj.toString();
					DValue dvalz = createStrutDValueFromMap(innerKey, xmap);
					dval.valueList.add(dvalz);
				}
				return null;
			}

			return obj;
		}
		private String getAsString(Map<String, Object> map, String key) {
			Object obj = map.get(key);
			if (obj == null) {
				return null;
			}

			if (obj instanceof Double) {
				Double d = (Double) obj;
				return Long.valueOf(d.longValue()).toString();
			}

			return obj.toString();
		}
		@Override
		public List<DValue> getDataL() {
			return dataL;
		}

		private DValue parseEntry(String input) {
			input = input.replace("__", " "); //!!
			input = input.replace("\"", "");
			Scanner scan = new Scanner(input);
			DValue dval = new DValue();


			int state = 0;
			while(scan.hasNext()) {
				String tok = scan.next();
				log(tok);
				tok = tok.trim();
				switch(state) {
				case 0:
					//					if (tok.startsWith("list_")) {
					//						String elType = StringUtils.substringAfter(tok, "list_");
					//						dval.type = String.format("list<%s>", elType);
					//					} else {
					dval.type = tok;
					//					}
					state = 1;
					break;
				case 1:
					dval.name = tok;
					state = 2;
					break;
				case 2:
				default:
					errorTracker.addError("unexpected token:" + tok + " state:" + state);
					break;
				}
			}
			scan.close();
			return dval;
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
		assertEquals(0, loader.getDataL().size());
	}
	@Test
	public void test1() {
		List<String> lines = buildFile(1);
		ParseErrorTracker errorTracker = new ParseErrorTracker();
		IDNALLoader loader = new TomlDNALLoader(errorTracker);
		boolean b = loader.load(lines);
		b = doValidation(b, loader, errorTracker);
		errorTracker.dumpErrors();
		assertEquals(true, b);
		assertEquals(1, loader.getDataL().size());
		checkStructVal(loader, 0, "Foo");
		checkDValInt(loader, 0, 0, "size", 45);
	}
	@Test
	public void test2() {
		List<String> lines = buildFile(2);
		ParseErrorTracker errorTracker = new ParseErrorTracker();
		IDNALLoader loader = new TomlDNALLoader(errorTracker);
		boolean b = loader.load(lines);
		b = doValidation(b, loader, errorTracker);
		errorTracker.dumpErrors();
		assertEquals(true, b);
		assertEquals(1, loader.getDataL().size());
		checkStructVal(loader, 0, "Foo");
		checkDValString(loader, 0, 0, "size", "five");
	}
	@Test
	public void test3() {
		List<String> lines = buildFile(3);
		ParseErrorTracker errorTracker = new ParseErrorTracker();
		IDNALLoader loader = new TomlDNALLoader(errorTracker);
		boolean b = loader.load(lines);
		b = doValidation(b, loader, errorTracker);
		errorTracker.dumpErrors();
		assertEquals(true, b);
		assertEquals(1, loader.getDataL().size());
		checkStructVal(loader, 0, "Foo");
		checkDValBoolean(loader, 0, 0, "size", true);
	}
	@Test
	public void test4() {
		List<String> lines = buildFile(4);
		ParseErrorTracker errorTracker = new ParseErrorTracker();
		IDNALLoader loader = new TomlDNALLoader(errorTracker);
		boolean b = loader.load(lines);
		b = doValidation(b, loader, errorTracker);
		errorTracker.dumpErrors();
		assertEquals(false, b);
		assertEquals(1, loader.getDataL().size());
	}


	@Test
	public void testFile2() {
		String path = "./test/testfiles/file2.toml";
		ParseErrorTracker errorTracker = new ParseErrorTracker();
		IDNALLoader loader = new TomlDNALLoader(errorTracker);
		boolean b = loader.load(path);
		b = doValidation(b, loader, errorTracker);
		errorTracker.dumpErrors();
		assertEquals(true, b);
		assertEquals(1, loader.getDataL().size());
		checkStructVal(loader, 0, "Foo");
		checkDValInt(loader, 0, 0, "size", 100);
		checkDValString(loader, 0, 1, "firstName", "sue mary");
		checkDValBoolean(loader, 0, 2, "flag", true);
	}


	private void checkDValBoolean(IDNALLoader loader, int i, int j, String name, Boolean expected) {
		Boolean b = (Boolean) loader.getDataL().get(i).valueList.get(j).finalValue;
		assertEquals(expected, b);
		assertEquals("boolean", loader.getDataL().get(i).valueList.get(j).type);
		assertEquals(name, loader.getDataL().get(i).valueList.get(j).name);
	}
	private void checkDValString(IDNALLoader loader, int i, int j, String name, String expected) {
		String s = (String) loader.getDataL().get(i).valueList.get(j).finalValue;
		assertEquals(expected, s);
		assertEquals("string", loader.getDataL().get(i).valueList.get(j).type);
		assertEquals(name, loader.getDataL().get(i).valueList.get(j).name);
	}
	private void checkDValInt(IDNALLoader loader, int i, int j, String name, int k) {
		Integer n = (Integer) loader.getDataL().get(i).valueList.get(j).finalValue;
		assertEquals(k, n.intValue());
		assertEquals("int", loader.getDataL().get(i).valueList.get(j).type);
		assertEquals(name, loader.getDataL().get(i).valueList.get(j).name);
	}
	private void checkStructVal(IDNALLoader loader, int i, String string) {
		assertEquals(string, loader.getDataL().get(i).name);
		assertEquals("struct", loader.getDataL().get(i).type);
	}

	private DNALLoadValidator loadValidator;
	private boolean doValidation(boolean b, IDNALLoader loader, ParseErrorTracker errorTracker) {
		if (!b) {
			return false;
		}

		if (loader.getDataL().size() == 0) {
			return true;
		}

		loadValidator = new DNALLoadValidator(errorTracker);
		loadValidator.registry = buildRegistry();
		b = loadValidator.validate(loader.getDataL());
		return b;
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
			add("int__size = 45");
			//			add("\"int size\" = 45");
			break;
		case 2:
			add("[Foo]");
			add("string__size = 'five'");
			break;
		case 3:
			add("[Foo]");
			add("boolean__size = true");
			break;
		case 4:
			add("[Foo]");
			add("int__size = 'zoo'");
			break;
			//		case 2:
			////			add("[TYPE]");
			//			add("[TYPE.Position]");
			//			add("a = 1");
			//			add("BASE = 'struct'");
			//			add("CONTAINS = [");
			//			add("'int x',");
			//			add("'int y'");
			//			add("]");
			//			add("");
			//			add("[TYPE.Person]");
			////			add("BASE = 'struct'");
			//			add("CONTAINS = [");
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
