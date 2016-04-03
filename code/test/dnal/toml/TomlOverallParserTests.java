package dnal.toml;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;
import org.mef.dnal.core.DType;
import org.mef.dnal.core.DTypeEntry;
import org.mef.dnal.core.DValue;
import org.mef.dnal.core.IDNALLoader;
import org.mef.dnal.core.IOverallFileScanner;
import org.mef.dnal.core.ITypeFileScanner;
import org.mef.dnal.parser.ParseErrorTracker;
import org.thingworld.sfx.SfxTextReader;

import com.moandjiezana.toml.Toml;

import testhelper.BaseTest;
import dnal.DNALLoadValidatorTests;
import dnal.RegistryTests;
import dnal.TypeGeneratorTests;
import dnal.DNALLoadValidatorTests.DNALLoadValidator;
import dnal.RegistryTests.RegistryBuilder;
import dnal.RegistryTests.TypeRegistry;
import dnal.RegistryTests.TypeValidator;
import dnal.TypeGeneratorTests.ITypeGenerator;
import dnal.TypeGeneratorTests.TypeGenerator;
import dnal.dio.PositionMutator;


public class TomlOverallParserTests extends BaseTest {

	public static enum OTState {
		WANT_START,
		INSIDE_TYPES,
		INSIDE_DATA,
		ERROR
	}

	public static class TomlOverallFileScanner implements IOverallFileScanner {
		private ParseErrorTracker errorTracker;
		private ITypeFileScanner tscanner;
		private IDNALLoader dloader;
		private DNALLoadValidator loadValidator;
		private boolean success;
		private TypeRegistry registry;
		private ITypeGenerator generator;

		public TomlOverallFileScanner(ParseErrorTracker errorTracker, IDNALLoader dloader, ITypeGenerator gen, ITypeFileScanner tscanner) {
			this.errorTracker = errorTracker;
			this.generator = gen;
			this.dloader = dloader;
			this.tscanner = tscanner;
		}
		@Override
		public boolean load(String path) {
			SfxTextReader reader = new SfxTextReader();
			List<String> lines = reader.readFile(path);
			success = scan(lines);
			return success;
		}
		@Override
		public boolean isValid() {
			return success;
		}

		@Override
		public void dumpErrors() {
			this.errorTracker.dumpErrors();
		}

		@Override
		public boolean scan(List<String> fileL) {
			RegistryTests.RegistryBuilder builder = new RegistryBuilder();
			this.registry = builder.buildRegistry();
			this.registry.generator = generator;
			
			boolean b = tscanner.scan(fileL);
			if (!b) {
				return b;
			}
			this.log(String.format("%d types found", tscanner.getDTypes().size()));
			RegistryTests.TypeValidator typeValidator = new TypeValidator(errorTracker, registry);
			b = typeValidator.validate(tscanner.getDTypes());
			if (!b) {
				return b;
			}
			
			log("load data..");
			b = dloader.load(fileL);
			if (!b) {
				return b;
			}
			
			loadValidator = new DNALLoadValidator(errorTracker);
			loadValidator.registry = registry;
			b = loadValidator.validate(dloader.getDataL());
			return b;
		}

		private void log(String s) {
			System.out.println(s);
		}
		public ITypeFileScanner getTscanner() {
			return tscanner;
		}
		public IDNALLoader getDloader() {
			return dloader;
		}
	}

	@Test
	public void testF0() {
		List<String> fileL = buildFile(0);

		ITypeGenerator gen = createGenerator();
		TomlOverallFileScanner scanner = createScanner();
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSizes(scanner, 0, 0);
	}
	
	private void checkSizes(TomlOverallFileScanner scanner, int tsize, int dsize) {
		assertEquals(dsize, scanner.getDloader().getDataL().size());
		assertEquals(tsize, scanner.getTscanner().getDTypes().size());
	}
	
	@Test
	public void testF1() {
		List<String> fileL = buildFile(1);

		TomlOverallFileScanner scanner = createScanner();
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSizes(scanner, 1, 0);
		checkEntrySize(0, scanner.tscanner.getDTypes().get(0).entries);
		checkDType(scanner.tscanner.getDTypes().get(0), "int", "Timeout");
	}
	@Test
	public void testF2() {
		List<String> fileL = buildFile(2);

		TomlOverallFileScanner scanner = createScanner();
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSizes(scanner, 1, 1);
		checkEntrySize(0, scanner.tscanner.getDTypes().get(0).entries);
		checkDType(scanner.tscanner.getDTypes().get(0), "int", "Timeout");
		
		List<DValue> list = scanner.dloader.getDataL();
		this.checkDValInt(scanner.dloader, 0, 0, "Timeout", "size", 10);
	}
	@Test
	public void testF3() {
		List<String> fileL = buildFile(3);

		TomlOverallFileScanner scanner = createScanner();
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSizes(scanner, 0, 1);
		this.checkDValInt(scanner.dloader, 0, 0, "int", "size", 10);
	}
	@Test
	public void testFile2() {
		String path = "./test/testfiles/file2.toml";
		TomlOverallFileScanner scanner = createScanner();
		boolean b = scanner.load(path);
		assertEquals(true, b);
	}


	private TomlOverallFileScanner createScanner() {
		ITypeGenerator gen = createGenerator();
		ParseErrorTracker errorTracker = new ParseErrorTracker();
		IDNALLoader dloader = new TomlDNALLoaderTests.TomlDNALLoader(errorTracker);
		ITypeFileScanner tscanner = new TomlTypeParserTests.TomlTypeFileScanner(errorTracker);
		TomlOverallFileScanner scanner = new TomlOverallFileScanner(errorTracker, dloader, gen, tscanner);
		return scanner;
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
	private void checkDValInt(IDNALLoader loader, int i, int j, String type, String name, int k) {
		Integer n = (Integer) loader.getDataL().get(i).valueList.get(j).finalValue;
		assertEquals(k, n.intValue());
		assertEquals(type, loader.getDataL().get(i).valueList.get(j).type);
		assertEquals(name, loader.getDataL().get(i).valueList.get(j).name);
	}

	private void checkStructVal(IDNALLoader loader, int i, String string) {
		assertEquals(string, loader.getDataL().get(i).name);
		assertEquals("struct", loader.getDataL().get(i).type);
	}
	private void checkEntrySize(int expectedSize, List<DTypeEntry> list) {
		assertEquals(expectedSize, list.size());
	}
	private void checkDType(DType dtype, String type, String name) {
		assertEquals(type, dtype.baseType);
		assertEquals(name, dtype.name);
	}
	private TypeGenerator createGenerator() {
		TypeGenerator gen = new TypeGenerator();
		gen.register("Position", PositionMutator.class);
		return gen;
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
			add("[TYPE.Timeout]");
			add("BASE = 'int'");
			break;
		case 2:
			add("[TYPE.Timeout]");
			add("BASE = 'int'");
			add("");
			add("[Foo]");
			add("Timeout__size = 10");
			break;
		case 3:
			add("");
			add("[Foo]");
			add("int__size = 10");
			break;
//		case 3:
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


}
