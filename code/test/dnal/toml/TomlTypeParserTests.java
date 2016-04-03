package dnal.toml;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Map.Entry;

import org.junit.Test;
import org.mef.dnal.core.DType;
import org.mef.dnal.core.DTypeEntry;
import org.mef.dnal.core.ITypeFileScanner;
import org.mef.dnal.parser.ParseErrorTracker;

import com.moandjiezana.toml.Toml;

import testhelper.BaseTest;


public class TomlTypeParserTests extends BaseTest {
	
	public static class TomlTypeFileScanner implements ITypeFileScanner {
		private ParseErrorTracker errorTracker;
		public List<DType> typeL = new ArrayList<>();

		public TomlTypeFileScanner(ParseErrorTracker errorTracker) {
			this.errorTracker = errorTracker;
		}

		@Override
		public boolean scan(List<String> fileL) {
			
			StringBuilder sb = new StringBuilder();
			for(String tmp: fileL) {
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
				return true; //may not have been a syntax error in TYPEs. fix later!!
			}
			
			Toml toml2 = toml.getTable("TYPE");
			if (toml2 != null) {
				for(Entry<String, Object> entry: toml2.entrySet()) {
//					log("2: " + entry.getKey());
					createDType(toml, entry);
				}
			}
			
			return true;
		}
		
		private void createDType(Toml rootToml, Entry<String, Object> tentry) {
			DType dtype = new DType();
			dtype.name = tentry.getKey();
			
			String key = "TYPE." + dtype.name;
			Toml toml = rootToml.getTable(key);
			String baseType = toml.getString("BASE");
			dtype.baseType = (baseType == null) ? "struct" : baseType;

			List<String> membL = toml.getList("CONTAINS");
			if (membL != null) {
				dtype.entries = new ArrayList<>();
				for(String field: membL) {
					DTypeEntry entry = parseEntry(field);
					dtype.entries.add(entry);
				}
			} else {
				membL = toml.getList("ENUM");
				if (membL != null) {
					if (baseType == null) {
						dtype.baseType = "enum";
					}
					dtype.entries = new ArrayList<>();
					
					for(String field: membL) {
						DTypeEntry entry = parseEntry(field);
						entry.name = entry.type;
						entry.type = "enumvalue";
						dtype.entries.add(entry);
					}
				}
			}
			
			
			this.typeL.add(dtype);
		}

		private void log(String s) {
			System.out.println(s);
		}

		@Override
		public List<DType> getDTypes() {
			return typeL;
		}
		
		private DTypeEntry parseEntry(String input) {
			Scanner scan = new Scanner(input);
			DTypeEntry entry = new DTypeEntry();
			
			int state = 0;
			while(scan.hasNext()) {
				String tok = scan.next();
				log(tok);
				tok = tok.trim();
				switch(state) {
				case 0:
					entry.type = tok;
					state = 1;
					break;
				case 1:
					entry.name = tok;
					state = 2;
					break;
				case 2:
				default:
					errorTracker.addError("unexpected token:" + tok + " state:" + state);
					break;
				}
			}
			scan.close();
			return entry;
		}
	}

	@Test
	public void testF0() {
		List<String> fileL = buildFile(0);
		ITypeFileScanner scanner = createScanner();
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
	}
	
	private ITypeFileScanner createScanner() {
		ParseErrorTracker errorTracker = new ParseErrorTracker();
		ITypeFileScanner scanner = new TomlTypeFileScanner(errorTracker);
		return scanner;
	}

	@Test
	public void testF1() {
		List<String> fileL = buildFile(1);

		ITypeFileScanner scanner = createScanner();
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSize(1, scanner.getDTypes());
		checkEntrySize(2, scanner.getDTypes().get(0).entries);
		checkDType(scanner, 0, "struct", "Position");
		checkEntry(scanner, 0, 0, "int", "x");
		checkEntry(scanner, 0, 1, "int", "y");
	}

	@Test
	public void testF2() {
		List<String> fileL = buildFile(2);

		ITypeFileScanner scanner = createScanner();
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSize(2, scanner.getDTypes());
		checkEntrySize(2, scanner.getDTypes().get(0).entries);
		int i = 0;
		checkDType(scanner, i, "struct", "Position");
		checkEntry(scanner, i, 0, "int", "x");
		checkEntry(scanner, 0, 1, "int", "y");

		i = 1;
		checkDType(scanner, i, "struct", "Person");
		checkEntry(scanner, i, 0, "string", "firstName");
		checkEntry(scanner, i, 1, "string", "lastName");
	}
	@Test
	public void testF3() {
		List<String> fileL = buildFile(3);

		ITypeFileScanner scanner = createScanner();
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSize(1, scanner.getDTypes());
		checkEntrySize(0, scanner.getDTypes().get(0).entries);
		int i = 0;
		checkDType(scanner, i, "int", "Timeout");
	}
	@Test
	public void testF4() {
		List<String> fileL = buildFile(4);

		ITypeFileScanner scanner = createScanner();
		boolean b = scanner.scan(fileL);
		assertEquals(true, b);
		checkSize(1, scanner.getDTypes());
		checkEntrySize(2, scanner.getDTypes().get(0).entries);
		int i = 0;
		checkDType(scanner, i, "enum", "Colour");
		checkEntry(scanner, i, 0, "enumvalue", "RED");
		checkEntry(scanner, i, 1, "enumvalue", "BLACK");
	}
	
	private void checkEntry(ITypeFileScanner scanner, int i, int j,
			String string, String string2) {
		
		assertEquals(string, scanner.getDTypes().get(i).entries.get(j).type);
		assertEquals(string2, scanner.getDTypes().get(i).entries.get(j).name);
		
	}

	private void checkDType(ITypeFileScanner scanner, int i, String string,
			String string2) {
		checkDType(scanner.getDTypes().get(i), string, string2);
	}

	private void checkSize(int expectedSize, List<DType> list) {
		assertEquals(expectedSize, list.size());
	}
	private void checkEntrySize(int expectedSize, List<DTypeEntry> list) {
		assertEquals(expectedSize, list.size());
	}
	private void checkDType(DType dtype, String type, String name) {
		assertEquals(type, dtype.baseType);
		assertEquals(name, dtype.name);
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
			add("[TYPE.Position]");
			add("a = 1");
			add("BASE = 'struct'");
			add("CONTAINS = [");
			add("'int x',");
			add("'int y'");
			add("]");
			break;
		case 2:
//			add("[TYPE]");
			add("[TYPE.Position]");
			add("a = 1");
			add("BASE = 'struct'");
			add("CONTAINS = [");
			add("'int x',");
			add("'int y'");
			add("]");
			add("");
			add("[TYPE.Person]");
//			add("BASE = 'struct'");
			add("CONTAINS = [");
			add("'string firstName',");
			add("'string lastName'");
			add("]");
			break;
		case 3:
			add("[TYPE.Timeout]");
			add("BASE = 'int'");
			break;
		case 4:
			add("[TYPE.Colour]");
			add("BASE = 'enum'");
			add("ENUM = [ 'RED', 'BLACK' ]");
			break;
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
